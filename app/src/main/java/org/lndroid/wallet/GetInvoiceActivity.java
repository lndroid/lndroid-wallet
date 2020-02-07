package org.lndroid.wallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.lndroid.framework.WalletData;

public class GetInvoiceActivity extends AppCompatActivity {

    private static final String TAG = "GetInvoiceActivity";

    private TextView amount_;
    private TextView description_;
    private TextView createTime_;
    private EditText payReq_;
    private TextView amountPaid_;
    private TextView settleTime_;
    private DateFormat dateFormat_;

    private GetInvoiceViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_invoice);

        Intent intent = getIntent();
        final long id = intent.getLongExtra(Application.ID_MESSAGE, 0);

        model_ = ViewModelProviders.of(this).get(GetInvoiceViewModel.class);

        dateFormat_ = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, new Locale("en", "US"));

        amount_ = findViewById(R.id.amount);
        description_ = findViewById(R.id.description);
        createTime_ = findViewById(R.id.createTime);
        payReq_ = findViewById(R.id.payreq);
        amountPaid_ = findViewById(R.id.amountPaid);
        settleTime_ = findViewById(R.id.settleTime);

        model_.getInvoice().data().observe(this, new Observer<WalletData.Invoice>() {
            @Override
            public void onChanged(WalletData.Invoice invoice) {
                amount_.setText("Amount: "+invoice.valueSat());
                description_.setText("Description: "+invoice.description());
                createTime_.setText("Created: "+ dateFormat_.format(new Date(invoice.createTime())));
                payReq_.setText(invoice.paymentRequest());
                amountPaid_.setText("Paid: "+invoice.amountPaidMsat() / 1000);
                settleTime_.setText("Settled: "+ (invoice.settleTime() > 0
                        ? dateFormat_.format(new Date(invoice.settleTime())) : ""));
            }
        });

        if (!model_.getInvoice().isActive()) {
            WalletData.GetRequestLong r = WalletData.GetRequestLong.builder()
                    .setSubscribe(true)
                    .setId(id)
                    .build();
            model_.setGetInvoiceRequest(r);
            model_.getInvoice().start();
        }
    }
}
