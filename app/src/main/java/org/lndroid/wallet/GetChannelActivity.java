package org.lndroid.wallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import org.lndroid.framework.WalletData;

public class GetChannelActivity extends AppCompatActivity {

    private static final String TAG = "GetChannelActivity";

    private TextView id_;
    private TextView capacity_;
    private EditText pubkey_;
    private EditText channelPoint_;
    private TextView active_;
    private TextView error_;

    private GetChannelViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_channel);

        Intent intent = getIntent();
        final long id = intent.getLongExtra(Application.ID_MESSAGE, 0);
        if (id == 0)
            throw new RuntimeException("Channel id not provided");

        model_ = ViewModelProviders.of(this).get(GetChannelViewModel.class);

//        dateFormat_ = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, new Locale("en", "US"));

        id_ = findViewById(R.id.channelId);
        pubkey_ = findViewById(R.id.pubkey);
        capacity_ = findViewById(R.id.capacity);
        channelPoint_ = findViewById(R.id.channelPoint);
        active_ = findViewById(R.id.active);
        error_ = findViewById(R.id.error);

        model_.getChannel().data().observe(this, new Observer<WalletData.Channel>() {
            @Override
            public void onChanged(WalletData.Channel c) {
                if (c == null)
                    return;

                id_.setText("Channel ID: "+(c.chanId() != 0 ? c.chanId() : "unknown"));
                capacity_.setText("Capacity: "+c.capacity());
                active_.setText("Active: "+c.active());
                pubkey_.setText(c.remotePubkey());
                channelPoint_.setText(c.channelPoint());
                error_.setText(c.errorMessage());
            }
        });

        if (!model_.getChannel().isActive()) {
            WalletData.GetRequestLong r = WalletData.GetRequestLong.builder()
                    .setSubscribe(true)
                    .setId(id)
                    .build();
            model_.setChannelRequest(r);
            model_.getChannel().start();
        }

    }
}
