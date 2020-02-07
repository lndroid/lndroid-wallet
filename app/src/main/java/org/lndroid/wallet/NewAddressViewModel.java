package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.ActionNewAddress;

public class NewAddressViewModel extends ViewModel {

    private static final String TAG = "NewAddressViewModel";
    private IPluginClient pluginClient_;

    private ActionNewAddress newAddress_;

    public NewAddressViewModel() {
        super();

        pluginClient_ = WalletServer.buildPluginClient();
        Log.i(TAG, "plugin client "+pluginClient_);

        // create use cases
        newAddress_ = new ActionNewAddress(pluginClient_);
    }

    @Override
    protected void onCleared() {
        newAddress_.destroy();
    }

    ActionNewAddress newAddress() { return newAddress_; }
}

