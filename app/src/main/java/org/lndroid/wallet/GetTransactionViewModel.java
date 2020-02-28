package org.lndroid.wallet;

import org.lndroid.framework.usecases.GetTransaction;

public class GetTransactionViewModel extends WalletViewModelBase {

    private static final String TAG = "GetTransactionViewModel";

    private GetTransaction loader_;
    private GetTransaction.Pager pager_;

    public GetTransactionViewModel() {
        super(TAG);

        // create use cases
        loader_ = new GetTransaction(pluginClient());
        pager_ = loader_.createPager();
    }

    @Override
    protected void onCleared() {
        loader_.destroy();
        super.onCleared();
    }

    GetTransaction.Pager getPager() { return pager_; }

}

