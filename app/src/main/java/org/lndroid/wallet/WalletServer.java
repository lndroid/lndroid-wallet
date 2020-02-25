package org.lndroid.wallet;

import android.content.Context;
import android.os.CancellationSignal;
import android.os.Messenger;
import android.util.Log;

import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.common.Errors;
import org.lndroid.framework.common.IResponseCallback;
import org.lndroid.framework.common.ISigner;
import org.lndroid.framework.common.PluginUtils;
import org.lndroid.framework.defaults.DefaultDaoProvider;
import org.lndroid.framework.defaults.DefaultKeyStore;
import org.lndroid.framework.defaults.DefaultSignAuthPrompt;
import org.lndroid.framework.engine.AuthClient;
import org.lndroid.framework.engine.IDaoConfig;
import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.engine.IKeyStore;
import org.lndroid.framework.client.PluginClientBuilder;
import org.lndroid.framework.engine.ISignAuthPrompt;
import org.lndroid.framework.engine.PluginServerStarter;
import org.lndroid.framework.engine.PluginUtilsLocal;

public class WalletServer {

    private static final String TAG = "WalletServer";

    static final int TOKEN_TTL = 3600000; // 1h

    private static final WalletServer instance_ = new WalletServer();

    private Messenger server_;
    private WalletData.UserIdentity userId_;
    private AuthClient authClient_;
    private IKeyStore keyStore_;

    // Dao config wrapping an ApplicationContext
    private static class DaoConfig implements IDaoConfig {
        private static final String WALLET_DB = "walletdb";
        private static final String LND_DIR = ".lnd";
        private static final String DATA_DIR = "data";
        private static final String PASSWORD_FILE = ".lndp";

        private Context ctx_;

        public DaoConfig(Context ctx) {
            ctx_ = ctx;
        }

        @Override
        public Context getContext() {
            return ctx_;
        }

        @Override
        public String getFilesPath() {
            return ctx_.getFilesDir().getAbsolutePath();
        }

        @Override
        public String getDataPath() {
            return getFilesPath() + "/" + DATA_DIR;
        }

        @Override
        public String getDatabaseName() {
            return WALLET_DB;
        }

        @Override
        public String getDatabasePath() {
            return ctx_.getDatabasePath(WALLET_DB).getAbsolutePath();
        }

        @Override
        public String getPasswordFileName() {
            return PASSWORD_FILE;
        }

        @Override
        public String getLndDirName() {
            return LND_DIR;
        }

        @Override
        public String getBackupPath() {
            return null;
        }
    }


    private WalletServer() {
    }

    private void startInstance(Context ctx, boolean mayExist) {

        userId_ = WalletData.UserIdentity.builder().setUserId(WalletData.ROOT_USER_ID).build();

        IDaoConfig cfg = new DaoConfig(ctx);
        keyStore_ = DefaultKeyStore.getInstance(ctx, cfg.getDataPath());

        server_ = new PluginServerStarter()
                .setDaoProvider(new DefaultDaoProvider(cfg, keyStore_))
                .setAuthComponentProvider(new AuthComponentProvider())
                .setKeyStore(keyStore_)
                .setDaoConfig(cfg)
                .start(mayExist);
        Log.i(TAG, "server "+server_);

        authClient_ = new AuthClient(server_);
        Log.i(TAG, "authClient "+authClient_);
    }

    public static void ensure(Context ctx) {
        instance_.startInstance(ctx, true);
    }

    public static void start(Context ctx) {
        instance_.startInstance(ctx, false);
    }

    public static WalletServer getInstance() {
        return instance_;
    }

    public static IPluginClient buildPluginClient() {
        return new PluginClientBuilder()
                .setUserIdentity(instance_.getCurrentUserId())
                .setServer(instance_.server())
                .build();
    }

    public Messenger server() {
        return server_;
    }

    public WalletData.UserIdentity getCurrentUserId() {
        return userId_;
    }

