package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.GetChannel;

public class GetChannelViewModel extends WalletViewModelBase {

    private static final String TAG = "GetChannelViewModel";

    private GetChannel getChannel_;

    public GetChannelViewModel() {
        super(TAG);

        // create use cases
        getChannel_ = new GetChannel(pluginClient());
    }

    @Override
    protected void onCleared() {
        getChannel_.destroy();
        super.onCleared();
    }

    void setChannelRequest(WalletData.GetRequestLong r) {
        getChannel_.setRequest(r.toBuilder().setNoAuth(true).build());
    }
    GetChannel getChannel() { return getChannel_; }
}