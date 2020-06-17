package com.jokerstation.common.util;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
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
	
	public static CloseableHttpClient createSSLClientDefault() {
		try {
			// 在调用SSL之前需要重写验证方法，取消检测SSL
            X509TrustManager trustManager = new X509TrustManager() {
                @Override public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                @Override public void checkClientTrusted(X509Certificate[] xcs, String str) {}
                @Override public void checkServerTrusted(X509Certificate[] xcs, String str) {}
            };
            SSLContext ctx = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
            ctx.init(null, new TrustManager[] { trustManager }, null);
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(ctx, NoopHostnameVerifier.INSTANCE);
            // 创建Registry
            RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT)
                    .setExpectContinueEnabled(Boolean.TRUE).setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM,AuthSchemes.DIGEST))
                    .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).build();
            org.apache.http.config.Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https",socketFactory).build();
            // 创建ConnectionManager，添加Connection配置信息
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            CloseableHttpClient closeableHttpClient = HttpClients.custom().setConnectionManager(connectionManager)
                    .setDefaultRequestConfig(requestConfig).build();
            return closeableHttpClient;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return HttpClientBuilder.create().build();
	}

	private static String execute(HttpUriRequest request) throws Exception {
		Header[] headers = request.getHeaders("User-Agent");
		if (null == headers || headers.length == 0) {
			request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; …) Gecko/20100101 Firefox/66.0");
		}
		
		CloseableHttpClient client = null;
		if (request.getURI().toString().startsWith("https://")) {
			System.out.println("is https");
			client = createSSLClientDefault();
		} else {
			client = HttpClientBuilder.create().build();
		}
		
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
