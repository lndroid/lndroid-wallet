package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.JobOpenChannel;

public class OpenChannelViewModel extends WalletViewModelBase {

    private static final String TAG = "OpenChannelViewModel";

    private JobOpenChannel openChannel_;

    public OpenChannelViewModel() {
        super(TAG);

        // create use cases
        openChannel_ = new JobOpenChannel(pluginClient());
    }

    @Override
    protected void onCleared() {
        openChannel_.destroy();
        super.onCleared();
    }

    JobOpenChannel openChannel() { return openChannel_; }
}
