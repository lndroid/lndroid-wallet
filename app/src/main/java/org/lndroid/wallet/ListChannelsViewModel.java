package org.lndroid.wallet;

import androidx.paging.PagedList;

import org.lndroid.framework.usecases.ListChannels;

public class ListChannelsViewModel extends WalletViewModelBase {

    private static final String TAG = "ListChannelsViewModel";

    private ListChannels loader_;
    private ListChannels.Pager pager_;

    public ListChannelsViewModel() {
        super(TAG);

        // create use cases
        loader_ = new ListChannels(pluginClient());
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .setPageSize(10)
                .build();
        pager_ = loader_.createPager(config);
    }

    @Override
    protected void onCleared() {
        loader_.destroy();
        super.onCleared();
    }

    ListChannels.Pager getPager() { return pager_; }
}

