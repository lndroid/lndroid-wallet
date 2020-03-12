package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.common.IResponseCallback;
import org.lndroid.framework.engine.AuthClient;
import org.lndroid.framework.engine.IAuthClient;

public class StartViewModel extends ViewModel {

    private static final String TAG = "StartViewModel";
    private IAuthClient authClient_;

    private WalletData.WalletState walletState_;
    private WalletData.User root_;
    private MutableLiveData<Boolean> ready_ = new MutableLiveData<>();
    private MutableLiveData<WalletData.Error> error_ = new MutableLiveData<>();

    public StartViewModel() {
        super();

        authClient_ = new AuthClient(WalletServer.getInstance().server());
        Log.i(TAG, "auth client "+authClient_);

        // wallet state
        getWalletState();
    }

    private void getRoot() {
        authClient_.getUserAuthInfo(WalletData.ROOT_USER_ID, new IResponseCallback<WalletData.User>() {
            @Override
            public void onResponse(WalletData.User user) {
                Log.i(TAG, "root info "+user);
                root_ = user;
                ready_.setValue(true);
            }

            @Override
            public void onError(String s, String s1) {
                Log.e(TAG, "root info error "+s+" err "+s1);
                error_.setValue(WalletData.Error.builder().setCode(s).setMessage(s1).build());
            }
        });
    }

    private void getWalletState() {
        authClient_.subscribeWalletState(new IResponseCallback<WalletData.WalletState>() {
            @Override
            public void onResponse(WalletData.WalletState state) {
                Log.i(TAG, "Wallet state "+state.state());
                walletState_ = state;
                if (state.state() == WalletData.WALLET_STATE_OK
                        || state.state() == WalletData.WALLET_STATE_AUTH) {

                    getRoot();
                } else {
                    ready_.setValue(true);
                }
            }

            @Override
            public void onError(String code, String e) {
                Log.e(TAG, "wallet state sub error "+code+" e "+e);
                error_.setValue(WalletData.Error.builder().setCode(code).setMessage(e).build());
            }
        });
    }

    WalletData.WalletState walletState() { return walletState_; }
    WalletData.User root() { return root_; }

    LiveData<Boolean> ready() { return ready_; }
    LiveData<WalletData.Error> error() { return error_; }
}

