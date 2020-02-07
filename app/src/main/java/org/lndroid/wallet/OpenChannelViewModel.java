package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.JobOpenChannel;

public class OpenChannelViewModel extends ViewModel {

    private static final String TAG = "OpenChannelViewModel";
    private IPluginClient pluginClient_;

    private JobOpenChannel openChannel_;

    public OpenChannelViewModel() {
        super();

        pluginClient_ = WalletServer.buildPluginClient();
        Log.i(TAG, "plugin client "+pluginClient_);

        // create use cases
        openChannel_ = new JobOpenChannel(pluginClient_);
    }

    @Override
    protected void onCleared() {
        openChannel_.destroy();
    }

    JobOpenChannel openChannel() { return openChannel_; }
}
