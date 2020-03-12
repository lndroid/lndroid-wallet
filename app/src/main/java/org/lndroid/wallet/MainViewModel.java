package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;


import org.lndroid.framework.common.Errors;
import org.lndroid.framework.common.IResponseCallback;
import org.lndroid.framework.WalletData;
import org.lndroid.framework.engine.AuthClient;
import org.lndroid.framework.engine.IAuthClient;
import org.lndroid.framework.usecases.user.GetChannelBalance;
import org.lndroid.framework.usecases.user.GetWalletBalance;
import org.lndroid.framework.usecases.rpc.RPCGenSeed;
import org.lndroid.framework.usecases.rpc.RPCInitWallet;
import org.lndroid.framework.usecases.rpc.RPCUnlockWallet;
import org.lndroid.framework.usecases.user.GetWalletInfo;

public class MainViewModel extends WalletViewModelBase {

    private static final String TAG = "MainViewModel";

    private GetWalletBalance walletBalance_;
    private GetChannelBalance channelBalance_;
    private GetWalletInfo walletInfo_;

    public MainViewModel() {
        super(TAG);

        walletBalance_ = new GetWalletBalance(pluginClient());
        channelBalance_ = new GetChannelBalance(pluginClient());
        walletInfo_ = new GetWalletInfo(pluginClient());

        startSubscribers();
    }

    @Override
    protected void onCleared() {
        // notify client that we no longer need these txs
        walletBalance_.destroy();
        channelBalance_.destroy();
        walletInfo_.destroy();

        super.onCleared();
    }

    private void subscribeWalletBalance() {
        WalletData.GetRequestLong r = WalletData.GetRequestLong.builder()
                .setSubscribe(true)
                .setNoAuth(true)
                .build();
        walletBalance_.setRequest(r);
        walletBalance_.start();
    }

    private void subscribeChannelBalance() {
        WalletData.GetRequestLong r = WalletData.GetRequestLong.builder()
                .setSubscribe(true)
                .setNoAuth(true)
                .build();
        channelBalance_.setRequest(r);
        channelBalance_.start();
    }

    private void subscribeWalletInfo() {
        WalletData.GetRequestLong r = WalletData.GetRequestLong.builder()
                .setSubscribe(true)
                .setNoAuth(true)
                .build();
        walletInfo_.setRequest(r);
        walletInfo_.start();
    }

    private void startSubscribers() {
        // subscribe to updates
        subscribeWalletBalance();
        subscribeChannelBalance();
        subscribeWalletInfo();
    }

    GetWalletBalance walletBalance() { return walletBalance_; }

    GetChannelBalance channelBalance() { return channelBalance_; }

    GetWalletInfo walletInfo() { return walletInfo_; }

}
