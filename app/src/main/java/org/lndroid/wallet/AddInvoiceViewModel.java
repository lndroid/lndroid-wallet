package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.ActionAddInvoice;

public class AddInvoiceViewModel extends ViewModel {

    private static final String TAG = "AddInvoiceViewModel";
    private IPluginClient pluginClient_;

    private ActionAddInvoice addInvoiceAction_;

    public AddInvoiceViewModel() {
        super();

        pluginClient_ = WalletServer.buildPluginClient();
        Log.i(TAG, "plugin client "+pluginClient_);

        // create use cases
        addInvoiceAction_ = new ActionAddInvoice(pluginClient_);
    }

    @Override
    protected void onCleared() {
        addInvoiceAction_.destroy();
    }

    ActionAddInvoice addInvoiceAction() { return addInvoiceAction_; }
}
