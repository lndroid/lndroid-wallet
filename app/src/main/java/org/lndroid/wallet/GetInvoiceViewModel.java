package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.GetInvoice;

public class GetInvoiceViewModel extends WalletViewModelBase {

    private static final String TAG = "GetInvoiceViewModel";

    private GetInvoice getInvoice_;

    public GetInvoiceViewModel() {
        super(TAG);

        // create use cases
        getInvoice_ = new GetInvoice(pluginClient());
    }

    @Override
    protected void onCleared() {
        getInvoice_.destroy();
        super.onCleared();
    }

    void setGetInvoiceRequest(WalletData.GetRequestLong r) {
        getInvoice().setRequest(r.toBuilder().setNoAuth(true).build());
    }
    GetInvoice getInvoice() { return getInvoice_; }
}
