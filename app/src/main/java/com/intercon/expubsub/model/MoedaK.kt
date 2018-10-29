package com.intercon.expubsub.model

import java.util.*

/**
 * Created by ricardoogliari on 10/29/18.
 */
data class MoedaK(var code: String, var nome: String) {

    var preco = FloatArray(10)

    fun init(preco: Float) {
        this.preco[0] = preco

        for (i in 1..9) {
            this.preco[i] = this.preco[i - 1] + (-40 + Random().nextInt(81))
        }

    }

}