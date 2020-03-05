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

public class ListInvoicesActivity extends WalletActivityBase {

    private ListInvoicesViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_invoices);

        model_ = ViewModelProviders.of(this).get(ListInvoicesViewModel.class);
        setModel(model_);

        // set payment list request
        WalletData.ListInvoicesRequest req = WalletData.ListInvoicesRequest.builder()
                .setPage(WalletData.ListPage.builder().setCount(10).build())
                .setSort("id")
                .setSortDesc(true)
                .setNoAuth(true)
                .setEnablePaging(true)
                .build();
        model_.getPager().setRequest(req);

        // create list view adapter
        final ListInvoicesView.Adapter adapter = new ListInvoicesView.Adapter();

        // subscribe adapter to model list updates
        model_.getPager().pagedList().observe(this, new Observer<PagedList<WalletData.Invoice>>() {
            @Override
            public void onChanged(PagedList<WalletData.Invoice> p) {
                adapter.submitList(p);
            }
        });

        // set adapter to list view, init list layout
        final RecyclerView listView = findViewById(R.id.invoices);
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));

        // set click listener to open payment details
        adapter.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListInvoicesView.ViewHolder viewHolder =
                        (ListInvoicesView.ViewHolder)listView.findContainingViewHolder(view);
                WalletData.Invoice p = viewHolder.data();
                if (p != null)
                    startGetInvoice(p.id());
            }
        });
    }

    private void startGetInvoice(long id) {
        Intent intent = new Intent(this, GetInvoiceActivity.class);
        intent.putExtra(Application.ID_MESSAGE, id);
        startActivity(intent);
    }
}
