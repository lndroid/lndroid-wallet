package org.lndroid.wallet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.lndroid.framework.usecases.ActionDecodePayReq;
import org.lndroid.framework.usecases.user.ActionAddContact;

public class AddContactViewModel extends WalletViewModelBase {

    private static final String TAG = "AddContactViewModel";

    private boolean scanning_;
    private MutableLiveData<String> scanResult_ = new MutableLiveData<>();
    private ActionDecodePayReq decodePayReq_;
    private ActionAddContact addContact_;

    public AddContactViewModel() {
        super(TAG);

        // create use cases
        decodePayReq_ = new ActionDecodePayReq(pluginClient());
        addContact_ = new ActionAddContact(pluginClient());
    }

    @Override
    protected void onCleared() {
        decodePayReq_.destroy();
        addContact_.destroy();
        super.onCleared();
    }

    boolean isScanning() { return scanning_; }
    void startScan() { scanning_ = true; }
    void setScanResult(String data) { scanResult_.setValue(data); scanning_ = false; }
    LiveData<String> scanResult() { return scanResult_; }

    ActionDecodePayReq decodePayReq() { return decodePayReq_; }
    ActionAddContact addContact() { return addContact_; }
}

