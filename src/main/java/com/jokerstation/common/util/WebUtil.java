package com.jokerstation.common.util;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class WebUtil {
	
	private static String charSet = "UTF-8";
	
	public static String doGet(String url, Map<String, String> header) throws Exception {
		HttpUriRequest request = RequestBuilder.get().setUri(url).build();
		if (null != header) {
			for (String key : header.keySet()) {
				request.setHeader(key, header.get(key));
			}
		}
		
		return execute(request);
	}
	
	public static String doPost(String url, Object params) throws Exception {
		HttpEntity httpEntity = new StringEntity(JsonUtils.toJson(params), "UTF-8");
		HttpUriRequest request = RequestBuilder.post()
				.setUri(url)
				.setHeader("Content-type", "application/json")
				.setEntity(httpEntity)
				.build();
		return execute(request);
	}

	private static String execute(HttpUriRequest request) throws Exception {
		Header[] headers = request.getHeaders("User-Agent");
		if (null == headers || headers.length == 0) {
			request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; …) Gecko/20100101 Firefox/66.0");
		}
		CloseableHttpClient client = HttpClientBuilder.create().build();
		CloseableHttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();
		StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() == 200) {
			String result = EntityUtils.toString(entity, charSet);
			client.close();
			response.close();
			return result;
		} else {
			throw new RuntimeException("http-execute error, status: " + statusLine.getStatusCode());
		}
	}
	
	public static void download(String url, FileOutputStream output) throws Exception {
		HttpUriRequest request = RequestBuilder.get().setUri(url).build();
		request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; …) Gecko/20100101 Firefox/66.0");
		CloseableHttpClient client = HttpClientBuilder.create().build();
		CloseableHttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();
		StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() == 200) {
			InputStream input = entity.getContent();
			IOUtils.copy(input, output);
			response.close();
			client.close();
		} else {
			throw new RuntimeException("http-execute error, status: " + statusLine.getStatusCode());
		}
	}
}
