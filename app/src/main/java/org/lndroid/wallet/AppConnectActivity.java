package org.lndroid.wallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.lndroid.framework.IResponseCallback;
import org.lndroid.framework.WalletData;
import org.lndroid.framework.usecases.user.ActionAddUser;
import org.lndroid.framework.usecases.user.GetAppUser;
import org.lndroid.framework.usecases.IRequestFactory;

public class AppConnectActivity extends AppCompatActivity {

    public static final String EXTRA_SERVICE_CLASSNAME = "org.lndroid.extra.SERVICE_CLASSNAME";
    public static final String EXTRA_SERVICE_PACKAGENAME = "org.lndroid.extra.SERVICE_PACKAGENAME";
    public static final String EXTRA_SERVICE_PUBKEY = "org.lndroid.extra.SERVICE_PUBKEY";
    public static final String EXTRA_APP_PUBKEY = "org.lndroid.extra.APP_PUBKEY";

    private static final String TAG = "AppConnectActivity";

    private AppConnectViewModel model_;
    private String appPubkey_;
    private ActivityInfo caller_;
    private TextView state_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_connect);

            Intent intent = getIntent();
            appPubkey_ = intent.getStringExtra(EXTRA_APP_PUBKEY);
            if (appPubkey_ == null || appPubkey_.isEmpty()) {
                Log.e(TAG, "No app appPubkey");
                setResult(RESULT_CANCELED);
                finish();
                return;
            }

        model_ = ViewModelProviders.of(this).get(AppConnectViewModel.class);

        TextView app = findViewById(R.id.app);
        Button confirm = findViewById(R.id.confirm);
        Button cancel = findViewById(R.id.cancel);
        state_ = findViewById(R.id.state);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rejectAppConnect();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmAppConnect();
            }
        });

        try {
//            getPackageManager().uid
            Log.i(TAG, "calling activity "+getCallingActivity());
            caller_ = getPackageManager().getActivityInfo(getCallingActivity(), 0);
            Log.i(TAG, "caller "+caller_.packageName+" appUid "+caller_.applicationInfo.uid+" name "+
                    getPackageManager().getNameForUid(caller_.applicationInfo.uid));
            app.setText(getPackageManager().getApplicationLabel(caller_.applicationInfo));
        } catch (PackageManager.NameNotFoundException e) {
            app.setText("Unknown application");
            confirm.setVisibility(View.INVISIBLE);
        }

        // FIXME
        //  check if similar app is already connected (same appUid and/or package but not appPubkey),
        //  if user(s) exist - show a list of them, asking User if he'd like to reuse
        //  the existing connection, or would like to create a new one

        ActionAddUser addUser = model_.addUser();
        addUser.setCallback(this, new IResponseCallback<WalletData.User>() {
            @Override
            public void onResponse(WalletData.User r) {
                Log.i(TAG, "user "+r);
                state_.setText("id "+r.id());
                replyToApp(r);
            }

            @Override
            public void onError(String code, String e) {
                state_.setText("Error "+code+": "+e);
                Log.e(TAG, "add user error "+code+" msg "+e);
            }
        });
        addUser.setRequestFactory(this, new IRequestFactory<WalletData.AddUserRequest>() {
            @Override
            public WalletData.AddUserRequest create() {
                return WalletData.AddUserRequest.builder()
                        .setRole(WalletData.USER_ROLE_APP)
                        .setAppLabel(getPackageManager().getApplicationLabel(caller_.applicationInfo).toString())
                        .setAppPackageName(getPackageManager().getNameForUid(caller_.applicationInfo.uid))
                        .setAppPubkey(appPubkey_)
                        .build();
            }
        });
        // FIXME actionAddInvoice.setAuthCallback();

        GetAppUser getAppUser = model_.getAppUser();
        getAppUser.data().observe(this, new Observer<WalletData.User>() {
            @Override
            public void onChanged(WalletData.User user) {
                if (user != null)
                {
                    if (!user.appPubkey().equals(appPubkey_))
                        throw new IllegalStateException("Bad app user returned");

                    final String callerName = getPackageManager().getNameForUid(caller_.applicationInfo.uid);
                    if (user.appPackageName().equals(callerName)) {
                        // if there is user for this pubkey and it has
                        // same package name then we just reuse it
                        Log.i(TAG, "user exists for pk " + appPubkey_ + " id " + user.id());
                        replyToApp(user);
                    } else {
                        Log.e(TAG, "app with pubkey "+appPubkey_+" already exists "+user.appPackageName());
                        state_.setText("Application with such Public Key already exists!");
                        // FIXME warn user that it might be a stolen pubkey or some such
                    }
                } else {
                    // otherwise we create a new user for this app
                    addUser();
                }
            }
        });
        getAppUser.error().observe(this, new Observer<WalletData.Error>() {
            @Override
            public void onChanged(WalletData.Error error) {
                Log.e(TAG, "failed to get user by app pubkey " + error);
                state_.setText("Error: "+error.message());
            }
        });


        recoverUseCases();
    }

    private void recoverUseCases() {
        if (model_.addUser().isExecuting())
            addUser();
    }

    private void addUser() {
        // NOTE: we use empty txId bcs there is no point in storing it
        // to restart the user creation. If this Activity dies
        // w/o having confirmed then the whole app-connect process
        // must be restarted by client, and thus since we don't support restoring
        // the app-connect process there is no need to restore a part of it (user
        // creation).

        state_.setText("Creating application user...");
        if (model_.addUser().isExecuting())
            model_.addUser().recover();
        else
            model_.addUser().execute("");
    }

    private void rejectAppConnect() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void confirmAppConnect() {

        // FIXME
        //  also if we found similar users on activity start and User has chosen to
        //  reuse old connection then again we shouldn't create new user but should
        //  just reply w/ existing user credentials
        //  etc

        // first, check if app w/ same pubkey exists,
        // further action is executed in the callback declared above
        model_.getAppUser().setRequest(
                WalletData.GetRequestString.builder().setId(appPubkey_).setNoAuth(true).build());
        model_.getAppUser().start();
    }

    private void replyToApp(WalletData.User u) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_SERVICE_CLASSNAME, Application.IpcService.class.getName());
        intent.putExtra(EXTRA_SERVICE_PACKAGENAME, Application.IpcService.class.getPackage().getName());
        intent.putExtra(EXTRA_SERVICE_PUBKEY, u.pubkey());
        setResult(RESULT_OK, intent);
        finish();
    }

}
