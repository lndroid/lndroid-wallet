package org.lndroid.wallet;

import androidx.appcompat.app.AppCompatActivity;
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

public class ListInvoicesActivity extends WalletActivityBase {

    private ListInvoicesViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_invoices);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Invoices");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        model_ = ViewModelProviders.of(this).get(ListInvoicesViewModel.class);
        setModel(model_);

        // set payment list request
        WalletData.ListInvoicesRequest req = WalletData.ListInvoicesRequest.builder()
                .setPage(WalletData.ListPage.builder().setCount(10).build())
                .setSort("id")
                .setSortDesc(true)
                .setNoKeysend(true)
                .setNoAuth(true)
                .setEnablePaging(true)
                .build();
        model_.getPager().setRequest(req);

        // create list view adapter
        final ListInvoicesView.Adapter adapter = new ListInvoicesView.Adapter();
        adapter.setEmptyView(findViewById(R.id.notFound));

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_invoices, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuAddInvoice:
                startAddInvoice();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startAddInvoice() {
        Intent intent = new Intent(this, AddInvoiceActivity.class);
        startActivity(intent);
    }

}
