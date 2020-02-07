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

class PaymentListView {

    public static class PaymentListViewHolder extends RecyclerView.ViewHolder {
        private TextView time_;
        private TextView type_;
        private TextView description_;
        private TextView amount_;
        private DateFormat dateFormat_;
        private WalletData.Payment boundPayment_;

        public PaymentListViewHolder(View itemView) {
            super(itemView);

            time_ = itemView.findViewById(R.id.time);
            type_ = itemView.findViewById(R.id.type);
            description_ = itemView.findViewById(R.id.description);
            amount_ = itemView.findViewById(R.id.amount);

            dateFormat_ = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, new Locale("en", "US"));
        }

        public void bindToInvoice(long pid, WalletData.Invoice i) {
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

        public void bindToSendPayment(long pid, WalletData.SendPayment p) {
            switch (p.state()) {
                case WalletData.SEND_PAYMENT_STATE_OK:
                    type_.setText("Sent payment");
                    break;
                case WalletData.SEND_PAYMENT_STATE_FAILED:
                    type_.setText("Payment failed");
                    break;
                case WalletData.SEND_PAYMENT_STATE_PENDING:
                    if (p.nextTryTime() > 0)
                        type_.setText("Retrying payment");
                    else
                        type_.setText("Sending payment");
                    break;
                case WalletData.SEND_PAYMENT_STATE_SENDING:
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

        public void bindTo(WalletData.Payment p) {
            boundPayment_= p;
            switch (p.type()) {
                case WalletData.PAYMENT_TYPE_INVOICE:
                    bindToInvoice(p.id(), p.invoices().get(p.sourceId()));
                    break;
                case WalletData.PAYMENT_TYPE_SENDPAYMENT:
                    bindToSendPayment(p.id(), p.sendPayments().get(p.sourceId()));
                    break;
            }
        }

        public WalletData.Payment boundPayment() {
            return boundPayment_;
        }

        public void clear() {
            boundPayment_ = null;
            type_.setText("");
            description_.setText("");
            amount_.setText("");
            time_.setText("");
        }
    }

    public static class PaymentListAdapter extends PagedListAdapter<WalletData.Payment, PaymentListViewHolder> {

        protected PaymentListAdapter() {
            super(DIFF_CALLBACK);
        }

        private View.OnClickListener itemClickListener_;

        public void setItemClickListener (View.OnClickListener cl) {
            itemClickListener_ = cl;
        }

        @NonNull
        @Override
        public PaymentListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View view = inflater.inflate(R.layout.list_payments, parent, false);
            if (itemClickListener_ != null)
                view.setOnClickListener(itemClickListener_);

            // Return a new holder instance
            return new PaymentListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PaymentListViewHolder holder,
                                     int position) {
            WalletData.Payment payment = getItem(position);

            if (payment != null)
                holder.bindTo(payment);
            else
                holder.clear();
        }

        private static DiffUtil.ItemCallback<WalletData.Payment> DIFF_CALLBACK
                = new DiffUtil.ItemCallback<WalletData.Payment>() {
            @Override
            public boolean areItemsTheSame(
                    @NonNull WalletData.Payment a, @NonNull WalletData.Payment b) {
                return a.id() == b.id();
            }

            @Override
            public boolean areContentsTheSame(
                    @NonNull WalletData.Payment a, @NonNull WalletData.Payment b) {

                return a.equals(b);
/*
                if (a.id() != b.id())
                    return false;

                if (a.type != b.type)
                    return false;

                if (a.sourceId != b.sourceId)
                    return false;

                // NOTE: adjust these based upon the visible list of fields
                if (a.sendPayment != null) {
                    return a.sendPayment.sendTime == b.sendPayment.sendTime
                            && a.sendPayment.feeMsat == b.sendPayment.feeMsat
                            && a.sendPayment.totalValueMsat == b.sendPayment.totalValueMsat
                            && a.sendPayment.lastTryTime == b.sendPayment.lastTryTime
                            && a.sendPayment.state == b.sendPayment.state;
                } else if (a.invoice != null) {
                    return a.invoice.settleIndex == b.invoice.settleIndex
                            && a.invoice.amountPaidMsat == b.invoice.amountPaidMsat
                            && a.invoice.authUserId == b.invoice.authUserId
                            && a.invoice.settleTime == b.invoice.settleTime
                            && a.invoice.htlcsCount == b.invoice.htlcsCount
                            && a.invoice.state == b.invoice.state;
                }
                return false;

 */
            }
        };
    }

}