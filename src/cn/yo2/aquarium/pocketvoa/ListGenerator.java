package cn.yo2.aquarium.pocketvoa;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;

import android.util.Log;

public class ListGenerator extends Generator {
	private static final String CLASSTAG = ListGenerator.class
			.getSimpleName();

	ListParser mParser;


	public ArrayList<Article> getArticleList(String url) throws IOException,
			IllegalContentFormatException {
		HttpGet get = new HttpGet(url);
		BasicResponseHandler handler = new BasicResponseHandler();
		try {
			String body = mClient.execute(get, handler);
			return mParser.parse(body);
		} catch (IOException e) {
			get.abort();
			Log.e(CLASSTAG, "IO Error when get list from " + url, e);
			throw e;
		} catch (IllegalContentFormatException e) {
			get.abort();
			Log.e(CLASSTAG, "Content format Error when get list from " + url, e);
			throw e;
		} 
	}

	public void release() {
		mClient.getConnectionManager().shutdown();
	}
}
