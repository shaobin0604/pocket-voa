package cn.yo2.aquarium.pocketvoa;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class HelpActivity extends Activity {
	
	private static final String HELP_FILE_NAME = "help.html";
	private static final String ASSET_DIR = "file:///android_asset/";
	private static final String TAG = HelpActivity.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.help);
		
		WebView webView = (WebView)findViewById(R.id.webview);
		
		String filename = Utils.getLocaleName(this, HELP_FILE_NAME);

		String url = ASSET_DIR + filename;
		
		Log.d(TAG, "[onCreate] url -- " + url);
		
		webView.loadUrl(url);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}
}
