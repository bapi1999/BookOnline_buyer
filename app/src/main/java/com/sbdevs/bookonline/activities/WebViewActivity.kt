package com.sbdevs.bookonline.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {
    private lateinit var wevView:WebView
    private lateinit var binding:ActivityWebViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wevView = binding.webView
        wevView.webViewClient = WebViewClient()
        wevView.loadUrl("https://www.sbdevs.co.in/tarm-and-condition")
    }
}