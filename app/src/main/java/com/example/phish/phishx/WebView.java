package com.example.phish.phishx;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

public class WebView extends AppCompatActivity implements View.OnClickListener {

    private static String url1;
    private static String url2 = null;
    /**
     * A default URL to open if the URL is empty.
     */
    private static final String DEFAULT_URL = "http://www.google.com";
    private static final String MODE_HTTP = "http://";
    private static final String MODE_HTTPS = "https://";
    /**
     * The URL for performing a google search
     */
    private static final String GOOGLE_SEARCH = "https://www.google.com/search?q=";

    private EditText search_url;
    private ImageButton search_btn;
    private ProgressBar superProgressBar;

    private android.webkit.WebView webView;
    private ViewTreeObserver.OnScrollChangedListener scrollChangedListener;

    SwipeRefreshLayout swipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        search_url = findViewById(R.id.search_url);
        search_btn = findViewById(R.id.search_btn);
        search_btn.setOnClickListener(this);
        swipe = findViewById(R.id.swipe);

        superProgressBar = findViewById(R.id.myProgresBar);
        superProgressBar.setMax(100);

        String message = null;

        Intent intent = getIntent();
        message = intent.getStringExtra(HomeActivity.EXTRA_MESSAGE);


        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initWebView();

                try {

                    openWebsite(getURLPassedToActivity());

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
        initWebView();

        if(message == null)
        {

                    openWebsite(getURLPassedToActivity());

        }
        else
            openWebsite(message);

    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }

    }

    @Override
    public void onClick(View view) {
        if (view == search_btn) {
            if (TextUtils.isEmpty(search_url.getText().toString()))
                Toast.makeText(this, "Please Enter URL or Text to Search", Toast.LENGTH_LONG).show();
            else
                openWebsite(search_url.getText().toString());
        }

    }

    /**
     * A method to handle initialisation of the WebView and its settings.
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        webView = findViewById(R.id.webview);
        //webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(android.webkit.WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                search_url.setText(url);

                swipe.getViewTreeObserver().addOnScrollChangedListener(scrollChangedListener =
                        new ViewTreeObserver.OnScrollChangedListener() {
                            @Override
                            public void onScrollChanged() {
                                if(webView.getScrollY() == 0)
                                    swipe.setEnabled(true);
                                else
                                    swipe.setEnabled(false);
                            }
                        });
            }

            @Override
            public void onPageFinished(android.webkit.WebView view, String url) {
                super.onPageFinished(view, url);
                swipe.setRefreshing(false);
                /*String username = "qwertyui";
                String email = "asdfghjkqw@gmail.com";
                String pass = "asdfghjklqwer";
                String js = "javascript:( function() { "+
                        "document.getElementById('username').value = '"+username+"'; "+
                        "document.getElementById('email').value = '"+email+"'; "+
                        "document.getElementById('password').value = '"+pass+"';"+
                        "document.getElementById('send').click(); })();";
                System.out.println(url);
                String url1 = url;
                webView.loadUrl(js);*/

            }

        });
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(android.webkit.WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                superProgressBar.setProgress(newProgress);
            }

            @Override
            public void onReceivedTitle(android.webkit.WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (getSupportActionBar() == null)
                    return;
                getSupportActionBar().setTitle("PhishX | "+title);
            }

        });
    }

    /**
     * This method handles the logic of fetching the URL passed to this activity.
     * This is required to be handled in order to get the URL that the user clicked and opened the app.
     * @return the URL passed or null
     */
    @Nullable
    private String getURLPassedToActivity() {

        Intent intent = getIntent();

        Uri data = intent.getData();
        URL url = null;

        try {
            url = new URL(data.getScheme(),
                    data.getHost(),
                    data.getPath());

        } catch (Exception e) {
            e.printStackTrace();
        }

        search_url.setText(url.toString());
       /*if (getIntent() == null || getIntent().getData() == null)
            return null;

        return getIntent().getData().toString();*/
       if(url==null)
           return null;
       else
           return url.toString();
    }


    /**
     * This method handles the logic of loading the website.
     * If the url is a string then It loads the google search and If it is a valid URL then loads it.
     * @param url a URL or the search string.
     */

    private void openWebsite(@Nullable final String url) {

        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        //startActivity(intent);
        String url1="https://checkurl.phishtank.com/checkurl/index.php?url="+url+"&format=json&api_key=88b7197aa0e0143c750de1a8e5b5ccc86badba3130e80f9dc52df6df65216e82";

        Log.d("WebView", "Opening URL: " + url1);
        if (TextUtils.isEmpty(url1)) {
            webView.loadUrl(DEFAULT_URL);
        }
        else {
            if (Patterns.WEB_URL.matcher(url1).matches()) {
                if (url1.startsWith(MODE_HTTP) || url1.startsWith(MODE_HTTPS)) {
                    search_url.setText(url1);
                    webView.loadUrl(url1);
                }
                else {
                    search_url.setText(MODE_HTTP+url1);
                    webView.loadUrl(MODE_HTTP + url1);
                }
            } else
                webView.loadUrl(GOOGLE_SEARCH + url1.trim());
        }


    }


}
