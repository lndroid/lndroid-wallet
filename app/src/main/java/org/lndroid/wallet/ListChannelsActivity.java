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

public class ListChannelsActivity extends WalletActivityBase {

    private ListChannelsViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_channels);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Channels");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        model_ = ViewModelProviders.of(this).get(ListChannelsViewModel.class);
        setModel(model_);

        // set payment list request
        WalletData.ListChannelsRequest req = WalletData.ListChannelsRequest.builder()
                .setPage(WalletData.ListPage.builder().setCount(10).build())
                .setSort("active")
                .setSortDesc(true)
                .setNoAuth(true)
                .setEnablePaging(true)
                .build();
        model_.getPager().setRequest(req);

        // create list view adapter
        final ListChannelsView.Adapter adapter = new ListChannelsView.Adapter();
        adapter.setEmptyView(findViewById(R.id.notFound));

        // subscribe adapter to model list updates
        model_.getPager().pagedList().observe(this, new Observer<PagedList<WalletData.Channel>>() {
            @Override
            public void onChanged(PagedList<WalletData.Channel> p) {
                adapter.submitList(p);
            }
        });

        // set adapter to list view, init list layout
        final RecyclerView listView = findViewById(R.id.channels);
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));

        // set click listener to open payment details
        adapter.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListChannelsView.ViewHolder viewHolder =
                        (ListChannelsView.ViewHolder)listView.findContainingViewHolder(view);
                WalletData.Channel p = viewHolder.data();
                if (p != null)
                    startGetChannel(p.id());
            }
        });
    }

    private void startGetChannel(long id) {
        Intent intent = new Intent(this, GetChannelActivity.class);
        intent.putExtra(Application.ID_MESSAGE, id);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_channels, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuOpenChannel:
                startOpenChannel();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startOpenChannel() {
        Intent intent = new Intent(this, OpenChannelActivity.class);
        startActivity(intent);
    }
}
