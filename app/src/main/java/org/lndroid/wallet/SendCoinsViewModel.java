package org.lndroid.wallet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.lndroid.framework.usecases.ActionEstimateFee;
import org.lndroid.framework.usecases.JobSendCoins;

public class SendCoinsViewModel extends WalletViewModelBase {

    private static final String TAG = "SendCoinsViewModel";

    private JobSendCoins sendCoins_;
    private ActionEstimateFee estimateFee_;
    private boolean scanning_;
    private MutableLiveData<String> scanResult_ = new MutableLiveData<>();

    public SendCoinsViewModel() {
        super(TAG);

        // create use cases
        sendCoins_ = new JobSendCoins(pluginClient());
    }

    @Override
    protected void onCleared() {
        sendCoins_.destroy();
        estimateFee_.destroy();
        super.onCleared();

    }
    boolean isScanning() { return scanning_; }
    void startScan() { scanning_ = true; }
    void setScanResult(String data) { scanResult_.setValue(data); scanning_ = false; }
    LiveData<String> scanResult() { return scanResult_; }

    JobSendCoins sendCoins() { return sendCoins_; }
    ActionEstimateFee estimateFee() { return estimateFee_; }
}

