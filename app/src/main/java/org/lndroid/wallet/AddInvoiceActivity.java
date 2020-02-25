package org.lndroid.wallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.lndroid.framework.common.IResponseCallback;
import org.lndroid.framework.WalletData;
import org.lndroid.framework.usecases.ActionAddInvoice;
import org.lndroid.framework.usecases.IRequestFactory;

public class AddInvoiceActivity extends AppCompatActivity {

    private static final String TAG = "AddInvoiceActivity";

    private EditText amount_;
    private EditText description_;
    private TextView state_;
    private AddInvoiceViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_invoice);

        model_ = ViewModelProviders.of(this).get(AddInvoiceViewModel.class);
        model_.getSessionToken(getApplicationContext());

        amount_ = findViewById(R.id.amount);
        description_ = findViewById(R.id.description);
        state_ = findViewById(R.id.state);

        Button button = findViewById(R.id.addInvoice);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addInvoice();
            }
        });

        ActionAddInvoice actionAddInvoice = model_.addInvoiceAction();
        actionAddInvoice.setCallback(this, new IResponseCallback<WalletData.Invoice>() {
            @Override
            public void onResponse(WalletData.Invoice r) {
                Log.i(TAG, "invoice "+r);
                state_.setText("id "+r.id()+" payreq "+r.paymentRequest());

                showInvoice(r.id());
                finish();
            }

            @Override
            public void onError(String code, String e) {
                state_.setText("Error "+code+": "+e);
                Log.e(TAG, "invoice error "+code+" msg "+e);
            }
        });
        actionAddInvoice.setRequestFactory(this, new IRequestFactory<WalletData.AddInvoiceRequest>() {
            @Override
            public WalletData.AddInvoiceRequest create() {
                WalletData.AddInvoiceRequest.Builder b = WalletData.AddInvoiceRequest.builder();
                try {
                    b.setValueSat(Long.parseLong(amount_.getText().toString()));
                } catch (Exception e) {
                    state_.setText("Bad amount");
                    return null;
                }
                b.setDescription(description_.getText().toString());
                return b.build();
            }
        });
        // FIXME actionAddInvoice.setAuthCallback();

        recoverUseCases();
    }

    private void recoverUseCases() {
        if (model_.addInvoiceAction().isExecuting())
            addInvoice();
    }

    private void addInvoice() {
        state_.setText("Adding invoice...");
        if (model_.addInvoiceAction().isExecuting())
            model_.addInvoiceAction().recover();
        else
            model_.addInvoiceAction().execute("");
    }

    private void showInvoice(long id) {
        Intent intent = new Intent(this, GetInvoiceActivity.class);
        intent.putExtra(Application.ID_MESSAGE, id);
        startActivity(intent);
    }
}
