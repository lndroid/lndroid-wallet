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
import android.view.View;

import org.lndroid.framework.WalletData;

public class ListAppsActivity extends WalletActivityBase {

    private ListAppsViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_apps);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Applications");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        model_ = ViewModelProviders.of(this).get(ListAppsViewModel.class);
        setModel(model_);

        // set payment list request
        WalletData.ListUsersRequest req = WalletData.ListUsersRequest.builder()
                .setPage(WalletData.ListPage.builder().setCount(10).build())
                .setRole(WalletData.USER_ROLE_APP)
                .setSort("name")
                .setNoAuth(true)
                .setEnablePaging(true)
                .build();
        model_.getPager().setRequest(req);

        // create list view adapter
        final ListAppsView.Adapter adapter = new ListAppsView.Adapter();
        adapter.setEmptyView(findViewById(R.id.notFound));

        // subscribe adapter to model list updates
        model_.getPager().pagedList().observe(this, new Observer<PagedList<WalletData.User>>() {
            @Override
            public void onChanged(PagedList<WalletData.User> p) {
                adapter.submitList(p);
            }
        });

        // set adapter to list view, init list layout
        final RecyclerView listView = findViewById(R.id.apps);
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));

        // set click listener to open payment details
        adapter.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListAppsView.ViewHolder viewHolder =
                        (ListAppsView.ViewHolder)listView.findContainingViewHolder(view);
                WalletData.User p = viewHolder.data();
                if (p != null)
                    startGetApp(p.id());
            }
        });
    }

    private void startGetApp(long id) {
        Intent intent = new Intent(this, GetAppActivity.class);
        intent.putExtra(Application.ID_MESSAGE, id);
        startActivity(intent);
    }
}
