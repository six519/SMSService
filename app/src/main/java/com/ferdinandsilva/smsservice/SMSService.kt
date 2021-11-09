package com.ferdinandsilva.smsservice

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.Executors

class SMSService : Service() {
    private val tag: String = "SMS_SERVICE_TAG"
    private val serverPort: Int = 8080
    private var httpServer: HttpServer? = null
    private val smsServiceText: String =
        """

  _____ ___ ___  _____                        
 / ___/|   T   T/ ___/                        
(   \_ | _   _ (   \_                         
 \__  T|  \_/  |\__  T                        
 /  \ ||   |   |/  \ |                        
 \    ||   |   |\    |                        
  \___jl___j___j \___j                        
                                              
  _____   ___  ____  __ __  ____    __    ___ 
 / ___/  /  _]|    \|  T  |l    j  /  ]  /  _]
(   \_  /  [_ |  D  )  |  | |  T  /  /  /  [_ 
 \__  TY    _]|    /|  |  | |  | /  /  Y    _]
 /  \ ||   [_ |    \l  :  ! |  |/   \_ |   [_ 
 \    ||     T|  .  Y\   /  j  l\     ||     T
  \___jl_____jl__j\_j \_/  |____j\____jl_____j
                                              

        """.trimIndent()
    private val indexPage = HttpHandler { httpExchange ->
        run {
            when (httpExchange.requestMethod) {
                "GET" -> {
                    writeResponse(httpExchange, smsServiceText)
                }
            }
        }
    }

    private fun returnDetailResponse(httpExchange: HttpExchange, detail: String) {
        val jsonObject: JSONObject = JSONObject()
        jsonObject.put("detail", detail)
        writeResponse(httpExchange, jsonObject.toString())
    }

    private val sendPage = HttpHandler { httpExchange ->
        run {
            when (httpExchange.requestMethod) {
                "GET" -> {
                    returnDetailResponse(httpExchange, "Invalid request...")
                }
                "POST" -> {
                    val inputStream = httpExchange.requestBody
                    val stringStream = inputStreamToString(inputStream)
                    val jsonRequest = JSONObject(stringStream)
                    val recipient = jsonRequest.get("recipient").toString()
                    val message = jsonRequest.get("message").toString()

                    val smsManager: SmsManager = SmsManager.getDefault()
                    val messageSentIntent = Intent("sent")
                    val pendingIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, messageSentIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                    smsManager.sendTextMessage(recipient, null, message, pendingIntent, null)
                    returnDetailResponse(httpExchange, "Message sent to ${recipient}...")
                }
            }
        }
    }

    private val readPage = HttpHandler { httpExchange ->
        run {
            when (httpExchange.requestMethod) {
                "GET" -> {
                    val cursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null)
                    val jsonArray: JSONArray = JSONArray()
                    if(cursor != null) {
                        if (cursor.moveToFirst()) {
                            do {
                                //2 - Number
                                //12 - Message
                                val jsonObject: JSONObject = JSONObject()
                                jsonObject.put("sender", cursor.getString(2))
                                jsonObject.put("message", cursor.getString(12))
                                jsonArray.put(jsonObject)
                                //break
                            } while (cursor.moveToNext())
                        }
                    }
                    writeResponse(httpExchange, jsonArray.toString())
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(tag, "Starting SMS Service...")

        httpServer = HttpServer.create(InetSocketAddress(serverPort), 0)
        httpServer.run {
            this?.executor = Executors.newCachedThreadPool()
            this?.createContext("/", indexPage)
            this?.createContext("/send", sendPage)
            this?.createContext("/read", readPage)
            this?.start()
            Log.d(tag, "SMS Service Started...")
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun writeResponse(httpExchange: HttpExchange, responseText: String) {
        httpExchange.sendResponseHeaders(200, responseText.length.toLong())
        val outputStream = httpExchange.responseBody
        outputStream.write(responseText.toByteArray())
        outputStream.close()
    }

    private fun inputStreamToString(inputStream: InputStream): String {
        val scanner = Scanner(inputStream).useDelimiter("\\A")
        return if (scanner.hasNext()) scanner.next() else ""
    }
}