package org.lndroid.wallet.auth;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;

import org.lndroid.framework.WalletData;
import org.lndroid.wallet.WalletViewModelBase;

public class AuthActivityBase extends FragmentActivity {

    private WalletViewModelBase model_;

    void setModel(WalletViewModelBase model) {
        model_ = model;

        // make sure we ask for auth immediately
        model_.ensureSessionToken(getApplicationContext());

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
