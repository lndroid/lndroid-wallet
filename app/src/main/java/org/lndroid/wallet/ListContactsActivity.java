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

public class ListContactsActivity extends WalletActivityBase {

    private ListContactsViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_contacts);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Contacts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        model_ = ViewModelProviders.of(this).get(ListContactsViewModel.class);
        setModel(model_);

        // set payment list request
        WalletData.ListContactsRequest req = WalletData.ListContactsRequest.builder()
                .setPage(WalletData.ListPage.builder().setCount(10).build())
                .setSort("name")
                .setNoAuth(true)
                .setEnablePaging(true)
                .build();
        model_.getPager().setRequest(req);

        // create list view adapter
        final ListContactsView.Adapter adapter = new ListContactsView.Adapter();
        adapter.setEmptyView(findViewById(R.id.notFound));

        // subscribe adapter to model list updates
        model_.getPager().pagedList().observe(this, new Observer<PagedList<WalletData.Contact>>() {
            @Override
            public void onChanged(PagedList<WalletData.Contact> p) {
                adapter.submitList(p);
            }
        });

        // set adapter to list view, init list layout
        final RecyclerView listView = findViewById(R.id.contacts);
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));

        // set click listener to open payment details
        adapter.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListContactsView.ViewHolder viewHolder =
                        (ListContactsView.ViewHolder)listView.findContainingViewHolder(view);
                WalletData.Contact p = viewHolder.data();
                if (p != null)
                    startGetContact(p.id());
            }
        });
    }

    private void startGetContact(long id) {
        Intent intent = new Intent(this, GetContactActivity.class);
        intent.putExtra(Application.ID_MESSAGE, id);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_contacts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuAddContact:
                startAddContact();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startAddContact() {
        Intent intent = new Intent(this, AddContactActivity.class);
        startActivity(intent);
    }
}
