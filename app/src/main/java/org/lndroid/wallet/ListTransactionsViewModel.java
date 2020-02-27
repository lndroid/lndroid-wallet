package org.lndroid.wallet;

import androidx.paging.PagedList;

import org.lndroid.framework.usecases.ListTransactions;

public class ListTransactionsViewModel extends WalletViewModelBase {

    private static final String TAG = "ListTXViewModel";

    private ListTransactions loader_;
    private ListTransactions.Pager pager_;

    public ListTransactionsViewModel() {
        super(TAG);

        // create use cases
        loader_ = new ListTransactions(pluginClient());
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

    ListTransactions.Pager getPager() { return pager_; }
}
