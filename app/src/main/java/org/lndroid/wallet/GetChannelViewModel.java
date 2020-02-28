package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.GetChannel;

public class GetChannelViewModel extends WalletViewModelBase {

    private static final String TAG = "GetChannelViewModel";

    private GetChannel loader_;
    private GetChannel.Pager pager_;

    public GetChannelViewModel() {
        super(TAG);

        // create use cases
        loader_ = new GetChannel(pluginClient());
        pager_ = loader_.createPager();
    }

    @Override
    protected void onCleared() {
        loader_.destroy();
        super.onCleared();
    }

    GetChannel.Pager getPager() { return pager_; }

}


