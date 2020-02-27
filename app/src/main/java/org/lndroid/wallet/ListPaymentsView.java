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
        private TextView time_;
        private TextView type_;
        private TextView description_;
        private TextView amount_;
        private DateFormat dateFormat_;

        public ViewHolder(View itemView) {
            super(itemView);

            time_ = itemView.findViewById(R.id.time);
            type_ = itemView.findViewById(R.id.type);
            description_ = itemView.findViewById(R.id.description);
            amount_ = itemView.findViewById(R.id.amount);

            dateFormat_ = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, new Locale("en", "US"));
        }

        public void fillInvoice(long pid, WalletData.Invoice i) {
            type_.setText("Received payment");
            amount_.setText("+"+i.amountPaidMsat()/1000+" sat");
            if (i.purpose() != null && !i.purpose().equals(""))
                description_.setText(i.purpose());
            else if (i.description() != null && !i.description().equals(""))
                description_.setText(i.description());
            else
                description_.setText("No description");
            time_.setText(dateFormat_.format(new Date(i.settleTime())));
        }

        public void fillSendPayment(long pid, WalletData.SendPayment p) {
            switch (p.state()) {
                case WalletData.SEND_PAYMENT_STATE_OK:
                    type_.setText("Sent payment");
                    break;
                case WalletData.SEND_PAYMENT_STATE_FAILED:
                    type_.setText("Payment failed");
                    break;
                case WalletData.SEND_PAYMENT_STATE_PENDING:
                    type_.setText("Sending payment");
                    break;
            }

            amount_.setText("-"+p.valueMsat()/1000+" sat");
            if (p.purpose() != null && !p.purpose().equals(""))
                description_.setText(p.purpose());
            else if (p.invoiceDescription() != null && !p.invoiceDescription().equals(""))
                description_.setText(p.invoiceDescription());
            else
                description_.setText("No description");
            time_.setText(dateFormat_.format(new Date(p.sendTime())));
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
            description_.setText("");
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