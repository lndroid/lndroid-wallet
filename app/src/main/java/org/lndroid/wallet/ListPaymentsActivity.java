package org.lndroid.wallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.lndroid.framework.WalletData;

public class ListPaymentsActivity extends AppCompatActivity {

    private ListPaymentsViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_payments);

        model_ = ViewModelProviders.of(this).get(ListPaymentsViewModel.class);
        model_.getSessionToken(getApplicationContext());

        // set payment list request
        WalletData.ListPaymentsRequest listPaymentsReq = WalletData.ListPaymentsRequest.builder()
                .setPage(WalletData.ListPage.builder().setCount(10).build())
                .setSort("time")
                .setSortDesc(true)
                .setNoAuth(true)
                .setEnablePaging(true)
                .build();
        model_.getPaymentListPager().setRequest(listPaymentsReq);

        // create list view adapter
        final PaymentListView.PaymentListAdapter adapter = new PaymentListView.PaymentListAdapter();

        // subscribe adapter to model list updates
        model_.getPaymentListPager().pagedList().observe(this, new Observer<PagedList<WalletData.Payment>>() {
            @Override
            public void onChanged(PagedList<WalletData.Payment> payments) {
                adapter.submitList(payments);
            }
        });

        // set adapter to list view, init list layout
        final RecyclerView listView = findViewById(R.id.payments);
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));

        // set click listener to open payment details
        adapter.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PaymentListView.PaymentListViewHolder viewHolder =
                        (PaymentListView.PaymentListViewHolder)listView.findContainingViewHolder(view);
                WalletData.Payment p = viewHolder.boundPayment();
                if (p == null)
                    return;

                switch(p.type()) {
                    case WalletData.PAYMENT_TYPE_INVOICE:
                        startGetInvoice(p.sourceId());
                        break;
                    case WalletData.PAYMENT_TYPE_SENDPAYMENT:
                        startGetPayment(p.sourceId());
                        break;
                    default:
                        throw new RuntimeException("Unknown payment type");
                }
            }
        });
    }

    private void startGetInvoice(long id) {
        Intent intent = new Intent(this, GetInvoiceActivity.class);
        intent.putExtra(Application.ID_MESSAGE, id);
        startActivity(intent);
    }

    private void startGetPayment(long id) {
        Intent intent = new Intent(this, GetSendPaymentActivity.class);
        intent.putExtra(Application.ID_MESSAGE, id);
        startActivity(intent);
    }
}
