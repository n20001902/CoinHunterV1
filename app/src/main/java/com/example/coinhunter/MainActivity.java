package com.example.coinhunter;

import android.app.Activity;
import android.media.projection.MediaProjectionManager;
import android.content.Context;
import android.content.Intent;
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
    private Button monitorButton;
    private boolean monitoring = false;
    private MediaProjectionManager projectionManager;

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

        root.addView(text("Coin Hunter V2", 27, true, Color.rgb(25,35,30)));
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

        card.addView(text("TÀI KHOẢN 01", 12, true,
                Color.rgb(95,105,100)));

        card.addView(text("—", 38, true, Color.rgb(22,134,91)));

        card.addView(text("Xu đã xác nhận trong phiên", 13, false,
                Color.rgb(95,105,100)));

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
                monitoring = false;
                status.setText("Đã dừng theo dõi");
                monitorButton.setText("CHO PHÉP ĐỌC MÀN HÌNH");
            }
        });

        TextView note = text("TRẠNG THÁI PHÁT TRIỂN", 13, true,
                Color.rgb(45,55,50));

        LinearLayout.LayoutParams noteLp =
                new LinearLayout.LayoutParams(-1, -2);
        noteLp.topMargin = dp(28);
        root.addView(note, noteLp);

        TextView details = text(
                "Bản V2 đang thử nghiệm chức năng xin quyền chia sẻ màn hình. " +
                "Chưa nhận diện hộp quà, chưa đọc đồng hồ và chưa tự nhấn. " +
                "Bạn cần xác nhận quyền trên Android trước khi chia sẻ màn hình.",
                14, false, Color.rgb(85,95,90)
        );
        details.setLineSpacing(dp(4), 1f);

        root.addView(details);

        TextView footer = text(
                "Android • 1 tài khoản • Không yêu cầu root",
                12, false, Color.rgb(120,128,124)
        );
        footer.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams footLp =
                new LinearLayout.LayoutParams(-1, -2);
        footLp.topMargin = dp(28);
        root.addView(footer, footLp);

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
                monitoring = true;
                status.setText("Đã được cấp quyền chia sẻ màn hình");
                monitorButton.setText("DỪNG");
            } else {
                monitoring = false;
                status.setText("Bạn chưa cấp quyền chia sẻ màn hình");
                monitorButton.setText("CHO PHÉP ĐỌC MÀN HÌNH");
            }
        }
    }
}
