package org.lndroid.wallet;

import androidx.paging.PagedList;

import org.lndroid.framework.usecases.ListContacts;

public class ListContactsViewModel extends WalletViewModelBase {

    private static final String TAG = "ListContactsViewModel";

    private ListContacts loader_;
    private ListContacts.Pager pager_;

    public ListContactsViewModel() {
        super(TAG);

        // create use cases
        loader_ = new ListContacts(pluginClient());
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

    ListContacts.Pager getPager() { return pager_; }
}


