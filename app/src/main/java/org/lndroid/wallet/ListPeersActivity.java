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

public class ListPeersActivity extends WalletActivityBase {

    private ListPeersViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_peers);

        model_ = ViewModelProviders.of(this).get(ListPeersViewModel.class);
        setModel(model_);

        // set payment list request
        WalletData.ListPeersRequest req = WalletData.ListPeersRequest.builder()
                .setPage(WalletData.ListPage.builder().setCount(10).build())
                .setSort("pubkey")
                .setNoAuth(true)
                .setEnablePaging(true)
                .build();
        model_.getPeerListPager().setRequest(req);

        // create list view adapter
        final ListPeersView.Adapter adapter = new ListPeersView.Adapter();

        // subscribe adapter to model list updates
        model_.getPeerListPager().pagedList().observe(this, new Observer<PagedList<WalletData.Peer>>() {
            @Override
            public void onChanged(PagedList<WalletData.Peer> p) {
                adapter.submitList(p);
            }
        });

        // set adapter to list view, init list layout
        final RecyclerView listView = findViewById(R.id.peers);
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));

        // set click listener to open payment details
        adapter.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListPeersView.ViewHolder viewHolder =
                        (ListPeersView.ViewHolder)listView.findContainingViewHolder(view);
                WalletData.Peer p = viewHolder.peer();
                if (p != null)
                    startGetPeer(p.id());
            }
        });
    }

    private void startGetPeer(long id) {
        Intent intent = new Intent(this, GetPeerActivity.class);
        intent.putExtra(Application.ID_MESSAGE, id);
        startActivity(intent);
    }
}
