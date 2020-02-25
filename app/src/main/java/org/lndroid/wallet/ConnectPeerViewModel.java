package org.lndroid.wallet;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.ActionConnectPeer;

public class ConnectPeerViewModel extends WalletViewModelBase {

    private static final String TAG = "ConnectPeerViewModel";

    private ActionConnectPeer connectPeer_;

    public ConnectPeerViewModel() {
        super(TAG);

        // create use cases
        connectPeer_ = new ActionConnectPeer(pluginClient());
    }

    @Override
    protected void onCleared() {
        connectPeer_.destroy();
        super.onCleared();
    }

    ActionConnectPeer connectPeer() { return connectPeer_; }
}
