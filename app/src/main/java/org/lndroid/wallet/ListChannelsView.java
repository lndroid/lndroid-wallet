package org.lndroid.wallet;

import android.view.View;
import android.widget.TextView;

import org.lndroid.framework.WalletData;

public class ListChannelsView {

    public static class ViewHolder extends ListViewBase.ViewHolder<WalletData.Channel> {
        private TextView peer_;
        private TextView capacity_;
        private TextView status_;
        private TextView balance_;

        public ViewHolder(View itemView) {
            super(itemView);

            peer_ = itemView.findViewById(R.id.peer);
            capacity_ = itemView.findViewById(R.id.capacity);
            status_ = itemView.findViewById(R.id.status);
            balance_ = itemView.findViewById(R.id.balance);
        }

        @Override
        protected void fillData() {
            String pubkey = data().remotePubkey();
            if (pubkey.length() > 12)
                pubkey = pubkey.substring(0, 6) + "..." + pubkey.substring(pubkey.length() - 6);

            peer_.setText(pubkey);
            capacity_.setText(Long.toString(data().capacity()));
            if (data().active())
                status_.setText("Active");
            else if (data().state() == WalletData.CHANNEL_STATE_OPEN)
                status_.setText("Open");
            else if (data().state() == WalletData.CHANNEL_STATE_PENDING_OPEN)
                status_.setText("Opening");
            else if (data().state() == WalletData.CHANNEL_STATE_FAILED)
                status_.setText("Failed to open");
            else if (data().state() == WalletData.CHANNEL_STATE_CLOSED)
                status_.setText("Closed");
            else if (data().state() == WalletData.CHANNEL_STATE_NEW)
                status_.setText("Opening");
            else
                status_.setText("Closing");

            if (data().isPrivate())
                status_.setText(status_.getText().toString()+ ", private");

            String balance = "In "+data().remoteBalance()+" / Out "+data().localBalance();
            balance_.setText(balance);
        }

        @Override
        protected void clearData() {
            peer_.setText("");
            capacity_.setText("");
            status_.setText("");
            balance_.setText("");
        }
    }

    public static class Adapter extends ListViewBase.Adapter<WalletData.Channel, ViewHolder> {

        protected Adapter() {
            super(R.layout.list_channels, new ListViewBase.IViewHolderFactory<ViewHolder>() {
                @Override
                public ViewHolder create(View view) {
                    return new ViewHolder(view);
                }
            });
        }
    }
}

