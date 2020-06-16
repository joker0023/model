package com.jokerstation.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.jokerstation.common.util.JsonUtils;
import com.jokerstation.common.util.WebUtil;
import com.jokerstation.model.BootApplication;
import com.jokerstation.model.service.SpiderGundamService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BootApplication.class)
public class SpiderTest {
	
	private static Logger logger = LoggerFactory.getLogger(SpiderTest.class);

	public static void main(String[] args) throws Exception {
//		spider();

		new SpiderGundamService().downloadPg();
		
		System.out.println("== over ==");
	}
	
	private static String getMiddleStr(String s, String startStr, String endStr) {
		int startIndex = s.indexOf(startStr);
		int endIndex = s.indexOf(endStr, startIndex + startStr.length());
		return s.substring(startIndex + startStr.length(), endIndex);
	}
	
	public static void spider() throws Exception {
		String file = "C:\\Users\\qiaoh\\Desktop\\aabb.txt";
		String result = "";
		FileInputStream in = new FileInputStream(file);
		List<String> lines = IOUtils.readLines(in);
		in.close();
		for (String s : lines) {
			result += s;
		}
		
		String json = getMiddleStr(result, "TShop.Setup(", ")");
		json = json.trim();
		Map bean = JsonUtils.toBean(json, Map.class);
		Map<String, String> api = (Map<String, String>)bean.get("api");
		String descUrl = api.get("descUrl");
		if (StringUtils.isBlank(descUrl)) {
			descUrl = api.get("httpsDescUrl");
		}
		if (StringUtils.isNotBlank(descUrl)) {
			descUrl = "https:" + descUrl;
		}
		
		System.out.println("descUrl: " + descUrl);
		
		Map<String, String> header = getHeader(null);
		header.put("Host", "descnew.taobao.com");
		String descJson = WebUtil.doGet(descUrl, header);
		String detailImg = getMiddleStr(descJson, "src=\"", "\"");
		System.out.println("detailImg: " + detailImg);
		
		System.out.println("=== over ===");
	}
	
	private static Map<String, String> getHeader(String cookies) {
		String defCookies = "cna=dD8zFJ6avHgCAXkhJ2LKkmPn; enc=t4xGMJr7aoxK29BXACHVW2lHzdVul6TubO9p4IUw5W%2FBoyB3YoTJY9eta9mXv6JMcECRdSq6kvlrc5NnKXD18A%3D%3D; lid=qiaohe11; hng=CN%7Czh-CN%7CCNY%7C156; t=a9c12159f935d8d70ed5f81031bc99d5; _tb_token_=f7ee7eb53a19e; cookie2=1f2a91b1a2d3957d3eba0c3295cb3b7c; isg=BBISy3y5mE4r_uQyOOwx3E_-YNj0Ixa9ZAcAwtxrSUWm77PpxLMuzZ1OX8tThI5V; l=eBMCMNr7Q578JJR1BOfaKurza77tDIObYuPzaNbMiOCPOM5p-7ocWZxck3L9Cn1VHsnMR3yblDgJBa49qyIVJ-PRqzI4BzWt3dC..; pnm_cku822=; cq=ccp%3D1; x5sec=7b2273686f7073797374656d3b32223a226263663038303233316461666135313635663362343166386536396166396431434f4f6d6a666346454f58693861447a6a64476265686f4c4e6a637a4f4451314d444d784f7a4d3d227d";
		if (null == cookies) {
			cookies = defCookies;
		}
		Map<String, String> header = new HashMap<String, String>();
		header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		header.put("Accept-Encoding", "gzip, deflate, br");
		header.put("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
		header.put("Cache-Control", "max-age=0");
		header.put("Connection", "keep-alive");
		header.put("Cookie", cookies);
		header.put("TE", "Trailers");
		header.put("Upgrade-Insecure-Requests", "1");
		header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:77.0) Gecko/20100101 Firefox/77.0");
		return header;
	}
	
	private static String baseDir = "html/gundam";
	
	private static void downloadPg() throws Exception {
		String dir = baseDir + "/pg";
//		String url = "https://bandai.tmall.com/category-1460930310.htm";
		String url = "https://bandai.tmall.com/i/asynSearch.htm?_ksTS=1591957229605_521&callback=jsonp522&mid=w-17160282518-0&wid=17160282518&path=/category-1460930310.htm&spm=a1z10.1-b-s.w4006-21334893843.37.4b0f5562R5X45S&scene=taobao_shop&catId=1460930310&scid=1460930310";
	}
}
