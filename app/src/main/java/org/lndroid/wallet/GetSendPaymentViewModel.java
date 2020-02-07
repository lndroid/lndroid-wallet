package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.GetSendPayment;

public class GetSendPaymentViewModel extends ViewModel {

    private static final String TAG = "GetSendPaymentViewModel";
    private IPluginClient pluginClient_;

    private GetSendPayment getSendPayment_;

    public GetSendPaymentViewModel() {
        super();

        pluginClient_ = WalletServer.buildPluginClient();
        Log.i(TAG, "plugin client "+pluginClient_);

        // create use cases
        getSendPayment_ = new GetSendPayment(pluginClient_);
    }

    @Override
    protected void onCleared() {
        getSendPayment_.destroy();
    }

    void setSendPaymentRequest(WalletData.GetRequestLong r) {
        getSendPayment().setRequest(r.toBuilder().setNoAuth(true).build());
    }
    GetSendPayment getSendPayment() { return getSendPayment_; }
}
