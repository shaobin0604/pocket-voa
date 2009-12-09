package cn.yo2.aquarium.pocketvoa;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.methods.HttpGet;

import android.util.Log;
import cn.yo2.aquarium.pocketvoa.parser.IListParser;

public class ListGenerator extends HttpUtil {
	private static final String CLASSTAG = ListGenerator.class
			.getSimpleName();

	IListParser mParser;


	public ArrayList<Article> getArticleList(String url) throws IOException,
			IllegalContentFormatException {
		HttpGet get = new HttpGet(url);
		try {
			String body = mClient.execute(get, mResponseHandler);
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
