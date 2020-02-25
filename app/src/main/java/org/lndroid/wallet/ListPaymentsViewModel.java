package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.ListPayments;

public class ListPaymentsViewModel extends WalletViewModelBase {

    private static final String TAG = "ListPaymentsViewModel";

    private ListPayments paymentListLoader_;
    private ListPayments.Pager paymentListPager_;

    public ListPaymentsViewModel() {
        super(TAG);

        // create use cases
        paymentListLoader_ = new ListPayments(pluginClient());
        PagedList.Config paymentListConfig = new PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .setPageSize(10)
                .build();
        paymentListPager_ = paymentListLoader_.createPager(paymentListConfig);
    }

    @Override
    protected void onCleared() {
        paymentListLoader_.destroy();
        super.onCleared();
    }

    ListPayments.Pager getPaymentListPager() { return paymentListPager_; }
}
