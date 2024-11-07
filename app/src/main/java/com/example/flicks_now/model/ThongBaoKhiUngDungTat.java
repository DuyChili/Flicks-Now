package com.example.flicks_now.model;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.flicks_now.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ThongBaoKhiUngDungTat extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "KênhDịchVụForeground";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Kiểm tra nếu thông điệp chứa dữ liệu
        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("message");

            // Hiển thị thông báo
            hienThiThongBao(title, message);
        }

        // Kiểm tra nếu thông điệp chứa thông báo
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String message = remoteMessage.getNotification().getBody();

            // Hiển thị thông báo
            hienThiThongBao(title, message);
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("ThongBaoKhiUngDungTat", "Token mới nhận được: " + token);
        // Gửi token lên máy chủ của bạn nếu cần
    }

    private void hienThiThongBao(String title, String message) {
        taoKenhThongBao();

        // Tạo và hiển thị thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    private void taoKenhThongBao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Kênh Dịch Vụ Foreground",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
