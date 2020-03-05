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

public class ListChannelsActivity extends WalletActivityBase {

    private ListChannelsViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_channels);

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
}
