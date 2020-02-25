package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.ActionAddInvoice;

public class AddInvoiceViewModel extends WalletViewModelBase {

    private static final String TAG = "AddInvoiceViewModel";

    private ActionAddInvoice addInvoiceAction_;

    public AddInvoiceViewModel() {
        super(TAG);

        // create use cases
        addInvoiceAction_ = new ActionAddInvoice(pluginClient());
    }

    @Override
    protected void onCleared() {
        addInvoiceAction_.destroy();
        super.onCleared();
    }

    ActionAddInvoice addInvoiceAction() { return addInvoiceAction_; }
}
