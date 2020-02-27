package org.lndroid.wallet;

import androidx.paging.PagedList;

import org.lndroid.framework.usecases.ListUtxo;

public class ListUtxoViewModel extends WalletViewModelBase {

    private static final String TAG = "ListTXViewModel";

    private ListUtxo loader_;
    private ListUtxo.Pager pager_;

    public ListUtxoViewModel() {
        super(TAG);

        // create use cases
        loader_ = new ListUtxo(pluginClient());
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

    ListUtxo.Pager getPager() { return pager_; }
}

