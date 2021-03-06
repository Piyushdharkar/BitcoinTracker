package com.example.bitcointracker

import android.content.res.Configuration
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class MainActivity : AppCompatActivity() {

    companion object {
        val url = "https://blockchain.info/ticker"
        val timeDuration = 300000.toLong()
    }

    private val currencyList = HashMap<String, CurrencyData>()
    private var currencySelected = "USD"
    private val currencies: ArrayList<String> = arrayListOf("USD", "AUD", "BRL", "CAD", "CHF", "CLP", "CNY", "DKK", "EUR", "GBP", "HKD", "INR", "ISK", "JPY", "KRW", "NZD", "PLN", "RUB", "SEK", "SGD", "THB", "TWD")

    private val handler = Handler()
    private val runnable = object : Runnable {
        override fun run() {
            try {
                DownloadTask().execute(url)
                handler.postDelayed(this, timeDuration)
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "An error has occurred", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            supportActionBar?.let { supportActionBar?.hide() }
            enterPictureInPictureMode()
        }

        spinner.adapter = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, currencies)

        spinner.setSelection(0)

        runnable.run()

        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                updateTextViews(spinner.selectedItem.toString())
            }
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

        if (isInPictureInPictureMode) {
            button.animate().alpha(0.0f).duration = 0
            buyTextView.textSize = 20f
            sellTextView.textSize = 20f
        } else {
            buyTextView.textSize = 40f
            sellTextView.textSize = 40f
            supportActionBar?.let { supportActionBar?.show() }
            button.animate().alpha(1.0f).duration = 3000

        }
    }

    fun updateTextViews(currencySelected: String) {
        try {
            val buyPrice = currencyList[currencySelected]?.buy ?: throw Exception()
            val sellPrice = currencyList[currencySelected]?.sell ?: throw Exception()
            val currencySymbol = currencyList[currencySelected]?.symbol ?: throw Exception()

            buyTextView.text = "$buyPrice $currencySymbol"
            sellTextView.text = "$sellPrice $currencySymbol"

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class DownloadTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg urls: String): String {
            val url = URL(urls[0])
            try {
                val httpURLConnection: HttpURLConnection = url.openConnection() as? HttpURLConnection ?: throw MalformedURLException()
                val inputStreamReader = InputStreamReader(httpURLConnection.inputStream)

                var data: Int = inputStreamReader.read()
                var result = ""

                while (data != -1) {
                    result += data.toChar()
                    data = inputStreamReader.read()
                }

                return result
            } catch (malformedURLException: MalformedURLException) {
                Log.i("Exception", "Error in opening connection")
            } catch (e: Exception) {
                e.printStackTrace()
            }

            throw Exception()
        }

        override fun onPostExecute(result: String) {

            val jsonObject = JSONObject(result)

            for (currency in currencies) {
                val currencyType = jsonObject.getJSONObject(currency)
                val buyPrice = currencyType.getDouble("buy")
                val sellPrice = currencyType.getDouble("sell")
                val currencySymbol = currencyType.getString("symbol")
                currencyList[currency] = CurrencyData(buy = buyPrice, sell = sellPrice, symbol = currencySymbol)
            }

            currencySelected = currencies[spinner.selectedItemId.toInt()]

            updateTextViews(currencySelected)
        }
    }
}
