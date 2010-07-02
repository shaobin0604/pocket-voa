package cn.yo2.aquarium.pocketvoa;

import java.io.IOException;

import org.acra.CrashReportingApplication;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import cn.yo2.aquarium.pocketvoa.parser.IDataSource;
import cn.yo2.aquarium.pocketvoa.parser.IListParser;

public class App extends CrashReportingApplication {
	private static final String CLASSTAG = App.class.getSimpleName();

	private static final int CONN_TIME_OUT = 1000 * 3; // millis
	private static final int READ_TIME_OUT = 1000 * 10; // millis
	private static final int MAX_TOTAL_CONN = 10;

	private static final String DEFAULT_CHARSET = "utf-8";

	public SharedPreferences mSharedPreferences;
	
	@Override
	public String getFormId() {
		
		return "dFRJZDI3QWN0bnN6emRBb2tnMWRwSEE6MQ";
	}
	
	@Override
	public Bundle getCrashResources() {
	    Bundle result = new Bundle();
	    result.putInt(RES_NOTIF_TICKER_TEXT, R.string.crash_notif_ticker_text);
	    result.putInt(RES_NOTIF_TITLE, R.string.crash_notif_title);
	    result.putInt(RES_NOTIF_TEXT, R.string.crash_notif_text);
	    result.putInt(RES_NOTIF_ICON, android.R.drawable.stat_notify_error); // optional. default is a warning sign
	    result.putInt(RES_DIALOG_TEXT, R.string.crash_dialog_text);
	    result.putInt(RES_DIALOG_ICON, android.R.drawable.ic_dialog_info); //optional. default is a warning sign
	    result.putInt(RES_DIALOG_TITLE, R.string.crash_dialog_title); // optional. default is your application name 
	    result.putInt(RES_DIALOG_COMMENT_PROMPT, R.string.crash_dialog_comment_prompt); // optional. when defined, adds a user text field input with this text resource as a label
	    result.putInt(RES_DIALOG_OK_TOAST, R.string.crash_dialog_ok_toast); // optional. Displays a Toast when the user accepts to send a report ("Thank you !" for example)
	    return result;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		// since version 1.1.0 remove multiple datasource support.
//		mDataSource = getDataSourceFromPrefs(mSharedPreferences);
		
		mDataSource = getDefaultDataSource();
		mDataSource.init(getMaxCountFromPrefs(mSharedPreferences));
	}

	public final DefaultHttpClient mHttpClient = setupHttpClient();

	public final ResponseHandler mResponseHandler = new ResponseHandler(
			DEFAULT_CHARSET);

	public final ListGenerator mListGenerator = new ListGenerator(
			mResponseHandler, mHttpClient);

	public final PageGenerator mPageGenerator = new PageGenerator(
			mResponseHandler, mHttpClient);

	public IDataSource mDataSource;

	public Integer getMaxCountFromPrefs(SharedPreferences sharedPreferences) {
		return Integer.valueOf(sharedPreferences.getString(
				getString(R.string.prefs_list_count_key), String
						.valueOf(IListParser.DEFAULT_MAX_COUNT)));
	}

	private IDataSource getDefaultDataSource() {
		return new cn.yo2.aquarium.pocketvoa.parser.voa51.DataSource();
	}

	public IDataSource getDataSourceFromPrefs(
			SharedPreferences sharedPreferences) {
		String datasourceStr = sharedPreferences.getString(
				getString(R.string.prefs_datasource_key), "");
//		Log.d(CLASSTAG, "datasource prefs -- " + datasourceStr);
		IDataSource dataSource = null;
		if (TextUtils.isEmpty(datasourceStr)) {
			dataSource = getDefaultDataSource();

		} else {
			try {
				dataSource = (IDataSource) Class.forName(datasourceStr)
						.newInstance();
			} catch (IllegalAccessException e) {
				Log.e(CLASSTAG, "Error in create DataSource", e);
				dataSource = getDefaultDataSource();
			} catch (InstantiationException e) {
				Log.e(CLASSTAG, "Error in create DataSource", e);
				dataSource = getDefaultDataSource();
			} catch (ClassNotFoundException e) {
				Log.e(CLASSTAG, "Error in create DataSource", e);
				dataSource = getDefaultDataSource();
			}
		}

		dataSource.init(getMaxCountFromPrefs(mSharedPreferences));

		return dataSource;
	}

	private DefaultHttpClient setupHttpClient() {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, CONN_TIME_OUT);
		HttpConnectionParams.setSoTimeout(params, READ_TIME_OUT);
		ConnManagerParams.setMaxTotalConnections(params, MAX_TOTAL_CONN);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory
				.getSocketFactory(), 443));

		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(
				params, schemeRegistry);

		DefaultHttpClient client = new DefaultHttpClient(cm, params);

		client.addRequestInterceptor(new HttpRequestInterceptor() {

			public void process(HttpRequest request, HttpContext context)
					throws HttpException, IOException {
				request.addHeader("User-Agent",
								"Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1.5) Gecko/20091102 Firefox/3.5.5 GTB6 (.NET CLR 3.5.30729)");

			}
		});
		return client;
	}

}
