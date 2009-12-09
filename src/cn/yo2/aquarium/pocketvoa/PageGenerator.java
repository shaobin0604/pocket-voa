package cn.yo2.aquarium.pocketvoa;

import java.io.IOException;

import org.apache.http.client.methods.HttpGet;

import android.text.TextUtils;
import android.util.Log;
import cn.yo2.aquarium.pocketvoa.parser.IPageParser;

public class PageGenerator extends HttpUtil {
	private static final String CLASSTAG = PageGenerator.class
			.getSimpleName();

	IPageParser mParser;

	public void getArticle(Article article) throws IOException,
			IllegalContentFormatException {
		if (TextUtils.isEmpty(article.url))
			throw new IllegalArgumentException("article's url should not be blank.");
		
		HttpGet get = new HttpGet(article.url);
		try {
			String body = mClient.execute(get, mResponseHandler);
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
