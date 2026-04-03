package com.pixiepath.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.GeolocationPermissions
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION")
@android.annotation.SuppressLint("SetJavaScriptEnabled", "AllowUniversalAccessFromFileURLs")
class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var fileCallback: ValueCallback<Array<Uri>>? = null
    private var cameraPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val perms = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.ACCESS_FINE_LOCATION)
            perms.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.CAMERA)
        }
        if (perms.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, perms.toTypedArray(), 1001)
        }

        webView = findViewById(R.id.webView)

        val ws: WebSettings = webView.settings
        ws.javaScriptEnabled = true
        ws.domStorageEnabled = true
        ws.allowFileAccess = true
        ws.loadsImagesAutomatically = true
        ws.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        ws.setSupportZoom(false)
        ws.builtInZoomControls = false
        ws.useWideViewPort = true
        ws.loadWithOverviewMode = true
        ws.setGeolocationEnabled(true)
        ws.allowUniversalAccessFromFileURLs = true
        ws.allowFileAccessFromFileURLs = true

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = object : WebChromeClient() {

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                callback?.invoke(origin, true, false)
            }

            override fun onShowFileChooser(
                view: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileCallback?.onReceiveValue(null)
                fileCallback = filePathCallback

                val types = fileChooserParams?.acceptTypes
                val wantsImage = types != null && types.any {
                    it != null && it.contains("image")
                }

                if (wantsImage) {
                    launchCamera()
                } else {
                    try {
                        val intent = fileChooserParams?.createIntent()
                        if (intent != null) {
                            startActivityForResult(intent, 1002)
                        } else {
                            fileCallback?.onReceiveValue(null)
                            fileCallback = null
                        }
                    } catch (e: Exception) {
                        fileCallback?.onReceiveValue(null)
                        fileCallback = null
                        return false
                    }
                }
                return true
            }
        }

        webView.loadUrl("file:///android_asset/index.html")
    }

    private fun launchCamera() {
        try {
            val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File.createTempFile("PIXIE_${ts}_", ".jpg", dir)
            val uri = FileProvider.getUriForFile(this, "com.pixiepath.app.fileprovider", file)
            cameraPhotoUri = uri

            val camIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            camIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            camIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(camIntent, 1002)
        } catch (e: Exception) {
            fileCallback?.onReceiveValue(null)
            fileCallback = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1002) {
            if (resultCode == Activity.RESULT_OK) {
                val uri = data?.data ?: cameraPhotoUri
                if (uri != null) {
                    fileCallback?.onReceiveValue(arrayOf(uri))
                } else {
                    fileCallback?.onReceiveValue(null)
                }
            } else {
                fileCallback?.onReceiveValue(null)
            }
            fileCallback = null
            cameraPhotoUri = null
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
