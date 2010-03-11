package cn.yo2.aquarium.pocketvoa;

import java.util.Locale;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.webkit.WebView;

public class Help extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Utils.setKeepScreenOn(this);
		
		setContentView(R.layout.help);
		
		WebView webView = (WebView)findViewById(R.id.webview);
		
		Locale locale = getResources().getConfiguration().locale;

		String url = Locale.SIMPLIFIED_CHINESE.equals(locale) ? "file:///android_asset/help_zh_CN.htm"
				: "file:///android_asset/help.htm";
		
		webView.loadUrl(url);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}
}
