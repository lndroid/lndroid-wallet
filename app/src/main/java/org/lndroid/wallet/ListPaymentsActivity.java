package org.lndroid.wallet;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.lndroid.framework.WalletData;

public class ListPaymentsActivity extends WalletActivityBase {

    private ListPaymentsViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_payments);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Payments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        model_ = ViewModelProviders.of(this).get(ListPaymentsViewModel.class);
        setModel(model_);

        // set payment list request
        WalletData.ListPaymentsRequest listPaymentsReq = WalletData.ListPaymentsRequest.builder()
                .setPage(WalletData.ListPage.builder().setCount(10).build())
                .setSort("id")
                .setSortDesc(true)
                .setNoAuth(true)
                .setEnablePaging(true)
                .build();
        model_.getPaymentListPager().setRequest(listPaymentsReq);

        // create list view adapter
        final ListPaymentsView.Adapter adapter = new ListPaymentsView.Adapter();
        adapter.setEmptyView(findViewById(R.id.notFound));

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
                ListPaymentsView.ViewHolder viewHolder =
                        (ListPaymentsView.ViewHolder)listView.findContainingViewHolder(view);
                WalletData.Payment p = viewHolder.data();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_payments, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuSendPayment:
                startSendPayment();
                return true;
            case R.id.menuAddInvoice:
                startAddInvoice();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startSendPayment() {
        Intent intent = new Intent(this, SendPaymentActivity.class);
        startActivity(intent);
    }

    private void startAddInvoice() {
        Intent intent = new Intent(this, AddInvoiceActivity.class);
        startActivity(intent);
    }

}
