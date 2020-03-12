package org.lndroid.wallet.init;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.common.IResponseCallback;
import org.lndroid.framework.usecases.IRequestFactory;
import org.lndroid.wallet.Application;
import org.lndroid.wallet.MainActivity;
import org.lndroid.wallet.R;
import org.lndroid.wallet.WalletServer;

public class CreateRootActivity extends AppCompatActivity {

    private static final String TAG = "CreateRootActivity";
    private TextView state_;
    private EditText pin_;
    private CreateRootViewModel model_;
    private String authType_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_root);

        model_ = ViewModelProviders.of(this).get(CreateRootViewModel.class);

        final boolean secure = WalletServer.getInstance().keyStore().isDeviceSecure();
        final boolean bio = WalletServer.getInstance().keyStore().isBiometricsAvailable();

        state_ = findViewById(R.id.state);
        pin_ = findViewById(R.id.pin);
        RadioButton radioNone = findViewById(R.id.radioNone);
        RadioButton radioUnlocked = findViewById(R.id.radioUnlocked);
        RadioButton radioDevice = findViewById(R.id.radioDevice);
        RadioButton radioBio = findViewById(R.id.radioBio);
        RadioButton radioPin = findViewById(R.id.radioPin);

        if (!secure) {
            radioDevice.setVisibility(View.GONE);
            radioUnlocked.setVisibility(View.GONE);
        }

        if (!bio) {
            radioBio.setVisibility(View.GONE);
        }

        final RadioGroup authType = findViewById(R.id.authType);
        authType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                state_.setText("");
                switch(i) {
                    case R.id.radioNone: {
                        authType_ = WalletData.AUTH_TYPE_NONE;
                        findViewById(R.id.authTypeNone).setVisibility(View.VISIBLE);
                        findViewById(R.id.authTypeUnlocked).setVisibility(View.GONE);
                        findViewById(R.id.authTypeDevice).setVisibility(View.GONE);
                        findViewById(R.id.authTypeBio).setVisibility(View.GONE);
                        findViewById(R.id.authTypePin).setVisibility(View.GONE);
                        break;
                    }

                    case R.id.radioUnlocked: {
                        authType_ = WalletData.AUTH_TYPE_SCREEN_LOCK;
                        findViewById(R.id.authTypeNone).setVisibility(View.GONE);
                        findViewById(R.id.authTypeUnlocked).setVisibility(View.VISIBLE);
                        findViewById(R.id.authTypeDevice).setVisibility(View.GONE);
                        findViewById(R.id.authTypeBio).setVisibility(View.GONE);
                        findViewById(R.id.authTypePin).setVisibility(View.GONE);
                        break;
                    }

                    case R.id.radioDevice: {
                        authType_ = WalletData.AUTH_TYPE_DEVICE_SECURITY;
                        findViewById(R.id.authTypeNone).setVisibility(View.GONE);
                        findViewById(R.id.authTypeUnlocked).setVisibility(View.GONE);
                        findViewById(R.id.authTypeDevice).setVisibility(View.VISIBLE);
                        findViewById(R.id.authTypeBio).setVisibility(View.GONE);
                        findViewById(R.id.authTypePin).setVisibility(View.GONE);
                        break;
                    }

                    case R.id.radioBio: {
                        authType_ = WalletData.AUTH_TYPE_BIO;
                        findViewById(R.id.authTypeNone).setVisibility(View.GONE);
                        findViewById(R.id.authTypeUnlocked).setVisibility(View.GONE);
                        findViewById(R.id.authTypeDevice).setVisibility(View.GONE);
                        findViewById(R.id.authTypeBio).setVisibility(View.VISIBLE);
                        findViewById(R.id.authTypePin).setVisibility(View.GONE);
                        break;
                    }

                    case R.id.radioPin: {
                        authType_ = WalletData.AUTH_TYPE_PASSWORD;
                        findViewById(R.id.authTypeNone).setVisibility(View.GONE);
                        findViewById(R.id.authTypeUnlocked).setVisibility(View.GONE);
                        findViewById(R.id.authTypeDevice).setVisibility(View.GONE);
                        findViewById(R.id.authTypeBio).setVisibility(View.GONE);
                        findViewById(R.id.authTypePin).setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }
        });

        Button confirm = findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUser();
            }
        });

        model_.createRoot().setRequestFactory(this, new IRequestFactory<WalletData.AddUserRequest>() {
            @Override
            public WalletData.AddUserRequest create() {
                if (authType_ == null) {
                    state_.setText("Please choose auth type");
                    return null;
                }

                final String pin = pin_.getText().toString();
                if (WalletData.AUTH_TYPE_PASSWORD.equals(authType_) && pin.length() != Application.PIN_LENGTH) {
                    state_.setText("Please set 6-digit pin");
                    return null;
                }

                return WalletData.AddUserRequest.builder()
                        .setRole(WalletData.USER_ROLE_ROOT)
                        .setAuthType(authType_)
                        .setPassword(pin)
                        .build();
            }
        });
        model_.createRoot().setCallback(this, new IResponseCallback<WalletData.User>() {
            @Override
            public void onResponse(WalletData.User user) {
                Log.i(TAG, "created root "+user);
                // FIXME actually would be nice to produce a valid session token here
                //  to not force the auth immediately after wallet start?
                startMain();
            }

            @Override
            public void onError(String s, String s1) {
                Log.e(TAG, "createRoot failed "+s+" err "+s1);
                state_.setText("Error: "+s1);
            }
        });

        recoverUseCases();
    }

    private void recoverUseCases() {
        if (model_.createRoot().isExecuting())
            createUser();
    }

    private void createUser() {
        state_.setText("Please wait...");
        if (model_.createRoot().isExecuting())
            model_.createRoot().recover();
        else
            model_.createRoot().execute();
    }

    private void startMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
