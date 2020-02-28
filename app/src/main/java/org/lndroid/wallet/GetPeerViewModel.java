package org.lndroid.wallet;

import androidx.paging.PagedList;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.usecases.GetPeer;

import java.util.ArrayList;
import java.util.List;

public class GetPeerViewModel extends WalletViewModelBase {

    private static final String TAG = "GetPeerViewModel";

    private GetPeer loader_;
    private GetPeer.Pager pager_;

    public GetPeerViewModel() {
        super(TAG);

        // create use cases
        loader_ = new GetPeer(pluginClient());
        pager_ = loader_.createPager();
    }

    @Override
    protected void onCleared() {
        loader_.destroy();
        super.onCleared();
    }

    GetPeer.Pager getPager() { return pager_; }

}

