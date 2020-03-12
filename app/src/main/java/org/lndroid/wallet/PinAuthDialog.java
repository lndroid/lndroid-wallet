package org.lndroid.wallet;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.common.Errors;
import org.lndroid.framework.common.IResponseCallback;
import org.lndroid.framework.defaults.DefaultSignAuthPrompt;

public class PinAuthDialog extends DialogFragment {

    private IResponseCallback<String> cb_;
    private EditText pin_;
    private TextView state_;

    public PinAuthDialog(IResponseCallback<String> cb) {
        super();
        cb_ = cb;

        setStyle(DialogFragment.STYLE_NORMAL, 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.pin_auth_dialog, container, false);
        pin_ = v.findViewById(R.id.pin);
        state_ = v.findViewById(R.id.state);

        // Watch for button clicks.
        Button button = v.findViewById(R.id.confirm);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String pin = pin_.getText().toString();
                if (pin.length() != Application.PIN_LENGTH) {
                    state_.setText("Enter 6-digit pin please");
                    return;
                }

                // FIXME this isn't good:
                //  this is a sync call to PBKDF which is blocking the UI
                //  thread and doesn't let us inform user about what is happening...
                cb_.onResponse(pin);
                cb_ = null;
                PinAuthDialog.this.dismiss();
            }
        });

        button = v.findViewById(R.id.cancel);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PinAuthDialog.this.dismiss();
            }
        });

        return v;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (cb_ != null)
            cb_.onError(Errors.REJECTED, Errors.errorMessage(Errors.REJECTED));
    }

    public static DefaultSignAuthPrompt.IPasswordAuthPrompt createPrompt() {
        return new DefaultSignAuthPrompt.IPasswordAuthPrompt() {
            @Override
            public void start(FragmentActivity activity,
                              final WalletData.User u,
                              final IResponseCallback<String> cb) {
                PinAuthDialog d = new PinAuthDialog(cb);
                d.show(activity.getSupportFragmentManager(), "dialog");
            }
        };
    }
}
