package com.example.bitcointracker

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class MainActivity : AppCompatActivity() {

    companion object {
        val url = "https://blockchain.info/ticker"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            DownloadTask().execute(url)
        } catch (e: Exception) {
            Toast.makeText(this, "An error has occurred", Toast.LENGTH_LONG).show()
        }
    }

    fun updateTextViews(buyPrice: Double, sellPrice: Double) {
        buyTextView.text = buyPrice.toString()
        sellTextView.text = sellPrice.toString()
    }

    inner class DownloadTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg urls: String): String {
            val url = URL(urls[0])
            try {
                val httpURLConnection: HttpURLConnection = url.openConnection() as? HttpURLConnection ?: throw MalformedURLException()
                val inputStream = httpURLConnection.inputStream
                val inputStreamReader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                val list = bufferedReader.readLines()

                return list.joinToString()
            } catch (malformedURLException: MalformedURLException) {
                Log.i("Exception", "Error in opening connection")
            } catch (e: Exception) {
                e.printStackTrace()
            }

            throw Exception()
        }

        override fun onPostExecute(result: String) {
            val jsonObject = JSONObject(result)
            val usdRate = jsonObject.getJSONObject("USD")
            val buyPrice = usdRate.getDouble("buy")
            val sellPrice = usdRate.getDouble("sell")

            updateTextViews(buyPrice, sellPrice)
        }
    }
}
