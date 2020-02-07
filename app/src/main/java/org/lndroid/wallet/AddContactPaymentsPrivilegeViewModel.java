package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.lndroid.framework.IResponseCallback;
import org.lndroid.framework.WalletData;
import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.common.Errors;
import org.lndroid.framework.engine.AuthClient;
import org.lndroid.framework.engine.IAuthClient;
import org.lndroid.framework.usecases.GetContact;
import org.lndroid.framework.usecases.rpc.RPCAuthorize;
import org.lndroid.framework.usecases.user.GetAuthRequestUser;

public class AddContactPaymentsPrivilegeViewModel extends ViewModel {
    private static final String TAG = "AddContactPaysPrivVM";
    private IPluginClient pluginClient_;
    private IAuthClient authClient_;

    private int authRequestId_;
    private WalletData.AuthRequest authRequest_;
    private WalletData.ContactPaymentsPrivilege request_;
    private WalletData.User user_;
    private WalletData.Contact contact_;

    private MutableLiveData<Boolean> ready_ = new MutableLiveData<>();
    private MutableLiveData<WalletData.Error> error_ = new MutableLiveData<>();

    private GetAuthRequestUser getAuthRequestUser_;
    private GetContact getContact_;
    private RPCAuthorize rpcAuthorize_;

    public AddContactPaymentsPrivilegeViewModel() {
        super();

        pluginClient_ = WalletServer.buildPluginClient();
        authClient_ = new AuthClient(WalletServer.getInstance().server());

        // create use cases
        getAuthRequestUser_ = new GetAuthRequestUser(pluginClient_);
        getContact_ = new GetContact(pluginClient_);
        rpcAuthorize_ = new RPCAuthorize(authClient_);
    }

    @Override
    protected void onCleared() {
        getAuthRequestUser_.destroy();
        getContact_.destroy();
    }

    private void setError(String label,String code, String err) {
        Log.e(TAG, label+" error "+code+" e "+err);
        error_.setValue(WalletData.Error.create(code, err));
        ready_.setValue(false);
    }

    private void getRequest() {
        authClient_.getTransactionRequest(authRequest_.userId(), authRequest_.txId(), WalletData.ContactPaymentsPrivilege.class,
                new IResponseCallback<WalletData.ContactPaymentsPrivilege>() {
                    @Override
                    public void onResponse(WalletData.ContactPaymentsPrivilege r) {
                        request_ = r;
                        getContact();
                    }

                    @Override
                    public void onError(String code, String e) {
                        setError("getTransactionRequestUser", code, e);
                    }
                });
    }

    private void getUser() {
        getAuthRequestUser_.setRequest(WalletData.GetRequestLong.builder()
                    .setId((long) authRequestId_)
                    .setNoAuth(true)
                    .build());
        getAuthRequestUser_.setCallback(new IResponseCallback<WalletData.User>() {
            @Override
            public void onResponse(WalletData.User r) {
                user_ = r;
                if (contact_ != null)
                    ready_.setValue(true);
            }

            @Override
            public void onError(String code, String e) {
                setError("getAuthRequestUser", code, e);
            }
        });
        getAuthRequestUser_.start();
    }

    private void getContact() {
        getContact_.setRequest(WalletData.GetRequestLong.builder()
                .setId(request_.contactId())
                .setNoAuth(true)
                .build());
        getContact_.setCallback(new IResponseCallback<WalletData.Contact>() {
            @Override
            public void onResponse(WalletData.Contact r) {
                contact_ = r;
                if (contact_ == null) {
                    setError("getContact", Errors.PLUGIN_INPUT, "Unknown contact");
                } else if (user_ != null) {
                    ready_.setValue(true);
                }
            }

            @Override
            public void onError(String code, String e) {
                setError("getContact", code, e);
            }
        });
        getContact_.start();
    }

    public void start(int authRequestId) {
        if (authRequestId_ == authRequestId)
            return;

        if (authRequestId_ != 0 && authRequestId != authRequestId_)
            throw new RuntimeException("View model already started");

        authRequestId_ = authRequestId;
        authClient_.getAuthRequest(authRequestId_, new IResponseCallback<WalletData.AuthRequest>() {
            @Override
            public void onResponse(WalletData.AuthRequest r) {
                authRequest_ = r;
                if (r == null) {
                    setError("getAuthRequest", Errors.AUTH_INPUT, "Unknown auth request");
                } else {
                    getRequest();
                    getUser();
                }
            }

            @Override
            public void onError(String code, String e) {
                setError("getAuthRequest", code, e);
            }
        });
    }

    RPCAuthorize rpcAuthorize() { return rpcAuthorize_; }

    WalletData.ContactPaymentsPrivilege request() { return request_; }
    WalletData.User user() { return user_; }
    WalletData.Contact contact() { return contact_; }
    LiveData<Boolean> ready() { return ready_; }
    LiveData<WalletData.Error> error() { return error_; }
}
