package com.example.coinhunter;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.hardware.display.*;
import android.media.*;
import android.media.projection.*;
import android.os.*;
import android.view.*;
import android.util.DisplayMetrics;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.*;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.nio.ByteBuffer;
import java.util.regex.*;

public class ScreenCaptureService extends Service {

    public static final String ACTION_START = "coinhunter.START";
    public static final String ACTION_STOP = "coinhunter.STOP";
    public static final String ACTION_OCR = "coinhunter.OCR";

    public static final String EXTRA_RESULT = "result";
    public static final String EXTRA_DATA = "data";
    public static final String EXTRA_TEXT = "recognized_text";
    public static final String EXTRA_SECONDS = "seconds";

    private static final int NOTIFICATION_ID = 301;

    private MediaProjection projection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private HandlerThread workerThread;
    private Handler worker;
    private TextRecognizer recognizer;
    private ToneGenerator tone;

    private long lastOcrTime = 0;
    private long lastBeepTime = 0;
    private boolean processing = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        if (ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (ACTION_START.equals(intent.getAction())) {
            startForegroundNotification();

            int resultCode = intent.getIntExtra(EXTRA_RESULT, Activity.RESULT_CANCELED);
            Intent data = intent.getParcelableExtra(EXTRA_DATA);

            if (data == null || resultCode != Activity.RESULT_OK) {
                stopSelf();
                return START_NOT_STICKY;
            }

            startCapture(resultCode, data);
        }

        return START_NOT_STICKY;
    }

    private void startForegroundNotification() {
        String channelId = "coinhunter_capture";

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Coin Hunter đang theo dõi",
                    NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class)
                    .createNotificationChannel(channel);
        }

        Notification notification = new Notification.Builder(this, channelId)
                .setContentTitle("Coin Hunter V3")
                .setContentText("Đang đọc đồng hồ trên màn hình")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(
                    NOTIFICATION_ID,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            );
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private void startCapture(int resultCode, Intent data) {
        try {
            MediaProjectionManager manager =
                    (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

            projection = manager.getMediaProjection(resultCode, data);

            if (projection == null) {
                stopSelf();
                return;
            }

            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            int density = metrics.densityDpi;

            workerThread = new HandlerThread("CoinHunterOCR");
            workerThread.start();
            worker = new Handler(workerThread.getLooper());

            recognizer = TextRecognition.getClient(
                    TextRecognizerOptions.DEFAULT_OPTIONS
            );

            tone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90);

            imageReader = ImageReader.newInstance(
                    width, height, PixelFormat.RGBA_8888, 2
            );

            imageReader.setOnImageAvailableListener(
                    this::processImage, worker
            );

            projection.registerCallback(
                    new MediaProjection.Callback() {
                        @Override
                        public void onStop() {
                            stopSelf();
                        }
                    },
                    worker
            );

            virtualDisplay = projection.createVirtualDisplay(
                    "CoinHunterDisplay",
                    width,
                    height,
                    density,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader.getSurface(),
                    null,
                    worker
            );

        } catch (Exception e) {
            stopSelf();
        }
    }

    private void processImage(ImageReader reader) {
        Image image = null;
        Bitmap bitmap = null;

        try {
            image = reader.acquireLatestImage();

            if (image == null || processing) return;

            long now = SystemClock.elapsedRealtime();
            if (now - lastOcrTime < 700) return;

            lastOcrTime = now;
            processing = true;

            Image.Plane plane = image.getPlanes()[0];
            ByteBuffer buffer = plane.getBuffer();
            int pixelStride = plane.getPixelStride();
            int rowStride = plane.getRowStride();

            int paddedWidth = rowStride / pixelStride;

            Bitmap padded = Bitmap.createBitmap(
                    paddedWidth,
                    image.getHeight(),
                    Bitmap.Config.ARGB_8888
            );

            buffer.rewind();
            padded.copyPixelsFromBuffer(buffer);

            bitmap = Bitmap.createBitmap(
                    padded, 0, 0,
                    image.getWidth(), image.getHeight()
            );

            padded.recycle();

        } catch (Exception e) {
            processing = false;
        } finally {
            if (image != null) image.close();
        }

        if (bitmap == null) return;

        Bitmap finalBitmap = bitmap;

        recognizer.process(InputImage.fromBitmap(finalBitmap, 0))
                .addOnSuccessListener(result -> {
                    String text = result.getText();
                    int seconds = findCountdown(text);

                    Intent broadcast = new Intent(ACTION_OCR);
                    broadcast.setPackage(getPackageName());
                    broadcast.putExtra(EXTRA_TEXT, text);
                    broadcast.putExtra(EXTRA_SECONDS, seconds);
                    sendBroadcast(broadcast);

                    if (seconds >= 0 && seconds <= 3) {
                        long now = SystemClock.elapsedRealtime();

                        if (now - lastBeepTime >= 500) {
                            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 180);
                            lastBeepTime = now;
                        }
                    }
                })
                .addOnCompleteListener(task -> {
                    finalBitmap.recycle();
                    processing = false;
                });
    }

    private int findCountdown(String text) {
        Pattern pattern = Pattern.compile(
                "(?<!\\d)(\\d{1,2}):(\\d{2})(?!\\d)"
        );

        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            try {
                int minutes = Integer.parseInt(matcher.group(1));
                int seconds = Integer.parseInt(matcher.group(2));

                if (seconds < 60) {
                    return minutes * 60 + seconds;
                }
            } catch (Exception ignored) {
            }
        }

        return -1;
    }

    @Override
    public void onDestroy() {
        if (virtualDisplay != null) virtualDisplay.release();
        if (imageReader != null) imageReader.close();
        if (projection != null) projection.stop();
        if (recognizer != null) recognizer.close();
        if (tone != null) tone.release();

        if (workerThread != null) workerThread.quitSafely();

        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public android.os.IBinder onBind(Intent intent) {
        return null;
    }
}
