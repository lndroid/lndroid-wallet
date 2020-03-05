package org.lndroid.wallet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.lndroid.framework.WalletData;

class ListPaymentsView {

    public static class ViewHolder extends ListViewBase.ViewHolder<WalletData.Payment> {
        private TextView amount_;
        private TextView fee_;
        private TextView time_;
        private TextView type_;
        private DateFormat dateFormat_;

        public ViewHolder(View itemView) {
            super(itemView);

            time_ = itemView.findViewById(R.id.time);
            type_ = itemView.findViewById(R.id.type);
            amount_ = itemView.findViewById(R.id.amount);
            fee_ = itemView.findViewById(R.id.fee);

            dateFormat_ = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, new Locale("en", "US"));
        }

        public void fillInvoice(long pid, WalletData.Invoice i) {
            if (i.message() != null)
                type_.setText("Message");
            else if (i.isKeysend())
                type_.setText("Key-send");
            else
                type_.setText("Invoice");
            amount_.setText("+"+i.amountPaidMsat()/1000+" sat");
            fee_.setText("");
            time_.setText(dateFormat_.format(new Date(i.settleTime())));
        }

        public void fillSendPayment(long pid, WalletData.SendPayment p) {
            if (p.message() != null)
                type_.setText("Message");
            else if (p.isKeysend())
                type_.setText("Key-send");
            else
                type_.setText("Invoice");
            amount_.setText("-"+p.valueMsat()/1000+" sat");
            fee_.setText("Fee: "+p.feeMsat());

            switch (p.state()) {
                case WalletData.SEND_PAYMENT_STATE_OK:
                    time_.setText(dateFormat_.format(new Date(p.sendTime())));
                    break;
                case WalletData.SEND_PAYMENT_STATE_FAILED:
                    time_.setText("Failed");
                    break;
                case WalletData.SEND_PAYMENT_STATE_PENDING:
                    time_.setText("Sending");
                    break;
            }

        }

        protected void fillData() {
            switch (data().type()) {
                case WalletData.PAYMENT_TYPE_INVOICE:
                    fillInvoice(data().id(), data().invoices().get(data().sourceId()));
                    break;
                case WalletData.PAYMENT_TYPE_SENDPAYMENT:
                    fillSendPayment(data().id(), data().sendPayments().get(data().sourceId()));
                    break;
            }
        }

        protected void clearData() {
            type_.setText("");
            fee_.setText("");
            amount_.setText("");
            time_.setText("");
        }
    }

    public static class Adapter extends ListViewBase.Adapter<WalletData.Payment, ViewHolder> {

        protected Adapter() {
            super(R.layout.list_payments, new ListViewBase.IViewHolderFactory<ViewHolder>() {
                @Override
                public ViewHolder create(View view) {
                    return new ViewHolder(view);
                }
            });
        }
    }

}