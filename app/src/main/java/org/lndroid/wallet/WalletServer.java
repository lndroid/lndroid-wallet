package org.lndroid.wallet;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Looper;
import android.os.Messenger;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.common.Errors;
import org.lndroid.framework.common.IResponseCallback;
import org.lndroid.framework.common.ISigner;
import org.lndroid.framework.common.PluginUtils;
import org.lndroid.framework.defaults.DefaultBroadcaster;
import org.lndroid.framework.defaults.DefaultDaoProvider;
import org.lndroid.framework.defaults.DefaultKeyStore;
import org.lndroid.framework.defaults.DefaultSignAuthPrompt;
import org.lndroid.framework.engine.AuthClient;
import org.lndroid.framework.engine.IDaoConfig;
import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.engine.IKeyStore;
import org.lndroid.framework.client.PluginClientBuilder;
import org.lndroid.framework.engine.PluginServerStarter;
import org.lndroid.framework.engine.PluginUtilsLocal;
import org.lndroid.framework.usecases.bg.PaidInvoicesNotifyWorker;

import java.util.concurrent.atomic.AtomicBoolean;

public class WalletServer {

    private static final String TAG = "WalletServer";

    static final int TOKEN_TTL = 3600000; // 1h

    private static final WalletServer instance_ = new WalletServer();

    private Messenger server_;
    private WalletData.UserIdentity userId_;
    private AuthClient authClient_;
    private IKeyStore keyStore_;
    private AtomicBoolean started_ = new AtomicBoolean(false);

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

    private void startInstance(final Context ctx, boolean mayExist) {

        if (server_ != null) {
            if (!mayExist)
                throw new RuntimeException("WalletServer already started");
            return;
        }

        userId_ = WalletData.UserIdentity.builder().setUserId(WalletData.ROOT_USER_ID).build();

        IDaoConfig cfg = new DaoConfig(ctx);
        keyStore_ = DefaultKeyStore.getInstance(ctx, cfg.getDataPath());

        server_ = new PluginServerStarter()
                .setDaoProvider(new DefaultDaoProvider(cfg, keyStore_))
                .setAuthComponentProvider(new AuthComponentProvider())
                .setBroadcaster(new DefaultBroadcaster(ctx))
                .setKeyStore(keyStore_)
                .setDaoConfig(cfg)
                .start(mayExist);
        Log.i(TAG, "server "+server_);

        authClient_ = new AuthClient(server_);
        Log.i(TAG, "authClient "+authClient_);

        PaidInvoicesNotifyWorker.getInstance()
                .setNotificationManager(new PaidInvoicesNotifyWorker.INotificationManager() {

                    @Override
                    public void createNotification(WalletData.PaidInvoicesEvent e) {
                        NotificationManager notificationManager = (NotificationManager)
                                ctx.getSystemService(Context.NOTIFICATION_SERVICE);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                                ctx, Application.PAID_INVOICE_CHANNEL_ID)
                                .setContentTitle("Payment received")
                                .setCategory(NotificationCompat.CATEGORY_EVENT)
                                // NOTE: this is required!
                                .setSmallIcon(R.drawable.ic_logo)
                                .setOngoing(false)
                                .setContentText("Sats: "+e.satsReceived()+", payments: "+e.invoicesCount())
                                .setDefaults(NotificationCompat.DEFAULT_ALL)
                                .setPriority(NotificationCompat.PRIORITY_MAX);

                        notificationManager.notify(0, builder.build());
                    }
                })
                .start(buildAnonymousPluginClient());

        started_.set(true);
    }

    public static void ensure(final Context ctx) {
        // make sure instance is started on the main thread
        // AND that this thread is blocked until the instance is started
        final boolean isMainThread = Looper.myLooper() == Looper.getMainLooper();
        if (isMainThread) {
            // if we're on the main thread then just execute synchronously
            instance_.startInstance(ctx, true);
        } else {
            // if we're not main thread then post a job on to the main
            // thread and wait until started_ turns to true
            ContextCompat.getMainExecutor(ctx).execute(new Runnable() {
                @Override
                public void run() {
                    instance_.startInstance(ctx, true);
                }
            });

            // FIXME use something less dumb, like condition variable or some such...
            while (!instance_.started_.get()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {}
            }
        }
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

    public static IPluginClient buildAnonymousPluginClient() {
        return new PluginClientBuilder()
                .setUserIdentity(WalletData.UserIdentity.builder().build())
                .setServer(instance_.server())
                .build();
    }

    public IKeyStore keyStore() { return keyStore_; }
    public Messenger server() {
        return server_;
    }

    public WalletData.UserIdentity getCurrentUserId() {
        return userId_;
    }

    public void getSessionToken(final FragmentActivity activity, final IResponseCallback<String> cb) {
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

                    DefaultSignAuthPrompt prompt = new DefaultSignAuthPrompt();
                    prompt.setPasswordAuthPrompt(PinAuthDialog.createPrompt());
                    prompt.auth(signer, activity, u, new IResponseCallback() {

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
            }

            @Override
            public void onError(String s, String s1) {
                cb.onError(s, s1);
            }
        });
    }
}
