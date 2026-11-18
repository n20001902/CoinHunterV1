package com.example.coinhunter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int SCREEN_CAPTURE_REQUEST = 1001;

    private TextView status;
    private TextView countdown;
    private Button monitorButton;
    private MediaProjectionManager projectionManager;
    private boolean monitoring = false;
    private boolean receiverRegistered = false;

    private final android.content.BroadcastReceiver ocrReceiver =
            new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!ScreenCaptureService.ACTION_OCR.equals(intent.getAction())) return;

            int seconds = intent.getIntExtra(
                    ScreenCaptureService.EXTRA_SECONDS, -1
            );

            if (seconds >= 0) {
                int minutes = seconds / 60;
                int remain = seconds % 60;

                countdown.setText(String.format(
                        java.util.Locale.US, "%02d:%02d", minutes, remain
                ));

                if (seconds <= 3) {
                    status.setText("ĐẾM NGƯỢC! HÃY NHẤN NGAY");
                } else {
                    status.setText("Đang theo dõi đồng hồ");
                }
            }
        }
    };

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, int size, boolean bold, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        projectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        IntentFilter filter = new IntentFilter(ScreenCaptureService.ACTION_OCR);

        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(ocrReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(ocrReceiver, filter);
        }
        receiverRegistered = true;

        getWindow().setStatusBarColor(Color.rgb(245,247,246));
        getWindow().setNavigationBarColor(Color.rgb(245,247,246));
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(22));
        root.setBackgroundColor(Color.rgb(245,247,246));

        root.addView(text("Coin Hunter V3", 27, true, Color.rgb(25,35,30)));
        root.addView(text("Trợ lý theo dõi phần thưởng LIVE", 14, false,
                Color.rgb(95,105,100)));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams cardLp =
                new LinearLayout.LayoutParams(-1, -2);
        cardLp.topMargin = dp(26);
        root.addView(card, cardLp);

        card.addView(text("ĐỒNG HỒ ĐẾM NGƯỢC", 13, true,
                Color.rgb(95,105,100)));

        countdown = text("--:--", 42, true, Color.rgb(22,134,91));
        countdown.setGravity(Gravity.CENTER);
        card.addView(countdown);

        status = text("Chưa cấp quyền chia sẻ màn hình", 15, true,
                Color.rgb(50,60,55));

        LinearLayout.LayoutParams statusLp =
                new LinearLayout.LayoutParams(-1, -2);
        statusLp.topMargin = dp(22);
        root.addView(status, statusLp);

        monitorButton = new Button(this);
        monitorButton.setText("CHO PHÉP ĐỌC MÀN HÌNH");

        LinearLayout.LayoutParams btnLp =
                new LinearLayout.LayoutParams(-1, -2);
        btnLp.topMargin = dp(14);
        root.addView(monitorButton, btnLp);

        monitorButton.setOnClickListener(v -> {
            if (!monitoring) {
                Intent captureIntent =
                        projectionManager.createScreenCaptureIntent();
                startActivityForResult(
                        captureIntent,
                        SCREEN_CAPTURE_REQUEST
                );
            } else {
                Intent stopIntent = new Intent(
                        this, ScreenCaptureService.class
                );
                stopIntent.setAction(ScreenCaptureService.ACTION_STOP);
                startService(stopIntent);

                monitoring = false;
                status.setText("Đã dừng theo dõi");
                countdown.setText("--:--");
                monitorButton.setText("CHO PHÉP ĐỌC MÀN HÌNH");
            }
        });

        TextView details = text(
                "Coin Hunter đọc đồng hồ trên màn hình và phát tiếng bíp " +
                "khi nhận diện còn 3 giây hoặc ít hơn. Bạn tự nhấn để nhận thưởng.",
                14, false, Color.rgb(85,95,90)
        );
        details.setLineSpacing(dp(4), 1f);

        LinearLayout.LayoutParams detailLp =
                new LinearLayout.LayoutParams(-1, -2);
        detailLp.topMargin = dp(26);
        root.addView(details, detailLp);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCREEN_CAPTURE_REQUEST) {
            if (resultCode == RESULT_OK && data != null) {
                Intent serviceIntent = new Intent(
                        this, ScreenCaptureService.class
                );
                serviceIntent.setAction(ScreenCaptureService.ACTION_START);
                serviceIntent.putExtra(
                        ScreenCaptureService.EXTRA_RESULT, resultCode
                );
                serviceIntent.putExtra(
                        ScreenCaptureService.EXTRA_DATA, data
                );

                if (Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }

                monitoring = true;
                status.setText("Đang khởi động nhận diện màn hình...");
                monitorButton.setText("DỪNG");
            } else {
                monitoring = false;
                status.setText("Bạn chưa cấp quyền chia sẻ màn hình");
                monitorButton.setText("CHO PHÉP ĐỌC MÀN HÌNH");
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (receiverRegistered) {
            unregisterReceiver(ocrReceiver);
            receiverRegistered = false;
        }
        super.onDestroy();
    }
}
