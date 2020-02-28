package org.lndroid.wallet;

import org.lndroid.framework.usecases.GetContact;

public class GetContactViewModel extends WalletViewModelBase {

    private static final String TAG = "GetContactViewModel";

    private GetContact loader_;
    private GetContact.Pager pager_;

    public GetContactViewModel() {
        super(TAG);

        // create use cases
        loader_ = new GetContact(pluginClient());
        pager_ = loader_.createPager();
    }

    @Override
    protected void onCleared() {
        loader_.destroy();
        super.onCleared();
    }

    GetContact.Pager getPager() { return pager_; }

}