    public void getSessionToken(final Context ctx, final IResponseCallback<String> cb) {
        authClient_.getUserAuthInfo(userId_.userId(), new IResponseCallback<WalletData.User>() {

            @Override
            public void onResponse(final WalletData.User u) {

                if (u == null) {
                    cb.onError(Errors.UNKNOWN_CALLER, Errors.errorMessage(Errors.UNKNOWN_CALLER));
                    return;
                }

                final ISigner signer = keyStore_.getKeySigner(PluginUtils.userKeyAlias(u.id()));
                if (signer == null || !signer.getPublicKey().equals(u.pubkey())) {
                    Log.e(TAG, "keystore signer not available for "+u);
                    cb.onError(Errors.FORBIDDEN, Errors.errorMessage(Errors.FORBIDDEN));
                    return;
                }

                // Try to sign, if it fails we need auth and retry
                PluginUtilsLocal.SessionToken st = PluginUtilsLocal.prepareSessionToken(TOKEN_TTL, null);
                st.signature = signer.sign(st.payload);
                if (st.signature != null) {
                    cb.onResponse(st.formatToken());
                } else {

                    ISignAuthPrompt prompt = new DefaultSignAuthPrompt();
                    prompt.auth(signer, (FragmentActivity) ctx, u, new IResponseCallback() {

                        @Override
                        public void onResponse(Object o) {
                            PluginUtilsLocal.SessionToken st = PluginUtilsLocal.prepareSessionToken(TOKEN_TTL, null);
                            st.signature = signer.sign(st.payload);
                            if (st.signature != null) {
                                cb.onResponse(st.formatToken());
                            } else {
                                cb.onError(Errors.FORBIDDEN, Errors.errorMessage(Errors.FORBIDDEN));
                            }
                        }

                        @Override
                        public void onError(String s, String s1) {
                            cb.onError(s, s1);
                        }
                    });
                }
/*                ISigner signer = null;
                if (!WalletData.AUTH_TYPE_PASSWORD.equals(u.authType())) {
                    signer = keyStore_.getKeySigner(PluginUtils.userKeyAlias(u.id()));
                } else {
                    String password = "123456";
                    // FIXME show password UI, can't create signer w/o knowing the password!

                    signer = keyStore_.getPasswordKeySigner(
                            PluginUtils.userKeyAlias(userId_.userId()),
                            u.nonce(), password);
                    if (signer == null) {
                        Log.e(TAG, "failed to get password signer for "+userId_.userId());
                        cb.onError(Errors.FORBIDDEN, Errors.errorMessage(Errors.FORBIDDEN));
                        return;
                    }
                }

                // Try to sign, if it fails w/ need-auth exception then show BioPrompt and then retry
                PluginUtilsLocal.SessionToken st = PluginUtilsLocal.prepareSessionToken(10000, null);
                st.signature = signer.sign(st.payload);
                if (st.signature != null) {
                    cb.onResponse(st.formatToken());
                } else {

                    final ISigner authSigner = signer;
                    if (WalletData.AUTH_TYPE_DEVICE_SECURITY.equals(u.authType())
                            || WalletData.AUTH_TYPE_SCREEN_LOCK.equals(u.authType())
                            || WalletData.AUTH_TYPE_BIO.equals(u.authType())
                    ) {
                        BiometricPrompt.PromptInfo.Builder b = new BiometricPrompt.PromptInfo.Builder()
                                .setTitle("Please authorize")
                                .setSubtitle("Confirm your identity before accessing the wallet");

                        if (!WalletData.AUTH_TYPE_BIO.equals(u.authType()))
                            b.setDeviceCredentialAllowed(true);
                        else
                            b.setNegativeButtonText("Cancel");

                        BiometricPrompt biometricPrompt = new BiometricPrompt(
                                // FIXME
                                (FragmentActivity)ctx,
                                ctx.getMainExecutor(),
                                new BiometricPrompt.AuthenticationCallback() {

                                    @Override
                                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                                        super.onAuthenticationSucceeded(result);

                                        // retry
                                        PluginUtilsLocal.SessionToken st = PluginUtilsLocal.prepareSessionToken(3600000, null);
                                        if (WalletData.AUTH_TYPE_BIO.equals(u.authType()))
                                            st.signature = authSigner.sign(st.payload, result.getCryptoObject());
                                        else
                                            st.signature = authSigner.sign(st.payload);
                                        cb.onResponse(st.formatToken());
                                    }

                                    @Override
                                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                                        super.onAuthenticationError(errorCode, errString);
                                        Log.e(TAG, "device auth failed: " + errorCode + " error " + errString);
                                        cb.onError(Errors.FORBIDDEN, "" + errString);
                                    }


                                    @Override
                                    public void onAuthenticationFailed() {
                                        super.onAuthenticationFailed();
                                        Log.e(TAG, "device auth failed");
                                        cb.onError(Errors.FORBIDDEN, Errors.errorMessage(Errors.FORBIDDEN));
                                    }

                                }
                                );

                        if (WalletData.AUTH_TYPE_BIO.equals(u.authType())) {
                            biometricPrompt.authenticate(b.build(), (BiometricPrompt.CryptoObject)signer.getCryptoObject());
                        } else {
                            biometricPrompt.authenticate(b.build());
                        }
                    }
                }

 */
            }

            @Override
            public void onError(String s, String s1) {
                cb.onError(s, s1);
            }
        });
    }
}
