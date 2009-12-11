package cn.yo2.aquarium.pocketvoa;

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

public class HttpUtil {
	private static final int CONN_TIME_OUT = 1000 * 3; // millis
	private static final int READ_TIME_OUT = 1000 * 10; // millis
	private static final int MAX_TOTAL_CONN = 10;

	private static final String DEFAULT_CHARSET = "utf-8";

	protected DefaultHttpClient mClient;
	protected ResponseHandler mResponseHandler;

	public HttpUtil() {
		super();

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
		
		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
		
		this.mClient = new DefaultHttpClient(cm, params);
		this.mResponseHandler = new ResponseHandler(DEFAULT_CHARSET);

		
	}
}
