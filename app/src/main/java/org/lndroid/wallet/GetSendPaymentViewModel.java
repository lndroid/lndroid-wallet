package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.GetSendPayment;

public class GetSendPaymentViewModel extends WalletViewModelBase {

    private static final String TAG = "GetSendPaymentViewModel";


    private GetSendPayment loader_;
    private GetSendPayment.Pager pager_;

    public GetSendPaymentViewModel() {
        super(TAG);

        // create use cases
        loader_ = new GetSendPayment(pluginClient());
        pager_ = loader_.createPager();
    }

    @Override
    protected void onCleared() {
        loader_.destroy();
        super.onCleared();
    }

    GetSendPayment.Pager getPager() { return pager_; }

}

