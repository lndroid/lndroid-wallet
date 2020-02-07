package org.lndroid.wallet;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.ActionConnectPeer;

public class ConnectPeerViewModel extends ViewModel {

    private static final String TAG = "ConnectPeerViewModel";
    private IPluginClient pluginClient_;

    private ActionConnectPeer connectPeer_;

    public ConnectPeerViewModel() {
        super();

        pluginClient_ = WalletServer.buildPluginClient();

        // create use cases
        connectPeer_ = new ActionConnectPeer(pluginClient_);
    }

    @Override
    protected void onCleared() {
        connectPeer_.destroy();
    }

    ActionConnectPeer connectPeer() { return connectPeer_; }
}
