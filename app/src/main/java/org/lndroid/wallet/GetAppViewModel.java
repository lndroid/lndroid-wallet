package org.lndroid.wallet;

import org.lndroid.framework.usecases.user.GetUser;

public class GetAppViewModel extends WalletViewModelBase {

    private static final String TAG = "GetAppViewModel";

    private GetUser loader_;
    private GetUser.Pager pager_;

    public GetAppViewModel() {
        super(TAG);

        // create use cases
        loader_ = new GetUser(pluginClient());
        pager_ = loader_.createPager();
    }

    @Override
    protected void onCleared() {
        loader_.destroy();
        super.onCleared();
    }

    GetUser.Pager getPager() { return pager_; }

}


