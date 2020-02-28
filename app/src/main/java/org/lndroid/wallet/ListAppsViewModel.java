package org.lndroid.wallet;

import androidx.paging.PagedList;

import org.lndroid.framework.usecases.ListUsers;


public class ListAppsViewModel extends WalletViewModelBase {

    private static final String TAG = "ListAppsViewModel";

    private ListUsers loader_;
    private ListUsers.Pager pager_;

    public ListAppsViewModel() {
        super(TAG);

        // create use cases
        loader_ = new ListUsers(pluginClient());
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

    ListUsers.Pager getPager() { return pager_; }
}

