package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.GetInvoice;

public class GetInvoiceViewModel extends WalletViewModelBase {

    private static final String TAG = "GetInvoiceViewModel";

    private GetInvoice loader_;
    private GetInvoice.Pager pager_;

    public GetInvoiceViewModel() {
        super(TAG);

        // create use cases
        loader_ = new GetInvoice(pluginClient());
        pager_ = loader_.createPager();
    }

    @Override
    protected void onCleared() {
        loader_.destroy();
        super.onCleared();
    }

    GetInvoice.Pager getPager() { return pager_; }

}



