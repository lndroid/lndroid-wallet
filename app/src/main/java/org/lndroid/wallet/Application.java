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
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.multidex.MultiDexApplication;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.bg.BackgroundActivityService;
import org.lndroid.framework.usecases.bg.ISyncNotificationManager;
import org.lndroid.framework.usecases.bg.RecvPaymentWorker;
import org.lndroid.framework.usecases.bg.SyncWorker;

public class Application extends MultiDexApplication {

    public static final String TAG = "Application";

    public static final String ID_MESSAGE = "org.lndroid.wallet.messages.ID_MESSAGE";
    public static final String SEND_PAYMENT_CHANNEL_ID = "org.lndroid.wallet.notifications.SEND_PAYMENT";
    public static final String PAID_INVOICE_CHANNEL_ID = "org.lndroid.wallet.notifications.PAID_INVOICE";
    public static final String SYNC_CHANNEL_ID = "org.lndroid.wallet.notifications.SYNC";

    public static final String EXTRA_AUTH_REQUEST_ID = "org.lndroid.extra.AUTH_REQUEST_ID";

    public static final int NOTIFICATION_ID_SEND_PAYMENT = 1;
    public static final int NOTIFICATION_ID_SYNC_GRAPH_CHAIN = 2;
    public static final int NOTIFICATION_ID_SYNC_RECV_PAYMENT = 3;

    public static final int PIN_LENGTH = 6;

    // RecvPayment worker implementation that will ensure WalletServer is
    // started and will provide a Plugin client for the worker
    public static class RecvPaymentWorkerImpl extends RecvPaymentWorker {

        public RecvPaymentWorkerImpl(@NonNull Context context, @NonNull WorkerParameters params) {
            super(context, params);
        }

        @Override
        public IPluginClient getPluginClient() {
            WalletServer.ensure(getApplicationContext());
            return WalletServer.buildAnonymousPluginClient();
        }

        @Override
        public ISyncNotificationManager getNotificationManager() {
            return new ISyncNotificationManager() {
                @Override
                public void showNotification(int i) {
                    NotificationManager notificationManager = (NotificationManager)
                            getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(
                            getApplicationContext(), SYNC_CHANNEL_ID)
                            .setContentTitle("Payment service")
                            .setCategory(NotificationCompat.CATEGORY_SERVICE)
                            // NOTE: this is required!
                            .setSmallIcon(R.drawable.ic_logo)
                            .setOngoing(true)
                            .setContentText("Waiting for incoming payments")
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                    ;

                    notificationManager.notify(NOTIFICATION_ID_SYNC_RECV_PAYMENT, builder.build());
                }

                @Override
                public void hideNotification(int i) {
                    NotificationManager notificationManager = (NotificationManager)
                            getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.cancel(NOTIFICATION_ID_SYNC_RECV_PAYMENT);
                }
            };
        }
    }

    // see notes above on RecvPaymentWorkerImpl
    public static class SyncWorkerImpl extends SyncWorker {

        public SyncWorkerImpl(@NonNull Context context, @NonNull WorkerParameters params) {
            super(context, params);
        }

        @Override
        public IPluginClient getPluginClient() {
            WalletServer.ensure(getApplicationContext());
            return WalletServer.buildAnonymousPluginClient();
        }

        @Override
        public ISyncNotificationManager getNotificationManager() {
            return new ISyncNotificationManager() {
                @Override
                public void showNotification(int i) {
                    NotificationManager notificationManager = (NotificationManager)
                            getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(
                            getApplicationContext(), SYNC_CHANNEL_ID)
                            .setContentTitle("Synchronization service")
                            .setCategory(NotificationCompat.CATEGORY_SERVICE)
                            // NOTE: this is required!
                            .setSmallIcon(R.drawable.ic_logo)
                            .setOngoing(true)
                            .setContentText("Synchronization of graph and blockchain")
                            .setPriority(NotificationCompat.PRIORITY_LOW);

                    notificationManager.notify(NOTIFICATION_ID_SYNC_GRAPH_CHAIN, builder.build());
                }

                @Override
                public void hideNotification(int i) {
                    NotificationManager notificationManager = (NotificationManager)
                            getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.cancel(NOTIFICATION_ID_SYNC_GRAPH_CHAIN);
                }
            };
        }
    }

    public static class IpcService extends Service {

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            Toast.makeText(getApplicationContext(), "Starting Lndroid.Wallet service", Toast.LENGTH_SHORT).show();

            WalletServer.ensure(getApplicationContext());
            return WalletServer.getInstance().server().getBinder();
        }
    }

    @Override
    public void onTerminate() {
        Log.i("WalletApplication", "terminating");
        super.onTerminate();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("WalletApplication", "starting");
        FlipperInitializer.init(this);
/*        SoLoader.init(this, false);

        // add Flipper in DEBUG build
        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
            final FlipperClient client = AndroidFlipperClient.getInstance(this);
            client.addPlugin(new InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()));
            client.addPlugin(new DatabasesFlipperPlugin(this));
            client.start();
        }

 */

        // ensure wallet server is started
        WalletServer.ensure(getApplicationContext());

        // make sure ongoing notifications are hidden on the app restart,
        // bcs due to app instability these might hang forever
        NotificationManager notificationManager = (NotificationManager)
                getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID_SYNC_GRAPH_CHAIN);
        notificationManager.cancel(NOTIFICATION_ID_SYNC_RECV_PAYMENT);

        // ensure RecvPayment and SyncPayment workers are scheduled
        // NOTE: increase version if you adjust work settings to reschedule it
        RecvPaymentWorker.schedule(getApplicationContext(), RecvPaymentWorkerImpl.class, 1);

        // NOTE: a) worker can only work for 10 minutes, instead we should implement
        // a long-lived BG service like here https://fabcirablog.weebly.com/blog/creating-a-never-ending-background-service-in-android-gt-7
        // b) we should do general experiment running LND in BG ALL THE TIME and
        // see how much battery drain it causes (slow down the bg plugin execution
        // to reduce the actual CPU utilization)
