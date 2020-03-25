package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.JobSendPayment;

public class SendPaymentViewModel extends WalletViewModelBase {

    private static final String TAG = "SendPaymentViewModel";

    private boolean scanning_;
    private MutableLiveData<String> scanResult_ = new MutableLiveData<>();
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

    boolean isScanning() { return scanning_; }
    void startScan() { scanning_ = true; }
    void setScanResult(String data) { scanResult_.setValue(data); scanning_ = false; }
    LiveData<String> scanResult() { return scanResult_; }

    JobSendPayment sendPaymentJob() { return sendPaymentJob_; }
}
