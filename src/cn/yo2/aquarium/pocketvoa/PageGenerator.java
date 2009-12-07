package cn.yo2.aquarium.pocketvoa;

import java.io.IOException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;

import android.text.TextUtils;
import android.util.Log;

public class PageGenerator extends HttpUtil {
	private static final String CLASSTAG = PageGenerator.class
			.getSimpleName();

	PageParser mParser;

	public void getArticle(Article article) throws IOException,
			IllegalContentFormatException {
		if (TextUtils.isEmpty(article.url))
			throw new IllegalArgumentException("article's url should not be blank.");
		
		HttpGet get = new HttpGet(article.url);
		BasicResponseHandler handler = new BasicResponseHandler();
		try {
			String body = mClient.execute(get, handler);
			mParser.parse(article, body);
		} catch (IOException e) {
			get.abort();
			Log.e(CLASSTAG, "IO Error when get list from " + article.url, e);
			throw e;
		} catch (IllegalContentFormatException e) {
			get.abort();
			Log.e(CLASSTAG, "Content format Error when get list from " + article.url,
					e);
			throw e;
		} 
	}
	
	public void release() {
		mClient.getConnectionManager().shutdown();
	}
}
