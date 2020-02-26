package org.lndroid.wallet;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;

import org.lndroid.framework.WalletData;

public class WalletActivityBase extends FragmentActivity {

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
