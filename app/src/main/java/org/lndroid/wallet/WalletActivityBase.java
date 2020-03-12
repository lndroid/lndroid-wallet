package org.lndroid.wallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import org.lndroid.framework.WalletData;

public class WalletActivityBase extends AppCompatActivity {

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
