package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.GetInvoice;

public class GetInvoiceViewModel extends ViewModel {

    private static final String TAG = "GetInvoiceViewModel";
    private IPluginClient pluginClient_;

    private GetInvoice getInvoice_;

    public GetInvoiceViewModel() {
        super();

        pluginClient_ = WalletServer.buildPluginClient();
        Log.i(TAG, "plugin client "+pluginClient_);

        // create use cases
        getInvoice_ = new GetInvoice(pluginClient_);
    }

    @Override
    protected void onCleared() {
        getInvoice_.destroy();
    }

    void setGetInvoiceRequest(WalletData.GetRequestLong r) {
        getInvoice().setRequest(r.toBuilder().setNoAuth(true).build());
    }
    GetInvoice getInvoice() { return getInvoice_; }
}
