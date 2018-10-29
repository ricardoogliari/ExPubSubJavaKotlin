package com.intercon.expubsub.service;

/**
 * Created by ricardoogliari on 10/27/18.
 */

import android.os.Handler;
import android.util.Log;

import com.intercon.expubsub.model.Moeda;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ricardoogliari on 10/27/18.
 */

public class FakeHttpService implements Runnable{

    private Handler handler;

    private static List<Moeda> moedas = new ArrayList<>();

    private PubNub pubnub;

    public List<Moeda> getMoedas(){
        return moedas;
    }

    public FakeHttpService(){
        handler = new Handler();

        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey("sub-c-c5dee452-6049-11e7-b272-02ee2ddab7fe");
        pnConfiguration.setPublishKey("pub-c-128c8884-d4b7-4c72-bfd5-273a0a73c35c");

        pubnub = new PubNub(pnConfiguration);

        handler.postDelayed(this, 500);
    }

    @Override
    public void run() {
        Moeda bitcoin = new Moeda("BTC", moedas.size() == 0 ? 327.12f : moedas.get(0).preco[9], "Bitcoin");
        Moeda ethereum = new Moeda("ETH", moedas.size() == 0 ? 212 : moedas.get(1).preco[9], "Ethereum");
        Moeda ripple = new Moeda("XRP", moedas.size() == 0 ? 159.50f : moedas.get(2).preco[9], "Ripple");

        moedas.clear();
        moedas.add(bitcoin);
        moedas.add(ethereum);
        moedas.add(ripple);

        //EventBus.getDefault().post(moedas);
        pubnub.publish()
                .message(moedas)
                .channel("channelPubSub")
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) { }
                });

        handler.postDelayed(this, 3500);
    }
}
