package com.intercon.expubsub.service

import android.os.Handler
import com.intercon.expubsub.model.Moeda
import com.intercon.expubsub.model.MoedaK
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.PNCallback
import com.pubnub.api.models.consumer.PNPublishResult
import com.pubnub.api.models.consumer.PNStatus
import java.util.ArrayList

/**
 * Created by ricardoogliari on 10/29/18.
 */
class FakeHttpServiceK : Runnable {

    private val handler: Handler

    private val pubnub: PubNub

    fun getMoedas(): List<MoedaK> {
        return moedas
    }

    init {
        handler = Handler()

        val pnConfiguration = PNConfiguration()
        pnConfiguration.subscribeKey = "sub-c-c5dee452-6049-11e7-b272-02ee2ddab7fe"
        pnConfiguration.publishKey = "pub-c-128c8884-d4b7-4c72-bfd5-273a0a73c35c"

        pubnub = PubNub(pnConfiguration)

        handler.postDelayed(this, 500)
    }

    override fun run() {
        val bitcoin = MoedaK("BTC","Bitcoin")
        bitcoin.init(if (moedas.size == 0) 327.12f else moedas[0].preco[9])
        val ethereum = MoedaK("ETH", "Ethereum")
        ethereum.init(if (moedas.size == 0) 212.0f else moedas[1].preco[9])
        val ripple = MoedaK("XRP", "Ripple")
        ripple.init(if (moedas.size == 0) 159.50f else moedas[2].preco[9])

        moedas.clear()
        moedas.add(bitcoin)
        moedas.add(ethereum)
        moedas.add(ripple)

        //EventBus.getDefault().post(moedas);
        pubnub.publish()
                .message(moedas)
                .channel("channelPubSub")
                .async(object : PNCallback<PNPublishResult>() {
                    override fun onResponse(result: PNPublishResult, status: PNStatus) {}
                })

        handler.postDelayed(this, 3500)
    }

    companion object {
        private val moedas = ArrayList<MoedaK>()
    }
}
