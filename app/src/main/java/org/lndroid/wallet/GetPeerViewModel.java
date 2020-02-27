package org.lndroid.wallet;

import androidx.paging.PagedList;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.usecases.GetPeer;

import java.util.ArrayList;
import java.util.List;

public class GetPeerViewModel extends WalletViewModelBase {

    private static final String TAG = "GetPeerViewModel";

    private GetPeer getPeer_;
    private GetPeer.Pager getPeerPager_;

    public GetPeerViewModel() {
        super(TAG);

        // create use cases
        getPeer_ = new GetPeer(pluginClient());
        getPeerPager_ = getPeer_.createPager(new GetPeer.IFieldMapper<WalletData.Peer>() {

            @Override
            public List<WalletData.Field> mapToFields(WalletData.Peer p) {
                List<WalletData.Field> fields = new ArrayList<>();
                fields.add(WalletData.Field.builder()
                        .setName("Public key")
                        .setValue(p.pubkey())
                        .build());
                fields.add(WalletData.Field.builder()
                        .setName("Addres")
                        .setValue(p.address())
                        .build());
                return fields;
            }
        });
    }

    @Override
    protected void onCleared() {
        getPeer_.destroy();
        super.onCleared();
    }

    GetPeer.Pager getPeerPager() { return getPeerPager_; }

}

