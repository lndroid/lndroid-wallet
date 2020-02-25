package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.ActionNewAddress;

public class NewAddressViewModel extends WalletViewModelBase {

    private static final String TAG = "NewAddressViewModel";

    private ActionNewAddress newAddress_;

    public NewAddressViewModel() {
        super(TAG);

        // create use cases
        newAddress_ = new ActionNewAddress(pluginClient());
    }

    @Override
    protected void onCleared() {
        newAddress_.destroy();
        super.onCleared();
    }

    ActionNewAddress newAddress() { return newAddress_; }
}

