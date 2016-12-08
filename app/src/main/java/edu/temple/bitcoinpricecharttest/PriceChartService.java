package edu.temple.bitcoinpricecharttest;

import android.app.Service;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class PriceChartService extends Service {
    public PriceChartService() {
}
    Handler handler;
    IBinder mBinder = new TestBinder();

    @Override
    public IBinder onBind(Intent intent) {

        Notification.Builder n;

        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setAction("SOME_ACTION");
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, i, 0);
        n  = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Your PriceChartService is running")
                .setContentIntent(pIntent)
                .setAutoCancel(false);

        return mBinder;
    }

    public class TestBinder extends Binder {
        PriceChartService getService (){
            return PriceChartService.this;
        }
    }

    public void getChart(final Handler handler, final String chartType) {
        this.handler = handler;
        Thread thd = new Thread() {
            public void run() {
                try {
                    while (true) {
                        InputStream is = (InputStream)
                                new URL("https://chart.yahoo.com/z?s=BTCUSD=X&t=" + chartType)
                                        .getContent();
                        Bitmap chart = BitmapFactory.decodeStream(is);
                        Message msg = Message.obtain();
                        msg.obj = chart;
                        Log.v("Downloaded chart", chart.toString());
                        handler.sendMessage(msg);
                        sleep(15000); // sleep for 15 seconds
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thd.start();
    }
}
