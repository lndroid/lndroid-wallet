package org.lndroid.wallet.auth;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.lndroid.framework.common .IResponseCallback;
import org.lndroid.framework.WalletData;
import org.lndroid.framework.usecases.IRequestFactory;
import org.lndroid.framework.usecases.rpc.RPCAuthorize;
import org.lndroid.wallet.Application;
import org.lndroid.wallet.R;
import org.lndroid.wallet.WalletServer;

public class AddContactPaymentsPrivilegeActivity extends AuthActivityBase {

    private static final String TAG = "AddContPaymentsPrivAct";

    private long authRequestId_;
    private TextView state_;
    private TextView app_;
    private TextView contact_;
    private AddContactPaymentsPrivilegeViewModel model_;
    private boolean rejected_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact_payments_privilege);

        Intent intent = getIntent();
        authRequestId_ = intent.getLongExtra(Application.EXTRA_AUTH_REQUEST_ID, 0);
        if (authRequestId_ == 0) {
            Log.e(TAG, "No auth request id");
            finish();
            return;
        }

        model_ = ViewModelProviders.of(this).get(AddContactPaymentsPrivilegeViewModel.class);
        setModel(model_);

        Button confirm = findViewById(R.id.confirm);
        Button cancel = findViewById(R.id.cancel);
        state_ = findViewById(R.id.state);
        app_ = findViewById(R.id.app);
        contact_ = findViewById(R.id.contact);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reject();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });

        state_.setText("Please wait...");

        // let model load all required data
        model_.start(authRequestId_);

        model_.ready().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean ready) {
                if (ready != null && ready == true) {
                    app_.setText(model_.user().appLabel());
                    contact_.setText(model_.contact().name());
                    state_.setText("");
                } else {
                    app_.setText("");
                    contact_.setText("");
                }
            }
        });

        model_.error().observe(this, new Observer<WalletData.Error>() {
            @Override
            public void onChanged(WalletData.Error error) {
                if (error != null) {
                    Log.e(TAG, "viewmodel error" + error);
                    state_.setText("Error: " + error.message());
                }
            }
        });

        RPCAuthorize rpcAuthorize = model_.rpcAuthorize();
        rpcAuthorize.setRequestFactory(this, new IRequestFactory<WalletData.AuthResponse>() {
            @Override
            public WalletData.AuthResponse create() {

                if (rejected_) {
                    return WalletData.AuthResponse.builder()
                            .setAuthId(authRequestId_)
                            .setAuthorized(false)
                            .setAuthUserId(WalletServer.getInstance().getCurrentUserId().userId())
                            .build();
                } else {

                    // make sure we wait until model is ready
                    if (model_.ready().getValue() == null || model_.ready().getValue() == false)
                        return null;

                    return WalletData.AuthResponse.builder()
                            .setAuthId(authRequestId_)
                            .setAuthorized(true)
                            .setAuthUserId(WalletServer.getInstance().getCurrentUserId().userId())
                            .setData(model_.request().toBuilder().setCreateTime(System.currentTimeMillis()).build())
                            .build();
                }
            }
        });

        rpcAuthorize.setCallback(this, new IResponseCallback<Boolean>() {
            @Override
            public void onResponse(Boolean r) {
                Log.i(TAG, "auth result accepted");
                finish();
            }

            @Override
            public void onError(String code, String e) {
                Log.e(TAG, "auth rejected e " + code + " m " + e);
                state_.setText("Error: " + e);
            }
        });

        recoverUseCases();
    }

    private void recoverUseCases() {
        if (model_.rpcAuthorize().isExecuting())
            sendResult();
    }

    private void sendResult(){
        // if we already tried then just exit
        if (model_.rpcAuthorize().request() != null) {
            finish();
            return;
        }

        state_.setText("Please wait...");
        if (model_.rpcAuthorize().isExecuting())
            model_.rpcAuthorize().recover();
        else
            model_.rpcAuthorize().execute();
    }

    @Override
    public void onBackPressed() {
        reject();
    }

    private void reject() {
        rejected_ = true;
        sendResult();
    }

    private void confirm() {
        rejected_ = false;
        sendResult();
    }

}
