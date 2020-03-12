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

import org.lndroid.framework.WalletData;

public class GetTransactionActivity extends WalletActivityBase {

    private GetTransactionViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_transaction);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Transaction info");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        final long id = intent.getLongExtra(Application.ID_MESSAGE, 0);

        model_ = ViewModelProviders.of(this).get(GetTransactionViewModel.class);
        setModel(model_);

        WalletData.GetRequestLong r = WalletData.GetRequestLong.builder()
                .setSubscribe(true)
                .setNoAuth(true)
                .setId(id)
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
