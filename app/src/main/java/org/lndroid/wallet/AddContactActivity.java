package org.lndroid.wallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.common.IResponseCallback;
import org.lndroid.framework.usecases.IRequestFactory;

public class AddContactActivity extends WalletActivityBase {

    private static final String TAG = "AddContactActivity";

    private EditText payReq_;
    private EditText pubkey_;
    private EditText name_;
    private EditText description_;
    private EditText url_;
    private TextView state_;
    private AddContactViewModel model_;
    private boolean rejected_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Contact");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        model_ = ViewModelProviders.of(this).get(AddContactViewModel.class);
        setModel(model_);

        Button confirm = findViewById(R.id.confirm);
        Button cancel = findViewById(R.id.cancel);
        Button scan = findViewById(R.id.scan);
        payReq_ = findViewById(R.id.payreq);
        pubkey_ = findViewById(R.id.pubkey);
        name_ = findViewById(R.id.name);
        description_ = findViewById(R.id.description);
        url_ = findViewById(R.id.url);
        state_ = findViewById(R.id.state);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (payReq_.getText().toString().isEmpty())
                    addContact();
                else
                    decodePayReq();
            }
        });
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan();
            }
        });

        model_.scanResult().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s != null && !s.isEmpty()) {
                    payReq_.setText(s);
                }
            }
        });

        model_.decodePayReq().setCallback(this, new IResponseCallback<WalletData.SendPayment>() {
            @Override
            public void onResponse(WalletData.SendPayment r) {
                Log.i(TAG, "invoice " + r);
                addContact();
            }

            @Override
            public void onError(String code, String e) {
                state_.setText("Error: " + e);
                Log.e(TAG, "decode payreq error " + code + " msg " + e);
            }
        });
        model_.decodePayReq().setRequestFactory(this, new IRequestFactory<String>() {
            @Override
            public String create() {
                String r = payReq_.getText().toString();
                if (r.isEmpty()) {
                    state_.setText("Please specify payreq");
                    return null;
                }
                return r;
            }
        });

        model_.addContact().setRequestFactory(this, new IRequestFactory<WalletData.AddContactRequest>() {
            @Override
            public WalletData.AddContactRequest create() {
                if (model_.decodePayReq().response() != null) {
                    return WalletData.AddContactRequest.builder()
                            .setPubkey(model_.decodePayReq().response().destPubkey())
                            .setName(name_.getText().toString())
                            .setDescription(description_.getText().toString())
                            .setUrl(url_.getText().toString())
                            .setRouteHints(model_.decodePayReq().response().routeHints())
                            .setFeatures(model_.decodePayReq().response().features())
                            .build();
                } else {
                    return WalletData.AddContactRequest.builder()
                            .setPubkey(pubkey_.getText().toString())
                            .setName(name_.getText().toString())
                            .setDescription(description_.getText().toString())
                            .setUrl(url_.getText().toString())
                            .build();
                }
            }
        });
        model_.addContact().setCallback(this, new IResponseCallback<WalletData.Contact>() {
            @Override
            public void onResponse(WalletData.Contact r) {
                Log.i(TAG, "added contact "+r);
                showContact(r.id());
                finish();
            }

            @Override
            public void onError(String code, String e) {
                Log.e(TAG, "add contact error e " + code + " m " + e);
                state_.setText("Error: " + e);
            }
        });

        recoverUseCases();
    }

    private void recoverUseCases() {
        if (model_.decodePayReq().isExecuting())
            decodePayReq();
        else if (model_.addContact().isExecuting())
            addContact();
    }

    private void decodePayReq() {
        state_.setText("Decoding...");
        if (model_.decodePayReq().isExecuting())
            model_.decodePayReq().recover();
        else
            model_.decodePayReq().execute("");
    }

    private void addContact(){
        state_.setText("Adding...");
        if (model_.addContact().isExecuting())
            model_.addContact().recover();
        else
            model_.addContact().execute("");
    }

    private void startScan(){
        model_.startScan();
        new IntentIntegrator(this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() != null) {
                model_.setScanResult(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showContact(long id) {
        Intent intent = new Intent(this, GetContactActivity.class);
        intent.putExtra(Application.ID_MESSAGE, id);
        startActivity(intent);
    }

}
