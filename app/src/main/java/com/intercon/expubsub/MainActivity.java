package com.intercon.expubsub;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intercon.expubsub.model.Moeda;
import com.intercon.expubsub.service.FakeHttpService;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LineChart chart;

    private TextView txtBitcoin;
    private TextView txtEthereum;
    private TextView txtRipple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new FakeHttpService();

        chart = findViewById(R.id.chart);

        txtBitcoin = findViewById(R.id.txtBitcoin);
        txtEthereum = findViewById(R.id.txtEthereum);
        txtRipple = findViewById(R.id.txtRipple);

        chart.setDrawGridBackground(true);
        chart.getDescription().setEnabled(false);
        chart.setDrawBorders(true);

        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setDrawAxisLine(true);
        chart.getAxisRight().setDrawGridLines(true);
        chart.getXAxis().setDrawAxisLine(true);
        chart.getXAxis().setDrawGridLines(true);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        //drawGraph(fakeHttpService.getMoedas());

        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey("sub-c-c5dee452-6049-11e7-b272-02ee2ddab7fe");
        pnConfiguration.setPublishKey("pub-c-128c8884-d4b7-4c72-bfd5-273a0a73c35c");

        PubNub pubnub = new PubNub(pnConfiguration);

        String channelName = "channelPubSub";

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                List<Moeda> moedas = new ArrayList<>();
                JsonArray array = message.getMessage().getAsJsonArray();
                for (int i = 0; i < 3; i++){
                    Moeda moeda = new Gson().fromJson(array.get(i).toString(), Moeda.class);
                    moedas.add(moeda);
                }

                drawGraph(moedas);
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {}
        });

        pubnub.subscribe().channels(Arrays.asList(channelName)).execute();
    }

    public void drawGraph(final List<Moeda> moedas){
        chart.resetTracking();

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

        for (int z = 0; z < 3; z++) {
            Moeda moeda = moedas.get(z);

            ArrayList<Entry> values = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                double val = moeda.preco[i];
                values.add(new Entry(i, (float) val));
            }

            LineDataSet d = new LineDataSet(values, moeda.code);
            d.setLineWidth(2.5f);
            d.setCircleRadius(4f);

            int color = colors[z];
            d.setColor(color);
            d.setCircleColor(color);
            dataSets.add(d);
        }

        final LineData data = new LineData(dataSets);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtBitcoin.setText("Bitcoin: R$" + moedas.get(0).preco[9]);
                txtEthereum.setText("Ethereum: R$" + moedas.get(1).preco[9]);
                txtRipple.setText("Ripple: R$" + moedas.get(2).preco[9]);

                chart.setData(data);
                chart.invalidate();
            }
        });
    }

    private final int[] colors = new int[] {
            ColorTemplate.VORDIPLOM_COLORS[0],
            ColorTemplate.VORDIPLOM_COLORS[1],
            ColorTemplate.VORDIPLOM_COLORS[2]
    };

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(List<Moeda> event) {
        drawGraph(event);
    };
}
