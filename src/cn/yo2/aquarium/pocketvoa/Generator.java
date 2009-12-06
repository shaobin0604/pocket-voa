package cn.yo2.aquarium.pocketvoa;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class Generator {
	private static int CONN_TIME_OUT = 1000 * 3; // millis
	private static int READ_TIME_OUT = 1000 * 10; // millis
	
	protected DefaultHttpClient mClient;
	
	public Generator() {
		super();
		
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, CONN_TIME_OUT);
		HttpConnectionParams.setSoTimeout(params, READ_TIME_OUT);

		this.mClient = new DefaultHttpClient(params);
	}
}
