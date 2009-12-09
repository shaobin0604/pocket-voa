package cn.yo2.aquarium.pocketvoa;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.util.EntityUtils;

public class ResponseHandler implements
		org.apache.http.client.ResponseHandler<String> {
	
	private String mDefaultCharset = "utf-8";
	
	public ResponseHandler(String defaultCharset) {
		this.mDefaultCharset = defaultCharset;
	}
	
	public String handleResponse(final HttpResponse response)
			throws HttpResponseException, IOException {
		StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() >= 300) {
			throw new HttpResponseException(statusLine.getStatusCode(),
					statusLine.getReasonPhrase());
		}

		HttpEntity entity = response.getEntity();
		return entity == null ? null : EntityUtils.toString(entity, mDefaultCharset);
	}

}
