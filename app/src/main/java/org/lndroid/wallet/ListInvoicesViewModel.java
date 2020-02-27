package org.lndroid.wallet;

import androidx.paging.PagedList;

import org.lndroid.framework.usecases.ListInvoices;

public class ListInvoicesViewModel extends WalletViewModelBase {

    private static final String TAG = "ListInvoicesViewModel";

    private ListInvoices loader_;
    private ListInvoices.Pager pager_;

    public ListInvoicesViewModel() {
        super(TAG);

        // create use cases
        loader_ = new ListInvoices(pluginClient());
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

    ListInvoices.Pager getPager() { return pager_; }
}

