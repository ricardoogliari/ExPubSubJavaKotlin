package com.intercon.expubsub.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.Random;

/**
 * Created by ricardoogliari on 10/27/18.
 */

public class Moeda {

    public String code;
    public String nome;
    public float[] preco = new float[10];

    public Moeda(String code, float preco, String nome) {
        this.code = code;
        this.nome = nome;
        this.preco[0] = preco;

        for (int i = 1; i < 10; i++){
            Random random = new Random();
            this.preco[i] = this.preco[i - 1] + (-40 + new Random().nextInt(81));
        }

    }
}
