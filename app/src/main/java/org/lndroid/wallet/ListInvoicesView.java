package org.lndroid.wallet;

import android.view.View;
import android.widget.TextView;

import org.lndroid.framework.WalletData;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

class ListInvoicesView {

    public static class ViewHolder extends ListViewBase.ViewHolder<WalletData.Invoice> {
        private TextView amount_;
        private TextView settleTime_;
        private TextView note_;
        private TextView protocol_;
        private DateFormat dateFormat_;

        public ViewHolder(View itemView) {
            super(itemView);

            amount_ = itemView.findViewById(R.id.amount);
            settleTime_ = itemView.findViewById(R.id.settleTime);
            note_ = itemView.findViewById(R.id.note);
            protocol_ = itemView.findViewById(R.id.protocol);
            dateFormat_ = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, new Locale("en", "US"));
        }

        protected void fillData() {
            amount_.setText((data().amountPaidMsat() / 1000)+" / "+data().valueSat()+" sat");
            if (data().settleTime() != 0)
                settleTime_.setText("Paid on "+dateFormat_.format(new Date(data().settleTime())));
            else if ((data().createTime() + data().expiry() * 1000) < System.currentTimeMillis())
                settleTime_.setText("Expired");
            else
                settleTime_.setText("Active");

            if (data().description() != null) {
                String desc = data().description();
                if (desc.length() > 20)
                    desc = desc.substring(0, 19)+"...";
                note_.setText(desc);
            } else if (data().descriptionHashHex() != null) {
                String hash = data().descriptionHashHex();
                if (hash.length() > 12)
                    hash = hash.substring(0, 6) + "..." + hash.substring(hash.length() - 6);
                note_.setText(hash);
            } else {
                note_.setText("");
            }

            if (data().isKeysend())
                protocol_.setText("keysend");
            else
                protocol_.setText("");
        }

        protected void clearData() {
            amount_.setText("");
            settleTime_.setText("");
            note_.setText("");
            protocol_.setText("");
        }
    }

    public static class Adapter extends ListViewBase.Adapter<WalletData.Invoice, ViewHolder> {

        protected Adapter() {
            super(R.layout.list_invoices, new ListViewBase.IViewHolderFactory<ViewHolder>() {
                @Override
                public ViewHolder create(View view) {
                    return new ViewHolder(view);
                }
            });
        }
    }

}