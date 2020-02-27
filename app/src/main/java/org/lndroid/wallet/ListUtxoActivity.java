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

public class ListUtxoActivity extends WalletActivityBase {

    private ListUtxoViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_utxo);

        model_ = ViewModelProviders.of(this).get(ListUtxoViewModel.class);
        setModel(model_);

        // set payment list request
        WalletData.ListUtxoRequest req = WalletData.ListUtxoRequest.builder()
                .setPage(WalletData.ListPage.builder().setCount(10).build())
                .setSort("id")
                .setNoAuth(true)
                .setEnablePaging(true)
                .build();
        model_.getPager().setRequest(req);

        // create list view adapter
        final ListUtxoView.Adapter adapter = new ListUtxoView.Adapter();

        // subscribe adapter to model list updates
        model_.getPager().pagedList().observe(this, new Observer<PagedList<WalletData.Utxo>>() {
            @Override
            public void onChanged(PagedList<WalletData.Utxo> p) {
                adapter.submitList(p);
            }
        });

        // set adapter to list view, init list layout
        final RecyclerView listView = findViewById(R.id.utxo);
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));

        // set click listener to open payment details
        adapter.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListUtxoView.ViewHolder viewHolder =
                        (ListUtxoView.ViewHolder)listView.findContainingViewHolder(view);
                WalletData.Utxo t = viewHolder.data();
                if (t != null)
                    startGetUtxo(t.id());
            }
        });
    }

    private void startGetUtxo(long id) {
        Intent intent = new Intent(this, GetUtxoActivity.class);
        intent.putExtra(Application.ID_MESSAGE, id);
        startActivity(intent);
    }
}
