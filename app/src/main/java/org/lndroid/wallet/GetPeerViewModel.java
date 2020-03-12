package org.lndroid.wallet;

import androidx.paging.PagedList;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.usecases.ActionConnectPeer;
import org.lndroid.framework.usecases.ActionDisconnectPeer;
import org.lndroid.framework.usecases.GetPeer;

import java.util.ArrayList;
import java.util.List;

public class GetPeerViewModel extends WalletViewModelBase {

    private static final String TAG = "GetPeerViewModel";

    private GetPeer loader_;
    private GetPeer.Pager pager_;
    private ActionConnectPeer connectPeer_;
    private ActionDisconnectPeer disconnectPeer_;

    public GetPeerViewModel() {
        super(TAG);

        // create use cases
        loader_ = new GetPeer(pluginClient());
        pager_ = loader_.createPager();

        connectPeer_ = new ActionConnectPeer(pluginClient());
        disconnectPeer_ = new ActionDisconnectPeer(pluginClient());
    }

    @Override
    protected void onCleared() {
        loader_.destroy();
        connectPeer_.destroy();
        disconnectPeer_.destroy();
        super.onCleared();
    }

    GetPeer getLoader() { return loader_; }
    GetPeer.Pager getPager() { return pager_; }
    ActionDisconnectPeer disconnectPeer() { return disconnectPeer_; }
    ActionConnectPeer connectPeer() { return connectPeer_; }
}

