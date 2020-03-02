package org.lndroid.wallet;

import org.lndroid.framework.usecases.user.GetWalletInfo;

public class GetWalletInfoViewModel extends WalletViewModelBase {

    private static final String TAG = "GetWalletInfoViewModel";

    private GetWalletInfo loader_;
    private GetWalletInfo.Pager pager_;

    public GetWalletInfoViewModel() {
        super(TAG);

        // create use cases
        loader_ = new GetWalletInfo(pluginClient());
        pager_ = loader_.createPager();
    }

    @Override
    protected void onCleared() {
        loader_.destroy();
        super.onCleared();
    }

    GetWalletInfo.Pager getPager() { return pager_; }

}


