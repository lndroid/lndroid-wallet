package org.lndroid.wallet.auth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;

import org.lndroid.framework.WalletData;
import org.lndroid.wallet.WalletViewModelBase;

public class AuthActivityBase extends AppCompatActivity {

    private WalletViewModelBase model_;

    void setModel(WalletViewModelBase model) {
        model_ = model;

        // make sure we ask for auth immediately
        model_.ensureSessionToken(this);

        // terminate immediately if user auth fails
        model_.authError().observe(this, new Observer<WalletData.Error>() {
            @Override
            public void onChanged(WalletData.Error error) {
                if (error != null)
                    finish();
            }
        });
    }

}
