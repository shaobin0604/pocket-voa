package cn.yo2.aquarium.pocketvoa;

import java.io.IOException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;

import android.util.Log;

public class PageGenerator extends Generator {
	private static final String CLASSTAG = PageGenerator.class
			.getSimpleName();

	PageParser mParser;
	
	public void downloadMp3() {
		
	}

	public void getArticle(String url, Article article) throws IOException,
			IllegalContentFormatException {
		HttpGet get = new HttpGet(url);
		BasicResponseHandler handler = new BasicResponseHandler();
		try {
			String body = mClient.execute(get, handler);
			mParser.parse(article, body);
		} catch (IOException e) {
			get.abort();
			Log.e(CLASSTAG, "IO Error when get list from " + url, e);
			throw e;
		} catch (IllegalContentFormatException e) {
			get.abort();
			Log.e(CLASSTAG, "Content format Error when get list from " + url,
					e);
			throw e;
		} 
	}
	
	public void release() {
		mClient.getConnectionManager().shutdown();
	}
}
