package cn.yo2.aquarium.pocketvoa;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.text.TextUtils;
import android.util.Log;
import cn.yo2.aquarium.pocketvoa.parser.IListParser;

public class ListGenerator {
	private static final String CLASSTAG = ListGenerator.class
			.getSimpleName();

	IListParser mParser;

	private ResponseHandler<String> mResponseHandler;
	
	private DefaultHttpClient mHttpClient;
	
	public ListGenerator(ResponseHandler<String> responseHandler,
			DefaultHttpClient httpClient) {
		super();
		this.mResponseHandler = responseHandler;
		this.mHttpClient = httpClient;
	}

	public ArticleList getArticleList(String url) throws IOException,
			IllegalContentFormatException {
		if (TextUtils.isEmpty(url))
			throw new IllegalArgumentException("Argument url should not be blank.");
		if (mParser == null) 
			throw new IllegalStateException("You should set a IListParser first.");
		HttpGet get = new HttpGet(url);
		try {
			String body = mHttpClient.execute(get, mResponseHandler);
			return new ArticleList(mParser.parse(body), mParser.parsePageCount(body));
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
}
