package cn.yo2.aquarium.pocketvoa;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class HttpUtil {
	private static final int CONN_TIME_OUT = 1000 * 3; // millis
	private static final int READ_TIME_OUT = 1000 * 10; // millis
	
	private static final String DEFAULT_CHARSET = "utf-8";
	
	protected DefaultHttpClient mClient;
	protected ResponseHandler mResponseHandler;
	
	public HttpUtil() {
		super();
		
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, CONN_TIME_OUT);
		HttpConnectionParams.setSoTimeout(params, READ_TIME_OUT);

		this.mClient = new DefaultHttpClient(params);
		this.mResponseHandler = new ResponseHandler(DEFAULT_CHARSET);
	}
}
