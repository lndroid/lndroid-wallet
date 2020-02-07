package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.GetChannel;

public class GetChannelViewModel extends ViewModel {

    private static final String TAG = "GetChannelViewModel";
    private IPluginClient pluginClient_;

    private GetChannel getChannel_;

    public GetChannelViewModel() {
        super();

        pluginClient_ = WalletServer.buildPluginClient();
        Log.i(TAG, "plugin client "+pluginClient_);

        // create use cases
        getChannel_ = new GetChannel(pluginClient_);
    }

    @Override
    protected void onCleared() {
        getChannel_.destroy();
    }

    void setChannelRequest(WalletData.GetRequestLong r) {
        getChannel_.setRequest(r.toBuilder().setNoAuth(true).build());
    }
    GetChannel getChannel() { return getChannel_; }
}