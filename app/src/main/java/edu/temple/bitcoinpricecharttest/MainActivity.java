package edu.temple.bitcoinpricecharttest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    String[] timeRanges = {"1d", "5d", "1M", "6M", "1Y", "2Y"};
    boolean connected;
    String chartType = "1d";
    PriceChartService mPriceChartService;

    // TODO: adapt this adapter!
    public class MyAdapter extends BaseAdapter {
        String[] list;
        int resourceId;
        public MyAdapter (String[] array, final int resourceId){
            list = array;
            this.resourceId = resourceId;
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            super.unregisterDataSetObserver(observer);
        }

        @Override
        public View getView(int position, View oldView, ViewGroup parent) {

            TextView tv = new TextView((Context) MainActivity.this);

            LinearLayout layout = new LinearLayout((Context) MainActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);

            final String item = getItem(position);
            if (item != null) {
                String[] displayedList = getResources().getStringArray(resourceId);
                tv.setText(displayedList[position]);
                tv.setTextSize(20);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.this.onSpinnerInteraction(item);
                    }
                });

                layout.addView(tv);
            }

            return layout;
        }
        @Override
        public String getItem(int position){
            return list[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public int getCount(){
            return list.length;
        }
    }

    public void onSpinnerInteraction(String item){
        chartType = item;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // set up spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        SpinnerAdapter adapter = new MyAdapter(timeRanges, R.array.time_ranges);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        // set up button
        ((Button) findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("MainActivity", "Connecting");
                if (connected) {
                    mPriceChartService.getChart(serviceHandler, chartType);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(this, PriceChartService.class);
        bindService(serviceIntent, myConnection, Context.BIND_AUTO_CREATE);
    }

    final Handler serviceHandler = new Handler(new Handler.Callback(){
        public boolean handleMessage(Message msg){
            Bitmap chart = (Bitmap) msg.obj;
            ImageView iv = (ImageView) findViewById(R.id.imageView);
            iv.setImageBitmap(chart);
            return false;
        }
    });

    ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PriceChartService.TestBinder binder = (PriceChartService.TestBinder) service;
            mPriceChartService = binder.getService();

            connected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connected = false;
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        unbindService(myConnection);
    }

}
