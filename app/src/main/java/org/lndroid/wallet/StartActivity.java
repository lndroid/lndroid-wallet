package org.lndroid.wallet;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import org.lndroid.framework.WalletData;
import org.lndroid.wallet.init.CreateRootActivity;
import org.lndroid.wallet.init.InitActivity;

public class StartActivity extends FragmentActivity {

    private StartViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        model_ = ViewModelProviders.of(this).get(StartViewModel.class);

        // watch state changes
        model_.ready().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean ready) {
                if (ready == null || !ready)
                    return;

                switch (model_.walletState().state()) {
                    case WalletData.WALLET_STATE_INIT:
                        start(InitActivity.class);
                        break;

                    case WalletData.WALLET_STATE_OK:
                        if (model_.root() == null) {
                            start(CreateRootActivity.class);
                        } else {
                            // FIXME also check if signer for root is valid,
                            //  and if not, start the recoverAuth flow
                            start(MainActivity.class);
                        }
                        break;

                    case WalletData.WALLET_STATE_AUTH:
                        start(UnlockActivity.class);
                        // FIXME we might need CreateRoot here too?
                        break;
                }
            }
        });
    }

    void start(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
        finish();
    }
}
