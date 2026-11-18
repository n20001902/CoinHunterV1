package com.example.coinhunter;

import android.app.Activity;
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
    private TextView status;
    private Button monitorButton;
    private boolean monitoring = false;

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

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(245,247,246));
        getWindow().setNavigationBarColor(Color.rgb(245,247,246));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(22));
        root.setBackgroundColor(Color.rgb(245,247,246));

        TextView title = text("Coin Hunter V1", 27, true, Color.rgb(25,35,30));
        root.addView(title);
        TextView subtitle = text("Trợ lý theo dõi phần thưởng LIVE", 14, false, Color.rgb(95,105,100));
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, -2);
        subLp.topMargin = dp(5); root.addView(subtitle, subLp);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1, -2);
        cardLp.topMargin = dp(26); root.addView(card, cardLp);
        card.addView(text("TÀI KHOẢN 01", 12, true, Color.rgb(95,105,100)));
        TextView count = text("—", 38, true, Color.rgb(22,134,91));
        LinearLayout.LayoutParams countLp = new LinearLayout.LayoutParams(-1, -2);
        countLp.topMargin = dp(12); card.addView(count, countLp);
        card.addView(text("Xu đã xác nhận trong phiên", 13, false, Color.rgb(95,105,100)));

        status = text("Chưa bắt đầu theo dõi", 15, true, Color.rgb(50,60,55));
        LinearLayout.LayoutParams statusLp = new LinearLayout.LayoutParams(-1, -2);
        statusLp.topMargin = dp(22); root.addView(status, statusLp);

        monitorButton = new Button(this);
        monitorButton.setText("BẮT ĐẦU THEO DÕI");
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(-1, -2);
        btnLp.topMargin = dp(14); root.addView(monitorButton, btnLp);
        monitorButton.setOnClickListener(v -> {
            monitoring = !monitoring;
            status.setText(monitoring ? "Đang ở chế độ chuẩn bị theo dõi" : "Đã dừng");
            monitorButton.setText(monitoring ? "DỪNG" : "BẮT ĐẦU THEO DÕI");
        });

        TextView note = text("LƯU Ý VỀ BẢN V1", 13, true, Color.rgb(45,55,50));
        LinearLayout.LayoutParams noteLp = new LinearLayout.LayoutParams(-1, -2);
        noteLp.topMargin = dp(28); root.addView(note, noteLp);
        TextView details = text("Đây là bộ khung giao diện thử nghiệm. Bản này CHƯA đọc màn hình TikTok, chưa nhận diện nút xu và chưa tự nhấn. Các chức năng đó cần được phát triển, kiểm thử và chỉ bật khi phù hợp với quy định TikTok. Không có cách nào bảo đảm tài khoản không bị quét hoặc hạn chế.", 14, false, Color.rgb(85,95,90));
        details.setLineSpacing(dp(4), 1f);
        LinearLayout.LayoutParams detailLp = new LinearLayout.LayoutParams(-1, -2);
        detailLp.topMargin = dp(9); root.addView(details, detailLp);

        TextView footer = text("Thiết kế cho Android • 1 tài khoản • Không yêu cầu root", 12, false, Color.rgb(120,128,124));
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footLp = new LinearLayout.LayoutParams(-1, -2);
        footLp.topMargin = dp(28); root.addView(footer, footLp);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
    }
}
