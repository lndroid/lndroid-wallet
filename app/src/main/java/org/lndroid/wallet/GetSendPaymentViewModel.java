package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.GetSendPayment;

public class GetSendPaymentViewModel extends WalletViewModelBase {

    private static final String TAG = "GetSendPaymentViewModel";

    private GetSendPayment getSendPayment_;

    public GetSendPaymentViewModel() {
        super(TAG);

        // create use cases
        getSendPayment_ = new GetSendPayment(pluginClient());
    }

    @Override
    protected void onCleared() {
        getSendPayment_.destroy();
        super.onCleared();
    }

    void setSendPaymentRequest(WalletData.GetRequestLong r) {
        getSendPayment().setRequest(r.toBuilder().setNoAuth(true).build());
    }
    GetSendPayment getSendPayment() { return getSendPayment_; }
}
