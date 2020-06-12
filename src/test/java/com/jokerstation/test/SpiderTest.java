package com.jokerstation.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jokerstation.common.util.JsonUtils;
import com.jokerstation.common.util.WebUtil;

public class SpiderTest {

	public static void main(String[] args) throws Exception {
//		String url = "https://s.taobao.com/search?spm=a230r.1.1998181369.d4919860.74667099Wm8Pit&q=%E9%AB%98%E8%BE%BE&imgfile=&commend=all&ssid=s5-e&search_type=item&sourceId=tb.index&ie=utf8&initiative_id=tbindexz_20170306&tab=mall";
//		String result = WebUtil.doGet(url);
//		System.out.println(result);
		
		
		downloadPg();
		
//		String s = "\"<class=\\\"ss\\\">\";
//		System.out.println(s);
//		System.out.println(JsonUtils.toBean(s, String.class));
		System.out.println("=== over ===");
	}
	
	private static String baseDir = "html/gundam/";
	
	private static void downloadPg() throws Exception {
		String dir = baseDir + "pg/";
//		String url = "https://bandai.tmall.com/category-1460930310.htm";
		String url = "https://bandai.tmall.com/i/asynSearch.htm?_ksTS=1591957229605_521&callback=jsonp522&mid=w-17160282518-0&wid=17160282518&path=/category-1460930310.htm&spm=a1z10.1-b-s.w4006-21334893843.37.4b0f5562R5X45S&scene=taobao_shop&catId=1460930310&scid=1460930310";
		downloadPage(dir, url);
	}
	
	private static Map<String, String> getHeader() {
		String cookies = "cna=dD8zFJ6avHgCAXkhJ2LKkmPn; enc=t4xGMJr7aoxK29BXACHVW2lHzdVul6TubO9p4IUw5W%2FBoyB3YoTJY9eta9mXv6JMcECRdSq6kvlrc5NnKXD18A%3D%3D; lid=qiaohe11; hng=CN%7Czh-CN%7CCNY%7C156; t=a9c12159f935d8d70ed5f81031bc99d5; _tb_token_=f7ee7eb53a19e; cookie2=1f2a91b1a2d3957d3eba0c3295cb3b7c; isg=BBISy3y5mE4r_uQyOOwx3E_-YNj0Ixa9ZAcAwtxrSUWm77PpxLMuzZ1OX8tThI5V; l=eBMCMNr7Q578JJR1BOfaKurza77tDIObYuPzaNbMiOCPOM5p-7ocWZxck3L9Cn1VHsnMR3yblDgJBa49qyIVJ-PRqzI4BzWt3dC..; pnm_cku822=; cq=ccp%3D1; x5sec=7b2273686f7073797374656d3b32223a226263663038303233316461666135313635663362343166386536396166396431434f4f6d6a666346454f58693861447a6a64476265686f4c4e6a637a4f4451314d444d784f7a4d3d227d";
		Map<String, String> header = new HashMap<String, String>();
		header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		header.put("Accept-Encoding", "gzip, deflate, br");
		header.put("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
		header.put("Cache-Control", "max-age=0");
		header.put("Connection", "keep-alive");
		header.put("Cookie", cookies);
		header.put("Host", "bandai.tmall.com");
		header.put("TE", "Trailers");
		header.put("Upgrade-Insecure-Requests", "1");
		header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:77.0) Gecko/20100101 Firefox/77.0");
		return header;
	}
	
	private static void downloadPage(String dir, String url) throws Exception {
		Map<String, String> header = getHeader();
		String result = WebUtil.doGet(url, header);
		result = result.trim();
//		System.out.println(result);
		String jsonpName = "jsonp522(";
		if (result.startsWith(jsonpName)) {
			result = result.substring(jsonpName.length());
			result = result.substring(0, result.length() - 1);
		}
		
		
		if (!result.startsWith("\"")) {
			System.out.println(result);
			throw new RuntimeException("cookies 过时了！");
		}
		result = JsonUtils.toBean(result, String.class);
		Document doc = Jsoup.parse(result);
//		System.out.println(result);
		Elements elements = doc.select(".J_TItems").select(".item");
		System.out.println("size: " + elements.size());
		
		Element element = elements.get(0);
		Element imgEle = element.select(".photo").select("img").first();
		Element titleEle = element.select(".detail").select(".item-name").first();
		System.out.println("https:" + imgEle.attr("data-ks-lazyload"));
		System.out.println(titleEle.text());
	}
	
	private static void downloadImg(String dir, String url) throws Exception {
		String fileName = System.currentTimeMillis() + ".jpg";
		FileOutputStream output = new FileOutputStream(new File(dir + fileName));
		WebUtil.download(url, output);
		output.flush();
		output.close();
	}
}
