package org.lndroid.wallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.lndroid.framework.common.IResponseCallback;
import org.lndroid.framework.WalletData;
import org.lndroid.framework.usecases.IRequestFactory;

public class ConnectPeerActivity extends WalletActivityBase {
    private static final String TAG = "ConnectPeerActivity";

    private EditText pubkey_;
    private EditText host_;
    private TextView state_;
    private ConnectPeerViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_peer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Connect to peer");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        model_ = ViewModelProviders.of(this).get(ConnectPeerViewModel.class);
        setModel(model_);

        pubkey_ = findViewById(R.id.pubkey);
        host_ = findViewById(R.id.host);
        state_ = findViewById(R.id.state);

        Button button = findViewById(R.id.connectPeer);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectPeer();
            }
        });

        model_.connectPeer().setCallback(this, new IResponseCallback<WalletData.Peer>() {
            @Override
            public void onResponse(WalletData.Peer r) {
                Log.i(TAG, "connecting "+r);
                state_.setText("Request accepted.");
                startGetPeer(r.id());
                finish();
            }

            @Override
            public void onError(String code, String e) {
                state_.setText("Error: "+code+": "+e);
                Log.e(TAG, "connect error "+code+" msg "+e);
            }
        });
        model_.connectPeer().setRequestFactory(this, new IRequestFactory<WalletData.ConnectPeerRequest>() {
            @Override
            public WalletData.ConnectPeerRequest create() {
                return WalletData.ConnectPeerRequest.builder()
                        .setPubkey(pubkey_.getText().toString())
                        .setAddress(host_.getText().toString())
                        .setPerm(true)
                        .build();
            }
        });
        // FIXME actionAddInvoice.setAuthCallback();

        recoverUseCases();
    }

    private void recoverUseCases() {
        if (model_.connectPeer().isExecuting())
            connectPeer();
    }

    private void connectPeer() {
        state_.setText("Connecting...");
        if (model_.connectPeer().isExecuting())
            model_.connectPeer().recover();
        else
            model_.connectPeer().execute("");
    }

    private void startGetPeer(long id) {
        Intent intent = new Intent(this, GetPeerActivity.class);
        intent.putExtra(Application.ID_MESSAGE, id);
        startActivity(intent);
    }

}
