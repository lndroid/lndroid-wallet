package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.JobSendPayment;

public class SendPaymentViewModel extends ViewModel {

    private static final String TAG = "SendPaymentViewModel";
    private IPluginClient pluginClient_;

    private JobSendPayment sendPaymentJob_;

    public SendPaymentViewModel() {
        super();

        pluginClient_ = WalletServer.buildPluginClient();
        Log.i(TAG, "plugin client "+pluginClient_);

        // create use cases
        sendPaymentJob_ = new JobSendPayment(pluginClient_);
    }

    @Override
    protected void onCleared() {
        sendPaymentJob_.destroy();
    }

    JobSendPayment sendPaymentJob() { return sendPaymentJob_; }
}