//        SyncWorker.schedule(getApplicationContext(), SyncWorkerImpl.class, 0);
        WorkManager wm = WorkManager.getInstance(getApplicationContext());
        wm.cancelAllWorkByTag(
                org.lndroid.framework.usecases.bg.SyncWorkerImpl.getVersionTag(SyncWorker.WORK_ID, 0));

        // ensure notification channel
        createNotificationChannel();

        // start payment service to keep app in bg while pending payments
        // are retried
        final BackgroundActivityService ps = BackgroundActivityService.getInstance();
        ps.setServiceManager(new BackgroundActivityService.IServiceManager() {
            private Notification notification_;
            private NotificationCompat.Builder builder_;

            @Override
            public void startService(WalletData.BackgroundInfo info) {
                // we might call this many times if tx terminates by timeout
                // and we restart, we'd get new active payment list but
                // service is already started and no need to restart it.
                if (notification_ != null)
                    return;

                Log.i(TAG, "starting foreground service");
                createNotification(info);

                Intent intent = new Intent(Application.this, BackgroundActivityService.ForegroundService.class);
                ContextCompat.startForegroundService(Application.this, intent);
            }

            private void updateBuilder(WalletData.BackgroundInfo info) {
                String text = "";
                if (info.activeSendPaymentCount() > 0)
                    text += (text.isEmpty() ? "": ", ")+"Sending payments: "+info.activeSendPaymentCount();
                // These might take weeks so don't bother...
//                if (info.pendingChannelCount() > 0)
//                    text += (text.isEmpty() ? "": ", ")+"Pending channels: "+info.pendingChannelCount()+"\n";
                if (info.activeOpenChannelCount() > 0)
                    text += (text.isEmpty() ? "": ", ")+"Opening channels: "+info.activeOpenChannelCount()+"\n";
                if (info.activeCloseChannelCount() > 0)
                    text += (text.isEmpty() ? "": ", ")+"Closing channels: "+info.activeCloseChannelCount()+"\n";
                if (info.activeSendCoinCount() > 0)
                    text += (text.isEmpty() ? "": ", ")+"Sending coins: "+info.activeSendCoinCount()+"\n";

                int priority = NotificationCompat.PRIORITY_LOW;
                if (info.pendingChannelCount() > 0)
                    priority = NotificationCompat.PRIORITY_MAX;

                builder_
                        .setContentText(text)
                        .setPriority(priority)
                ;
            }

            @Override
            public void updateNotification(WalletData.BackgroundInfo info) {
                updateBuilder(info);
                notification_ = builder_.build();

                NotificationManager notificationManager = (NotificationManager)
                        getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(notificationId(), notification_);
            }

            private void createNotification(WalletData.BackgroundInfo info) {
                Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent pendingIntent =
                        PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

                builder_ = new NotificationCompat.Builder(getApplicationContext(), SEND_PAYMENT_CHANNEL_ID)
                        .setContentTitle("Background activity service")
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        // NOTE: this is required!
                        .setSmallIcon(R.drawable.ic_logo)
                        .setContentIntent(pendingIntent)
                        .setOnlyAlertOnce(true)
                        .setOngoing(true)
                        .setDefaults(0) // don't ring or vibrate
                ;

                updateBuilder(info);
                notification_ = builder_.build();
            }

            @Override
            public void stopService() {
                Log.i(TAG, "stopping foreground service");
                Intent intent = new Intent(Application.this, BackgroundActivityService.ForegroundService.class);
                Application.this.stopService(intent);

                NotificationManager notificationManager = (NotificationManager)
                        getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(notificationId());
                notification_ = null;
            }

            @Override
            public int notificationId() {
                return NOTIFICATION_ID_SEND_PAYMENT;
            }

            @Override
            public Notification notification() {
                return notification_;
            }
        });

        ps.start(WalletServer.buildAnonymousPluginClient());
    }

    private void createNotificationChannel( ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(new NotificationChannel(
                    SEND_PAYMENT_CHANNEL_ID,
                    "Wallet backround activity service",
                    NotificationManager.IMPORTANCE_HIGH
            ));
            manager.createNotificationChannel(new NotificationChannel(
                    PAID_INVOICE_CHANNEL_ID,
                    "Wallet payment received",
                    NotificationManager.IMPORTANCE_HIGH
            ));
            manager.createNotificationChannel(new NotificationChannel(
                    SYNC_CHANNEL_ID,
                    "Wallet synchronization service",
                    NotificationManager.IMPORTANCE_DEFAULT
            ));
        }
    }
}
