
package com.hanvon.hwepen;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

public class HelpActivity extends Activity implements OnClickListener {

    private WebView mWebView;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_help);
        initView();
    }

    private void initView()
    {
        ((ImageView) findViewById(R.id.iv_help_backbtn)).setOnClickListener(this);
        
        ((TextView) findViewById(R.id.tv_title_help)).setText(R.string.title_help);
        
        mWebView = (WebView) findViewById(R.id.webView);
        
        mWebView.loadUrl("file:///android_asset/help.htm");
    }

    @Override
    public void onClick(View v) 
    {
        switch (v.getId()) 
        {
            case R.id.iv_help_backbtn:
                finish();
            break;

            default:
            break;
        }
    }
}
