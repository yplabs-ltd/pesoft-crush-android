package net.pesofts.crush.push;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.koushikdutta.ion.Ion;
import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationDisplayedResult;
import com.onesignal.OSNotificationReceivedResult;

import net.pesofts.crush.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * Created by erkas on 2017. 6. 5..
 */

public class OneSignalNotificationExtender extends NotificationExtenderService {
    private String thumbnail;

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult notification) {

        JSONObject data = notification.payload.additionalData;

        try {
            final String type = data.getString("type");
            if (data.has("thumbnail")) {
                thumbnail = data.getString("thumbnail");
            }

            OverrideSettings overrideSettings = new OverrideSettings();
            overrideSettings.extender = new NotificationCompat.Extender() {
                @Override
                public NotificationCompat.Builder extend(NotificationCompat.Builder builder) {
                    // Sets the background notification color to Green on Android 5.0+ devices.
//                return builder.setColor(new BigInteger("FF00FF00", 16).intValue());
                    try {
                        if (thumbnail != null) {
                            Bitmap bitmap = Ion.with(getBaseContext())
                                    .load(thumbnail)
                                    .withBitmap()
                                    .resize(192, 192)
                                    .asBitmap()
                                    .get();

                            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), android.graphics.Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(output);

                            final int color = 0xff424242;
                            final Paint paint = new Paint();
                            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                            final RectF rectF = new RectF(rect);

                            paint.setAntiAlias(true);
                            canvas.drawARGB(0, 0, 0, 0);
                            paint.setColor(color);
                            float roundPx = bitmap.getWidth() / 2;
                            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                            canvas.drawBitmap(bitmap, rect, rect, paint);

                            builder.setLargeIcon(output);
                        }

                        return builder
                                .setSmallIcon(R.drawable.ic_ticker)
                                .setColor(0xFFF2503B);

                    } catch (InterruptedException | ExecutionException e) {
                        Log.e("Push", null, e);
                    }

                    return builder;
                }
            };

            OSNotificationDisplayedResult displayedResult = displayNotification(overrideSettings);
            Log.d("OneSignalExample", "Notification displayed with id: " + displayedResult.androidNotificationId);

        } catch (JSONException e) {
            Log.e("Push", null, e);
        }

        return true;
    }
}
// additionalData : {"thumbnail":"http:\/\/imgcdn.ablesquare.co.kr\/300x300\/filters:format(jpeg)\/image.ablesquare.co.kr\/profile\/20161220\/1076621482221674165","type":"likefavor"}
