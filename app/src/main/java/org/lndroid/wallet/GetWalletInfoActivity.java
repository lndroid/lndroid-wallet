package org.lndroid.wallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import org.lndroid.framework.WalletData;

public class GetWalletInfoActivity extends WalletActivityBase {

    private GetWalletInfoViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_wallet_info);

        model_ = ViewModelProviders.of(this).get(GetWalletInfoViewModel.class);
        setModel(model_);

        WalletData.GetRequestLong r = WalletData.GetRequestLong.builder()
                .setSubscribe(true)
                .setNoAuth(true)
                .build();

        model_.getPager().setRequest(r);

        // create list view adapter
        final ListFieldsView.Adapter adapter = new ListFieldsView.Adapter();

        // subscribe adapter to model list updates
        model_.getPager().pagedList().observe(this, new Observer<PagedList<WalletData.Field>>() {
            @Override
            public void onChanged(PagedList<WalletData.Field> fields) {
                adapter.submitList(fields);
            }
        });

        // set adapter to list view, init list layout
        final RecyclerView listView = findViewById(R.id.fields);
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));
    }
}

