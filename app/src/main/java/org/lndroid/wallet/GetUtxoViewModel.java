package org.lndroid.wallet;

import org.lndroid.framework.usecases.GetUtxo;

public class GetUtxoViewModel extends WalletViewModelBase {

    private static final String TAG = "GetUtxoViewModel";

    private GetUtxo loader_;
    private GetUtxo.Pager pager_;

    public GetUtxoViewModel() {
        super(TAG);

        // create use cases
        loader_ = new GetUtxo(pluginClient());
        pager_ = loader_.createPager();
    }

    @Override
    protected void onCleared() {
        loader_.destroy();
        super.onCleared();
    }

    GetUtxo.Pager getPager() { return pager_; }

}

