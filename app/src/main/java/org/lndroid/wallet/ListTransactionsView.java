package org.lndroid.wallet;

import android.view.View;
import android.widget.TextView;

import org.lndroid.framework.WalletData;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class ListTransactionsView {

    public static class ViewHolder extends ListViewBase.ViewHolder<WalletData.Transaction> {
        private TextView hash_;
        private TextView amount_;
        private TextView fee_;
        private TextView time_;
        private TextView confs_;
        private DateFormat dateFormat_;

        public ViewHolder(View itemView) {
            super(itemView);

            hash_ = itemView.findViewById(R.id.hash);
            amount_ = itemView.findViewById(R.id.amount);
            fee_ = itemView.findViewById(R.id.fee);
            time_ = itemView.findViewById(R.id.time);
            confs_ = itemView.findViewById(R.id.confs);
            dateFormat_ = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, new Locale("en", "US"));
        }

        @Override
        protected void fillData() {
            String hash = data().txHash() != null ? data().txHash() : "";
            if (hash.length() > 12)
                hash = hash.substring(0, 6) + "..." + hash.substring(hash.length() - 6);
            hash_.setText(hash);
            amount_.setText(Long.toString(data().amount()));
            fee_.setText("Fee: "+(data().totalFees() > 0 ? "-"+data().totalFees() : "0"));
            time_.setText(dateFormat_.format(new Date(
                    data().timestamp() != 0
                            ? data().timestamp() * 1000
                            : data().createTime())));
            if (data().numConfirmations() == 0)
                confs_.setText("Unconfirmed");
            else
                confs_.setText("Confs "+data().numConfirmations()+" block "+data().blockHeight());
        }

        @Override
        protected void clearData() {
            hash_.setText("");
            amount_.setText("");
            fee_.setText("");
            time_.setText("");
            confs_.setText("");
        }
    }

    public static class Adapter extends ListViewBase.Adapter<WalletData.Transaction, ViewHolder> {

        public Adapter() {
            super(R.layout.list_transactions, new ListViewBase.IViewHolderFactory<ViewHolder>() {
                @Override
                public ViewHolder create(View view) {
                    return new ViewHolder(view);
                }
            });
        }
    }
}

