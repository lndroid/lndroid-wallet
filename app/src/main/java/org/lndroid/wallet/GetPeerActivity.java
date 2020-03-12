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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.common.IResponseCallback;
import org.lndroid.framework.usecases.IRequestFactory;

public class GetPeerActivity extends WalletActivityBase {

    private static final String TAG = "GetPeerActivity";
    private GetPeerViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_peer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Peer info");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        final long id = intent.getLongExtra(Application.ID_MESSAGE, 0);

        model_ = ViewModelProviders.of(this).get(GetPeerViewModel.class);
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

        model_.connectPeer().setRequestFactory(this, new IRequestFactory<WalletData.ConnectPeerRequest>() {
            @Override
            public WalletData.ConnectPeerRequest create() {
                WalletData.Peer peer = model_.getLoader().data().getValue();
                if (peer == null)
                    return null;

                // FIXME instead open 'connect' dialog w/ preset fields to
                // allow us to change 'perm' field!
                return WalletData.ConnectPeerRequest.builder()
                        .setAddress(peer.address())
                        .setPubkey(peer.pubkey())
                        .setPerm(peer.perm())
                        .build();
            }
        });
        model_.connectPeer().setCallback(this, new IResponseCallback<WalletData.Peer>() {
            @Override
            public void onResponse(WalletData.Peer peer) {
                Log.i(TAG, "connecting peer "+peer);
                Toast.makeText(GetPeerActivity.this, "Connecting peer", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String s, String s1) {
                Log.e(TAG, "Failed to connect peer "+s+" err "+s1);
                Toast.makeText(GetPeerActivity.this, "Error: "+s1, Toast.LENGTH_LONG).show();
            }
        });


        model_.disconnectPeer().setRequestFactory(this, new IRequestFactory<WalletData.DisconnectPeerRequest>() {
            @Override
            public WalletData.DisconnectPeerRequest create() {
                return WalletData.DisconnectPeerRequest.builder()
                        .setId(id)
                        .build();
            }
        });
        model_.disconnectPeer().setCallback(this, new IResponseCallback<WalletData.Peer>() {
            @Override
            public void onResponse(WalletData.Peer peer) {
                Log.i(TAG, "disconnecting peer "+peer);
                Toast.makeText(GetPeerActivity.this, "Disconnecting peer", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String s, String s1) {
                Log.e(TAG, "Failed to disconnect peer "+s+" err "+s1);
                Toast.makeText(GetPeerActivity.this, "Error: "+s1, Toast.LENGTH_LONG).show();
            }
        });

        recoverUseCases();
    }

    private void recoverUseCases() {
        if (model_.connectPeer().isExecuting())
            connectPeer();
        if (model_.disconnectPeer().isExecuting())
            disconnectPeer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_get_peer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuConnectPeer:
                connectPeer();
                return true;
            case R.id.menuDisconnectPeer:
                disconnectPeer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void connectPeer() {
        if (model_.connectPeer().isExecuting())
            model_.connectPeer().recover();
        else
            model_.connectPeer().execute("");
    }

    private void disconnectPeer() {
        if (model_.disconnectPeer().isExecuting())
            model_.disconnectPeer().recover();
        else
            model_.disconnectPeer().execute("");
    }

}
