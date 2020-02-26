package org.lndroid.wallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.lndroid.framework.WalletData;

public class GetSendPaymentActivity extends WalletActivityBase {

    private static final String TAG = "GetInvoiceActivity";

    private TextView id_;
    private TextView destination_;
    private TextView amount_;
    private TextView description_;
    private TextView sendTime_;
    private TextView state_;
    private DateFormat dateFormat_;

    private GetSendPaymentViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_send_payment);

        Intent intent = getIntent();
        final long id = intent.getLongExtra(Application.ID_MESSAGE, 0);

        model_ = ViewModelProviders.of(this).get(GetSendPaymentViewModel.class);
        setModel(model_);

        dateFormat_ = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, new Locale("en", "US"));

        id_ = findViewById(R.id.sendPaymentId);
        destination_ = findViewById(R.id.destination);
        amount_ = findViewById(R.id.amount);
        description_ = findViewById(R.id.description);
        sendTime_ = findViewById(R.id.sendTime);
        state_ = findViewById(R.id.state);

        model_.getSendPayment().data().observe(this, new Observer<WalletData.SendPayment>() {
            @Override
            public void onChanged(WalletData.SendPayment p) {
                if (p == null)
                    return;

                id_.setText("ID: "+p.id());
                destination_.setText("Destination: "+p.destPubkey());
                amount_.setText("Amount: "+p.valueMsat() / 1000);
                description_.setText("Description: "+p.invoiceDescription());
                sendTime_.setText("Sent: "+ (p.sendTime() > 0 ? dateFormat_.format(new Date(p.sendTime())) : ""));
                switch (p.state()) {
                    case WalletData.SEND_PAYMENT_STATE_PENDING:
                        state_.setText("State: sending");
                        break;
                    case WalletData.SEND_PAYMENT_STATE_OK:
                        state_.setText("State: sent");
                        break;
                    case WalletData.SEND_PAYMENT_STATE_FAILED:
                        state_.setText("State: error "+
                                (p.paymentError() != null ? p.paymentError() : "")+" "+
                                (p.errorMessage() != null ? p.errorMessage() : ""));
                        break;
                }
            }
        });

        if (!model_.getSendPayment().isActive()) {
            WalletData.GetRequestLong r = WalletData.GetRequestLong.builder()
                    .setSubscribe(true)
                    .setId(id)
                    .build();
            model_.setSendPaymentRequest(r);
            model_.getSendPayment().start();
        }
    }
}
