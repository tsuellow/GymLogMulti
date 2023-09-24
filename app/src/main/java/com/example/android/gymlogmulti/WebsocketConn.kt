package com.example.android.gymlogmulti


import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.example.android.gymlogmulti.data.ClientEntry
import com.example.android.gymlogmulti.data.GymDatabase
import com.example.android.gymlogmulti.data.PaymentEntry
import com.example.android.gymlogmulti.utils.PhoneUtilities
import com.example.android.gymlogmulti.utils.PhotoUtils
import com.example.android.gymlogmulti.utils.QrCodeUtilities
import com.fasterxml.jackson.databind.ObjectMapper
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URISyntaxException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class WebSocketConn(url:String, receptionists:String, val context:Context) {
 lateinit var mSocket:Socket
 val resources: Resources =context.resources
 val mDB:GymDatabase= GymDatabase.getInstance(context)
 val objectMapper=ObjectMapper()
    init{
     try {
         val opts = IO.Options()
         opts.forceNew = true
         opts.query = "gymId=${Constants.GYM_ID}&branch=${Constants.GYM_BRANCH}"
         opts.extraHeaders= hashMapOf("receptionists" to listOf<String>(receptionists))
         mSocket = IO.socket(url, opts)
         Log.d("socketTest", "success")
     } catch (e: URISyntaxException) {
         e.printStackTrace()
         Log.d("socketTest", "failed")
     }
    }

    val onRequestClientInfo = Emitter.Listener { args ->
        Log.d("socketTest", "requestclientinfo " + args[0].toString())
        var json:JSONObject?=null
        try {
            json=JSONObject(args[0].toString())
        } catch (e: Exception) {
            Log.d("Error", e.toString())
        }
        json?.let{requestClientInfo(it)}

    }

    private fun requestClientInfo(json: JSONObject) {
        val res = JSONObject()
        val id = json.getInt("id")
        res.put("id", id)
        if (mDB.clientDao().isIdNew(id) == 1) {
            res.put("isNew", true)
        } else {
            val products:List<String> = if (Constants.customProducts) resources.getStringArray(R.array.product_array2).toCollection(ArrayList()) else resources.getStringArray(R.array.product_array).toCollection(ArrayList())
            val prodsArray = JSONArray()
            for (prod in products) {
                prodsArray.put(prod)
            }
            val client: ClientEntry? = mDB.clientDao().getClientByIdAsync(id)
            val lastPayment: PaymentEntry? = mDB.paymentDao().getLastPaymentByClientAsync(id)
            res.put("isNew", false)
            res.put("clientData", JSONObject(objectMapper.writeValueAsString(client)))
            if (lastPayment==null){
                res.put("lastPayment", "null")
            }else{
                res.put("lastPayment", JSONObject(objectMapper.writeValueAsString(lastPayment)))
            }
            res.put("products",prodsArray)

        }
        mSocket.emit("clientInfo", res.toString())
        Log.d("socketTest", res.toString())
    }

    val onPaymentReceived = Emitter.Listener { args ->
        Log.d("socketTest", "size " + args[0].toString())
        var res:JSONObject
        try {
            val json=JSONObject(args[0].toString())
            val payment:PaymentEntry=objectMapper.readValue(json.toString(),PaymentEntry::class.java)
            res=insertNewPayment(payment)
            Log.d("socketTest", json.toString())
            val daysLeft = TimeUnit.DAYS.convert(
                payment.getPaidUntil().getTime() - Date().getTime(),
                TimeUnit.MILLISECONDS
            ) + 1
            CoroutineScope(Dispatchers.Main).launch{
                Toast.makeText(context, "Nuevo pago de ID:${payment.clientId},\nmonto en USD: ${payment.amountUsd},\ndias de acceso: $daysLeft",Toast.LENGTH_LONG).show()
            }
            //Toast.makeText(context, "New payment for ID:${payment.clientId}, cost:${payment.amountUsd}, du",Toast.LENGTH_SHORT).show()
            //parse json, check for client existence and insert payment
        } catch (e: Exception) {
            res= JSONObject()
            res.put("status", "FAIL")
            Log.d("Error", e.toString())
        }
        mSocket.emit("payment", res.toString())
    }

    private fun insertNewPayment(payment:PaymentEntry?):JSONObject{
        val res= JSONObject()
        res.put("id",payment?.id)
        var status:String="FAIL;"
        payment?.let {
            mDB.paymentDao().insertPayment(it)
            status="OK"
        }
        res.put("status",status)
        return res
    }

    val onNewClient= Emitter.Listener { args ->
        Log.d("socketTest", "size " + args[0].toString())
        val res=JSONObject()
        try {
            val json=JSONObject(args[0].toString())
            //decode and save client photo
            val client:ClientEntry=objectMapper.readValue(json.getJSONObject("clientData").toString(),ClientEntry::class.java)
            val bitarray=Base64.decode(json.getString("photo"),0)
            val bitmapDecoded = BitmapFactory.decodeByteArray(bitarray, 0, bitarray.size)
            val base64=PhotoUtils.saveAndGenerateBase64(bitmapDecoded,client.id,context)
            //get and insert client data
            client.photo=base64
            mDB.clientDao().insertClient(client)
            client.phone?.let{
                NewClientActivity.addContact(client.firstName+" "+client.lastName[0]+". ID:"+client.id,PhoneUtilities.depuratePhone(client.phone),context)
            }
            //display qr code dialog
            QrCodeUtilities.saveQrCodeNew(client.id,context)
            CoroutineScope(Dispatchers.Main).launch{
                QrCodeUtilities.displayQrDialog(context,client).show()
            }
            res.put("id",client.id)
            res.put("status","OK")
        } catch (e: Exception) {
            Log.d("socketTest", e.toString())
            res.put("id",0)
            res.put("status","FAIL")
        }
        mSocket.emit("createClient",res.toString())
    }


    fun openSocket(){
        mSocket.on("clientInfo",onRequestClientInfo)
        mSocket.on("payment",onPaymentReceived)
        mSocket.on("createClient", onNewClient)
        mSocket.connect()
        Log.d("socketTest", "is connected"+mSocket.connected())
    }

    fun closeSocket(){
        mSocket.off()
        mSocket.disconnect()
    }

}