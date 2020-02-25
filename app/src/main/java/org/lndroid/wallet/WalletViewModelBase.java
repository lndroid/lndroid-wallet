package org.lndroid.wallet;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.common.IResponseCallback;

class WalletViewModelBase extends ViewModel {
    private String tag_;
    private IPluginClient pluginClient_;
    private MutableLiveData<WalletData.Error> clientError_ = new MutableLiveData<>();

    WalletViewModelBase(String tag) {
        super();

        tag_ = tag;

        pluginClient_ = WalletServer.buildPluginClient();
        Log.i(tag_, "plugin client "+pluginClient_);

        pluginClient_.setOnError(new IResponseCallback<WalletData.Error>() {
            @Override
            public void onResponse(WalletData.Error error) {
                clientError_.setValue(error);
            }

            @Override
            public void onError(String c, String e) {
                clientError_.setValue(WalletData.Error.builder()
                    .setCode(c)
                    .setMessage(e)
                    .build());
            }
        });
    }

    boolean haveSessionToken() {
        return pluginClient_.haveSessionToken();
    }

    void getSessionToken(Context ctx) {
        WalletServer.getInstance().getSessionToken(ctx, new IResponseCallback<String>() {
            @Override
            public void onResponse(String s) {
                pluginClient_.setSessionToken(s);
            }

            @Override
            public void onError(String s, String s1) {
                Log.e(tag_, "Failed to get session token: "+s);
                // FIXME now what?
            }
        });
    }

    IPluginClient pluginClient() { return pluginClient_; }
    LiveData<WalletData.Error> clientError() { return clientError_; }
}
