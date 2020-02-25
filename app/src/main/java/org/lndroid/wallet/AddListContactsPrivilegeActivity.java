package org.lndroid.wallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.lndroid.framework.common.IResponseCallback;
import org.lndroid.framework.WalletData;
import org.lndroid.framework.usecases.IRequestFactory;
import org.lndroid.framework.usecases.rpc.RPCAuthorize;
import org.lndroid.framework.usecases.user.GetAuthRequestUser;

public class AddListContactsPrivilegeActivity extends AppCompatActivity {

    private static final String TAG = "AddListContactsPrivAct";

    private long authRequestId_;
    private TextView state_;
    private TextView app_;
    private AddListContactsPrivilegeViewModel model_;
    private boolean rejected_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_list_contacts_privilege);

        model_ = ViewModelProviders.of(this).get(AddListContactsPrivilegeViewModel.class);
        model_.getSessionToken(getApplicationContext());

        Intent intent = getIntent();
        authRequestId_ = intent.getLongExtra(Application.EXTRA_AUTH_REQUEST_ID, 0);
        if (authRequestId_ == 0) {
            Log.e(TAG, "No auth request id");
            finish();
            return;
        }

        Button confirm = findViewById(R.id.confirm);
        Button cancel = findViewById(R.id.cancel);
        state_ = findViewById(R.id.state);
        app_ = findViewById(R.id.app);

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

        final GetAuthRequestUser getUser = model_.getAuthRequestUser();
        if (!getUser.isActive()) {
            getUser.setRequest(WalletData.GetRequestLong.builder()
                    .setId((long) authRequestId_)
                    .setNoAuth(true)
                    .setSubscribe(true)
                    .build());
            getUser.start();
        }

        getUser.data().observe(this, new Observer<WalletData.User>() {
            @Override
            public void onChanged(WalletData.User user) {
                app_.setText(user.appLabel());
                state_.setText("");
            }
        });
        getUser.error().observe(this, new Observer<WalletData.Error>() {
            @Override
            public void onChanged(WalletData.Error error) {
                Log.e(TAG, "getAuthRequestUser error"+error);
                state_.setText("Error: "+error.message());
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

                    // make sure user can see the app User before confirm is allowed
                    if (getUser.data().getValue() == null)
                        return null;

                    return WalletData.AuthResponse.builder()
                            .setAuthId(authRequestId_)
                            .setAuthorized(true)
                            .setAuthUserId(WalletServer.getInstance().getCurrentUserId().userId())
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
        state_.setText("Please wait...");
        if (model_.rpcAuthorize().isExecuting())
            model_.rpcAuthorize().recover();
        else
            model_.rpcAuthorize().execute();
    }

    public void onBackPressed() {
        reject();
    }

    private void reject() {
        // if we already tried then just exit
        if (model_.rpcAuthorize().request() != null) {
            finish();
            return;
        }

        rejected_ = true;
        sendResult();
    }

    private void confirm() {
        rejected_ = false;
        sendResult();
    }

}
