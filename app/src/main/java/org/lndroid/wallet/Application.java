package org.lndroid.wallet;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.multidex.MultiDexApplication;
import androidx.work.WorkerParameters;

import com.facebook.flipper.android.AndroidFlipperClient;
import com.facebook.flipper.android.utils.FlipperUtils;
import com.facebook.flipper.core.FlipperClient;
import com.facebook.flipper.plugins.inspector.DescriptorMapping;
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin;
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin;
import com.facebook.soloader.SoLoader;

import org.lndroid.framework.DefaultKeyStore;
import org.lndroid.framework.IDaoConfig;
import org.lndroid.framework.IKeyStore;
import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.bg.RecvPaymentWorker;
import org.lndroid.framework.usecases.bg.SendPaymentService;
import org.lndroid.framework.usecases.bg.SyncWorker;

public class Application extends MultiDexApplication {

    public static final String ID_MESSAGE = "org.lndroid.wallet.messages.ID_MESSAGE";
    public static final String SEND_PAYMENT_CHANNEL_ID = "org.lndroid.wallet.notifications.SEND_PAYMENT";

    public static final String EXTRA_AUTH_REQUEST_ID = "org.lndroid.extra.AUTH_REQUEST_ID";

    // Dao config wrapping an ApplicationContext
    private static class DaoConfig implements IDaoConfig {
        private static final String WALLET_DB = "walletdb";
        private static final String LND_DIR = ".lnd";
        private static final String DATA_DIR = "data";
        private static final String PASSWORD_FILE = ".lndp";
        private static final int WP_AUTH_VALIDITY_PERIOD = 6 * 3600; // 6 hours

        private Context ctx_;
        private DefaultKeyStore keyStore_;

        public DaoConfig(Context ctx) {
            ctx_ = ctx;

            keyStore_ = new DefaultKeyStore(ctx);
            keyStore_.setAuthValidityDuration(WP_AUTH_VALIDITY_PERIOD);
        }

        @Override
        public Context getContext() {
            return ctx_;
        }

        @Override
        public String getFilesPath() {
            return ctx_.getFilesDir().getAbsolutePath();
        }

        @Override
        public String getDataPath() {
            return getFilesPath() + "/" + DATA_DIR;
        }

        @Override
        public String getDatabaseName() {
            return WALLET_DB;
        }

        @Override
        public String getDatabasePath() {
            return ctx_.getDatabasePath(WALLET_DB).getAbsolutePath();
        }

        @Override
        public String getPasswordFileName() {
            return PASSWORD_FILE;
        }

        @Override
        public String getLndDirName() {
            return LND_DIR;
        }

        @Override
        public IKeyStore getKeyStore() {
            return keyStore_;
        }
    }

    // RecvPayment worker implementation that will ensure WalletServer is
    // started and will provide a Plugin client for the worker
    public static class RecvPaymentWorkerImpl extends RecvPaymentWorker {

        public RecvPaymentWorkerImpl(@NonNull Context context, @NonNull WorkerParameters params) {
            super(context, params);
        }

        @Override
        public IPluginClient getPluginClient() {
            WalletServer.ensure(new DaoConfig(getApplicationContext()));
            return WalletServer.buildPluginClient();
        }
    }

    // see notes above on RecvPaymentWorkerImpl
    public static class SyncWorkerImpl extends SyncWorker {

        public SyncWorkerImpl(@NonNull Context context, @NonNull WorkerParameters params) {
            super(context, params);
        }

        @Override
        public IPluginClient getPluginClient() {
            WalletServer.ensure(new DaoConfig(getApplicationContext()));
            return WalletServer.buildPluginClient();
        }
    }

    public static class IpcService extends Service {

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            Toast.makeText(getApplicationContext(), "Starting Lndroid.Wallet service", Toast.LENGTH_SHORT).show();

            WalletServer.ensure(new DaoConfig(getApplicationContext()));
            return WalletServer.getInstance().server().getBinder();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SoLoader.init(this, false);

        // add Flipper in DEBUG build
        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
            final FlipperClient client = AndroidFlipperClient.getInstance(this);
            client.addPlugin(new InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()));
            client.addPlugin(new DatabasesFlipperPlugin(this));
            client.start();
        }

        // ensure wallet server is started
        WalletServer.ensure(new DaoConfig(getApplicationContext()));

        // ensure RecvPayment and SyncPayment workers are scheduled
        // NOTE: increase version if you adjust work settings to reschedule it
        RecvPaymentWorker.schedule(getApplicationContext(), RecvPaymentWorkerImpl.class, 0);
        SyncWorker.schedule(getApplicationContext(), SyncWorkerImpl.class, 0);

        // ensure notification channel
        createNotificationChannel();

        // start payment service to keep app in bg while pending payments
        // are retried
        SendPaymentService ps = SendPaymentService.getInstance();
        ps.setContext(this);
        ps.setNotificationFactory(new SendPaymentService.INofiticationFactory() {
            @Override
            public int notificationId() {
                // FIXME
                return 1;
            }

            @Override
            public Notification createNotification(int paymentCount) {
                Intent notificationIntent = new Intent(getApplicationContext(), ListPaymentsActivity.class);
                PendingIntent pendingIntent =
                        PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

                return new NotificationCompat.Builder(getApplicationContext(), SEND_PAYMENT_CHANNEL_ID)
                        .setContentTitle("Lndroid Wallet")
                        .setContentText("Sending payments: "+paymentCount)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        // NOTE: this is required!
                        // FIXME change
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pendingIntent)
//                        .setOngoing(true)
                        .build();
            }
        });
        ps.start(WalletServer.buildPluginClient());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    SEND_PAYMENT_CHANNEL_ID,
                    "Payment service",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
