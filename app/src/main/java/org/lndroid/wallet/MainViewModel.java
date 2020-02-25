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
import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.ActionAddInvoice;
import org.lndroid.framework.usecases.ActionNewAddress;
import org.lndroid.framework.usecases.user.GetChannelBalance;
import org.lndroid.framework.usecases.user.GetWalletBalance;
import org.lndroid.framework.usecases.JobOpenChannel;
import org.lndroid.framework.usecases.JobSendPayment;
import org.lndroid.framework.usecases.rpc.RPCGenSeed;
import org.lndroid.framework.usecases.rpc.RPCInitWallet;
import org.lndroid.framework.usecases.rpc.RPCUnlockWallet;
import org.lndroid.framework.usecases.user.GetWalletInfo;

public class MainViewModel extends WalletViewModelBase {

    private static final String TAG = "MainViewModel";
    private IAuthClient authClient_;

    private MutableLiveData<WalletData.WalletState> walletState_ = new MutableLiveData<>();
    private RPCUnlockWallet unlockWalletRPC_;
    private RPCInitWallet initWalletRPC_;
    private RPCGenSeed genSeedRPC_;
    private GetWalletBalance walletBalance_;
    private GetChannelBalance channelBalance_;
    private GetWalletInfo walletInfo_;

    public MainViewModel() {
        super(TAG);

        authClient_ = new AuthClient(WalletServer.getInstance().server());
        Log.i(TAG, "auth client "+authClient_);

        // create use cases
        unlockWalletRPC_ = new RPCUnlockWallet(authClient_);
        initWalletRPC_ = new RPCInitWallet(authClient_);
        genSeedRPC_ = new RPCGenSeed(authClient_);

        walletBalance_ = new GetWalletBalance(pluginClient());
        channelBalance_ = new GetChannelBalance(pluginClient());
        walletInfo_ = new GetWalletInfo(pluginClient());

        // wallet state
        subscribeWalletState();
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

    private void stopSubscribers() {
        walletBalance_.stop();
        channelBalance_.stop();
        walletInfo_.stop();
    }

    private void subscribeWalletState() {
        authClient_.subscribeWalletState(new IResponseCallback<WalletData.WalletState>() {
            @Override
            public void onResponse(WalletData.WalletState state) {
                boolean wasOk = walletState_.getValue() != null
                        && walletState_.getValue().state() == WalletData.WALLET_STATE_OK;

                Log.i(TAG, "Wallet state "+state.state()+" wasOk "+wasOk);
                if (state.state() == WalletData.WALLET_STATE_OK && !wasOk) {
                    // turned to ok?
                    startSubscribers();
                } else if (state.state() != WalletData.WALLET_STATE_OK && wasOk) {
                    // turned FROM ok?
                    stopSubscribers();
                }

                walletState_.setValue(state);
            }

            @Override
            public void onError(String code, String e) {
                Log.e(TAG, "wallet state sub error "+code+" e "+e);
            }
        });
    }

    LiveData<WalletData.WalletState> walletState() { return walletState_; }

    GetWalletBalance walletBalance() { return walletBalance_; }

    GetChannelBalance channelBalance() { return channelBalance_; }

    GetWalletInfo walletInfo() { return walletInfo_; }

    RPCUnlockWallet unlockWalletRPC() { return unlockWalletRPC_; }

    RPCInitWallet initWalletRPC() { return initWalletRPC_; }

    RPCGenSeed genSeedRPC() { return genSeedRPC_; }

}
