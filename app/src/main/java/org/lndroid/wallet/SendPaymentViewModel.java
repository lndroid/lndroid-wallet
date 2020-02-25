package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.JobSendPayment;

public class SendPaymentViewModel extends WalletViewModelBase {

    private static final String TAG = "SendPaymentViewModel";

    private JobSendPayment sendPaymentJob_;

    public SendPaymentViewModel() {
        super(TAG);

        // create use cases
        sendPaymentJob_ = new JobSendPayment(pluginClient());
    }

    @Override
    protected void onCleared() {
        sendPaymentJob_.destroy();
        super.onCleared();
    }

    JobSendPayment sendPaymentJob() { return sendPaymentJob_; }
}
