package org.lndroid.wallet;

import android.os.Messenger;
import android.util.Log;

import org.lndroid.framework.DefaultIpcCodecProvider;
import org.lndroid.framework.IDaoConfig;
import org.lndroid.framework.WalletData;
import org.lndroid.framework.DefaultDaoProvider;
import org.lndroid.framework.common.ICodecProvider;
import org.lndroid.framework.engine.IAuthComponentProvider;
import org.lndroid.framework.engine.IDaoProvider;
import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.engine.IPluginProvider;
import org.lndroid.framework.client.PluginClientBuilder;
import org.lndroid.framework.engine.PluginServerStarter;
import org.lndroid.framework.DefaultPluginProvider;

public class WalletServer {

    private static final String TAG = "WalletServer";
    private static final WalletServer instance_ = new WalletServer();

    private Messenger server_;
    private IDaoProvider daoProvider_;
    private IPluginProvider pluginProvider_;
    private ICodecProvider ipcCodecProvider_;
    private IAuthComponentProvider authComponentProvider_;
    private WalletData.UserIdentity userId_;

    private WalletServer() {
    }

    private void startInstance(IDaoConfig cfg, boolean mayExist) {
        daoProvider_ = new DefaultDaoProvider(cfg);
        pluginProvider_ = new DefaultPluginProvider();
        ipcCodecProvider_ = new DefaultIpcCodecProvider();
        authComponentProvider_ = new AuthComponentProvider();

        userId_ = WalletData.UserIdentity.builder().setUserId(WalletData.ROOT_USER_ID).build();

        server_ = new PluginServerStarter()
                .setDaoProvider(daoProvider_)
                .setPluginProvider(pluginProvider_)
                .setIpcCodecProvider(ipcCodecProvider_)
                .setAuthComponentProvider(authComponentProvider_)
                .setKeyStore(cfg.getKeyStore())
                .start(mayExist);
        Log.i(TAG, "server "+server_);

    }

    public static void ensure(IDaoConfig cfg) {
        instance_.startInstance(cfg, true);
    }

    public static void start(IDaoConfig cfg) {
        instance_.startInstance(cfg, false);
    }

    public static WalletServer getInstance() {
        return instance_;
    }

    public static IPluginClient buildPluginClient() {
        return new PluginClientBuilder()
                .setUserIdentity(instance_.getCurrentUserId())
                .setServer(instance_.server())
                .setIpcCodecProvider(new DefaultIpcCodecProvider())
                .build();
    }

    public Messenger server() {
        return server_;
    }

    public WalletData.UserIdentity getCurrentUserId() {
        return userId_;
    }

    public IDaoProvider getDaoProvider() {
        return daoProvider_;
    }

    public IPluginProvider getPluginProvider () {
        return pluginProvider_;
    }

}
