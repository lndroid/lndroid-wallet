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

public class ListTransactionsActivity extends WalletActivityBase {

    private ListTransactionsViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_transactions);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Transactions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        model_ = ViewModelProviders.of(this).get(ListTransactionsViewModel.class);
        setModel(model_);

        // set payment list request
        WalletData.ListTransactionsRequest req = WalletData.ListTransactionsRequest.builder()
                .setPage(WalletData.ListPage.builder().setCount(10).build())
                .setSort("id")
                .setSortDesc(true)
                .setNoAuth(true)
                .setEnablePaging(true)
                .build();
        model_.getPager().setRequest(req);

        // create list view adapter
        final ListTransactionsView.Adapter adapter = new ListTransactionsView.Adapter();
        adapter.setEmptyView(findViewById(R.id.notFound));

        // subscribe adapter to model list updates
        model_.getPager().pagedList().observe(this, new Observer<PagedList<WalletData.Transaction>>() {
            @Override
            public void onChanged(PagedList<WalletData.Transaction> p) {
                adapter.submitList(p);
            }
        });

        // set adapter to list view, init list layout
        final RecyclerView listView = findViewById(R.id.transactions);
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));

        // set click listener to open payment details
        adapter.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListTransactionsView.ViewHolder viewHolder =
                        (ListTransactionsView.ViewHolder)listView.findContainingViewHolder(view);
                WalletData.Transaction t = viewHolder.data();
                if (t != null)
                    startGetTransaction(t.id());
            }
        });
    }

    private void startGetTransaction(long id) {
        Intent intent = new Intent(this, GetTransactionActivity.class);
        intent.putExtra(Application.ID_MESSAGE, id);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_transactions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuSendCoins:
                startSendCoins();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startSendCoins() {
        Intent intent = new Intent(this, SendCoinsActivity.class);
        startActivity(intent);
    }


}
