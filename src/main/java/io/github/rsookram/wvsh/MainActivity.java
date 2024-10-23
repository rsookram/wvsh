package io.github.rsookram.wvsh;

import android.app.Activity;
import android.graphics.Insets;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowInsets;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class MainActivity extends Activity {

    private static final String DEFAULT_DOMAIN = "appassets.androidplatform.net";

    private static final int CHOOSE_FILE_REQUEST_CODE = 10;

    private ValueCallback<Uri[]> filePathCallback;
    private WebView webView;

    private final OnBackInvokedCallback onBackInvokedCallback = new OnBackInvokedCallback() {
        @Override
        public void onBackInvoked() {
            webView.goBack();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        findViewById(android.R.id.content).setOnApplyWindowInsetsListener((v, insets) -> {
            Insets systemInsets = insets.getInsets(WindowInsets.Type.systemBars());
            v.setPadding(systemInsets.left, systemInsets.top, systemInsets.right, systemInsets.bottom);
            return insets;
        });

        this.webView = findViewById(R.id.webview);

        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);

        AssetManager assets = getAssets();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Uri url = request.getUrl();
                if (url == null) {
                    return null;
                }
                String path = url.getPath();
                if (path == null) {
                    return null;
                }

                String suffix = path.replaceFirst(DEFAULT_DOMAIN, "").substring("/".length());
                try {
                    InputStream is = assets.open(suffix, AssetManager.ACCESS_STREAMING);
                    String mime = URLConnection.guessContentTypeFromName(suffix);
                    return new WebResourceResponse(mime, null, is);
                } catch (IOException e) {
                    return new WebResourceResponse(null, null, null);
                }
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                if (view.canGoBack()) {
                    getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                            OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                            onBackInvokedCallback
                    );
                } else {
                    getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(onBackInvokedCallback);
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams
            ) {
                if (MainActivity.this.filePathCallback != null) {
                    MainActivity.this.filePathCallback.onReceiveValue(null);
                }
                MainActivity.this.filePathCallback = filePathCallback;

                startActivityForResult(fileChooserParams.createIntent(), CHOOSE_FILE_REQUEST_CODE);

                return true;
            }
        });

        webView.loadUrl("https://appassets.androidplatform.net/index.html");
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.destroy();
        filePathCallback = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == CHOOSE_FILE_REQUEST_CODE && filePathCallback != null) {
            filePathCallback.onReceiveValue(new Uri[]{data.getData()});
            filePathCallback = null;
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
