package cn.yo2.aquarium.pocketvoa;

import java.io.IOException;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.text.TextUtils;
import android.util.Log;
import cn.yo2.aquarium.pocketvoa.parser.IPageParser;

public class PageGenerator {
	private static final String CLASSTAG = PageGenerator.class
			.getSimpleName();

	IPageParser mParser;
	
	private ResponseHandler<String> mResponseHandler;
	
	private DefaultHttpClient mHttpClient;

	public PageGenerator(ResponseHandler<String> responseHandler,
			DefaultHttpClient httpClient) {
		super();
		this.mResponseHandler = responseHandler;
		this.mHttpClient = httpClient;
	}

	public void getArticle(Article article, boolean translate) throws IOException,
			IllegalContentFormatException {
		if (TextUtils.isEmpty(article.urltext))
			throw new IllegalArgumentException("article's url should not be blank.");
		HttpGet get = null;
		if (translate)
			get = new HttpGet(article.urltextzh);
		else 
			get = new HttpGet(article.urltext);
		try {
			String body = mHttpClient.execute(get, mResponseHandler);
			mParser.parse(article, body);
		} catch (IOException e) {
			get.abort();
			Log.e(CLASSTAG, "IO Error when get list from " + article.urltext, e);
			throw e;
		} catch (IllegalContentFormatException e) {
			get.abort();
			Log.e(CLASSTAG, "Content format Error when get list from " + article.urltext,
					e);
			throw e;
		} 
	}
}
