package org.lndroid.wallet;

import androidx.paging.PagedList;

import org.lndroid.framework.usecases.ListPeers;

public class ListPeersViewModel extends WalletViewModelBase {

    private static final String TAG = "ListPeersViewModel";

    private ListPeers peerListLoader_;
    private ListPeers.Pager peerListPager_;

    public ListPeersViewModel() {
        super(TAG);

        // create use cases
        peerListLoader_ = new ListPeers(pluginClient());
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .setPageSize(10)
                .build();
        peerListPager_ = peerListLoader_.createPager(config);
    }

    @Override
    protected void onCleared() {
        peerListLoader_.destroy();
        super.onCleared();
    }

    ListPeers.Pager getPeerListPager() { return peerListPager_; }
}
