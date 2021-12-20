package com.example.android.gymlogmulti


import android.content.Context
import android.util.Log
import com.example.android.gymlogmulti.data.PaymentEntry
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.lang.Exception
import java.net.URISyntaxException

class WebSocketConn(url:String, branch:String, context:Context) {
 lateinit var mSocket:Socket

    val objectMapper=ObjectMapper()
    init{
     try {
         val opts = IO.Options()
         opts.forceNew = true
         opts.query = "branch=$branch"
         mSocket = IO.socket(url, opts)
         Log.d("socketTest", "success")
     } catch (e: URISyntaxException) {
         e.printStackTrace()
         Log.d("socketTest", "failed")
     }
    }

    val onPaymentReceived = Emitter.Listener { args ->
        Log.d("socketTest", "size " + args[0].toString())
        try {
            val json=JSONObject(args[0].toString())
            //val payment:PaymentEntry=objectMapper.readValue(json.toString(),PaymentEntry::class.java)
            Log.d("socketTest", json.toString())
            //parse json, check for client existence and insert payment
        } catch (e: Exception) {
            Log.d("Error", e.toString())
        }
    }

    val onNewClientAndPaymentReceived = Emitter.Listener { args ->
        Log.d("socketTest", "size " + args[0].toString())
        try {
            //parse json, insert client and payment sequentially
        } catch (e: Exception) {
            Log.d("Error", e.toString())
        }
    }


    fun openSocket(){
        mSocket.on("payment",onPaymentReceived)
        mSocket.on("createAndPayment", onNewClientAndPaymentReceived)
        mSocket.connect()
        Log.d("socketTest", "is connected"+mSocket.connected())
    }

    fun closeSocket(){
        mSocket!!.off()
        mSocket!!.disconnect()
    }

}