package com.intercon.expubsub

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.gson.Gson
import com.intercon.expubsub.model.Moeda
import com.intercon.expubsub.service.FakeHttpServiceK
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * Created by ricardoogliari on 10/29/18.
 */
class MainActivityK : AppCompatActivity() {

    val subKey = "sub-c-c5dee452-6049-11e7-b272-02ee2ddab7fe"
    val pubKey = "pub-c-128c8884-d4b7-4c72-bfd5-273a0a73c35c"
    val channel = "channelPubSub"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FakeHttpServiceK()

        chart.setDrawGridBackground(true)
        chart.description.isEnabled = false
        chart.setDrawBorders(true)

        chart.axisLeft.isEnabled = false
        chart.axisRight.setDrawAxisLine(true)
        chart.axisRight.setDrawGridLines(true)
        chart.xAxis.setDrawAxisLine(true)
        chart.xAxis.setDrawGridLines(true)

        // enable touch gestures
        chart.setTouchEnabled(true)

        // enable scaling and dragging
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false)

        val l = chart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)

        val pnConfiguration = PNConfiguration()
        pnConfiguration.subscribeKey = subKey
        pnConfiguration.publishKey = pubKey

        val pubnub = PubNub(pnConfiguration)

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, status: PNStatus) {}

            override fun message(pubnub: PubNub, message: PNMessageResult) {
                val moedas = ArrayList<Moeda>()
                val array = message.message.asJsonArray
                for (i in 0..2) {
                    val moeda = Gson().fromJson(array.get(i).toString(), Moeda::class.java)
                    moedas.add(moeda)
                }

                drawGraph(moedas)
            }

            override fun presence(pubnub: PubNub, presence: PNPresenceEventResult) {}
        })

        pubnub.subscribe().channels(Arrays.asList(channel)).execute()
    }

    fun drawGraph(moedas: List<Moeda>) {
        chart.resetTracking()

        val dataSets = ArrayList<ILineDataSet>()

        for (z in 0..2) {
            val moeda = moedas[z]

            val values = ArrayList<Entry>()

            for (i in 0..9) {
                values.add(Entry(i.toFloat(), moeda.preco[i].toFloat()))
            }

            val d = LineDataSet(values, moeda.code)
            d.lineWidth = 2.5f
            d.circleRadius = 4f

            val color = colors[z]
            d.color = color
            d.setCircleColor(color)
            dataSets.add(d)
        }

        val data = LineData(dataSets)

        runOnUiThread {
            txtBitcoin.text = "Bitcoin: R$" + moedas[0].preco[9]
            txtEthereum.text = "Ethereum: R$" + moedas[1].preco[9]
            txtRipple.text = "Ripple: R$" + moedas[2].preco[9]

            chart.data = data
            chart.invalidate()
        }
    }

    private val colors = intArrayOf(ColorTemplate.VORDIPLOM_COLORS[0], ColorTemplate.VORDIPLOM_COLORS[1], ColorTemplate.VORDIPLOM_COLORS[2])

    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: List<Moeda>) {
        drawGraph(event)
    };
}