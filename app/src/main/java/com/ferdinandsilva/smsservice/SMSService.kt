package com.ferdinandsilva.smsservice

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.io.OutputStream
import java.net.InetSocketAddress
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
    private val index = HttpHandler { httpExchange ->
        run {
            when (httpExchange.requestMethod) {
                "GET" -> {
                    writeResponse(httpExchange, smsServiceText)
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
        httpServer!!.executor = Executors.newCachedThreadPool()
        httpServer!!.createContext("/", index)
        httpServer!!.start()
        Log.d(tag, "SMS Service Started...")
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
}