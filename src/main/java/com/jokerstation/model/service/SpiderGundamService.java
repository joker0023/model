package com.jokerstation.model.service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.jokerstation.common.util.JsonUtils;
import com.jokerstation.common.util.WebUtil;
import com.jokerstation.model.config.ThreadPoolConfig;
import com.jokerstation.model.pojo.ModelItem;

public class SpiderGundamService {
	
	private static Logger logger = LoggerFactory.getLogger(SpiderGundamService.class);
	
	private ThreadPoolTaskExecutor downloadPool = new ThreadPoolConfig().downloadPool();
	
	private static final String BASEDIR = "html";
	
	public void downloadPg() throws Exception {
//		String dir = "/gundam/pg";
		String type = "pg";
		String url = "https://bandai.tmall.com/category-1460930310.htm"; //?pageNo=2
		downloadOneType(type, url);
	}
	
	private void downloadOneType(String type, String url) throws Exception {
		for (int i = 1; i < 50; i++) {
			String pageUrl = url;
			if (i > 1) {
				pageUrl = url + "?pageNo=" + i;
			}
			List<ModelItem> itemList = downloadPage(pageUrl);
			logger.info("itemList-size: " + itemList.size());
			
			if (itemList.size() == 0) {
				break;
			}
			
			for (ModelItem item : itemList) {
				item.setType(type);
//				System.out.println(item.getTitle());
//				System.out.println(item.getUrl());
			}
		}
		
		
	}
	
	private List<ModelItem> downloadPage(String pageUrl) throws Exception {
		logger.info("pageUrl: " + pageUrl);
		String url = getShopAsynSearchURL(pageUrl);
		logger.info("asynSearchURL: " + url);
		Map<String, String> header = getHeader(null);
		header.put("Host", "bandai.tmall.com");
		String result = WebUtil.doGet(url, header).trim();
		String jsonpName = "jsonp522(";
		if (result.startsWith(jsonpName)) {
			result = result.substring(jsonpName.length());
			result = result.substring(0, result.length() - 1);
			
			if (!result.startsWith("\"")) {
				System.out.println(result);
				throw new RuntimeException("cookies 过时了！");
			}
			result = JsonUtils.toBean(result, String.class);
		} else {
			result = "\"" + result + "\"";
			result = JsonUtils.toBean(result, String.class);
		}
		
		Document doc = Jsoup.parse(result);
		Elements jItems = doc.select(".J_TItems");
		if (jItems.size() == 0) {
			throw new RuntimeException("cookies 过时了！");
		}
		
		List<ModelItem> itemList = new ArrayList<ModelItem>();
		for (Element ele : jItems.first().children()) {
			String className = ele.attr("class");
			if (!"item4line1".equals(className)) {
				break;
			}
			
			Elements items = ele.select(".item");
			for (Element item : items) {
				Element itemNameEle = item.select(".detail").select(".item-name").first();
				String itemUrl = itemNameEle.attr("href");
				if (itemUrl.startsWith("//")) {
					itemUrl = "https:" + itemUrl;
				}
				itemUrl = unicodeToStr(itemUrl);
				String title = itemNameEle.text();
				
				ModelItem modelItem = new ModelItem();
				modelItem.setUrl(itemUrl);
				modelItem.setTitle(title);
				modelItem.setStatus(0);
				itemList.add(modelItem);
			}
		}
		
		return itemList;
	}
	
	private String getShopAsynSearchURL(String url) throws Exception {
		Map<String, String> header = getHeader(null);
		header.put("Host", "bandai.tmall.com");
		String result = WebUtil.doGet(url, header).trim();
		Document doc = Jsoup.parse(result);
		String shopAsynSearchURL = doc.select("#J_ShopAsynSearchURL").attr("value");
		if (StringUtils.isNotBlank(shopAsynSearchURL)) {
			return "https://bandai.tmall.com" + shopAsynSearchURL;
		} else {
			throw new RuntimeException("没有找到搜索url");
		}
	}
	
	private void downloadItem(ModelItem modelItem) throws Exception {
		try {
//			Map<String, String> header = getHeader(null);
//			header.put("Host", "detail.tmall.com");
//			String result = WebUtil.doGet(modelItem.getUrl(), header);
//			Document doc = Jsoup.parse(result);
//			
//			String title = getTitle(doc);
//			String coverImg = getCoverImg(doc);
//			String detailImg = getDetailImg(doc);
//			
//			ModelItem item = new ModelItem();
//			item.setTitle(title);
//			item.setCoverImg(coverImg);
//			item.setDetailImg(detailImg);
//			item.setCreated(new Date());
//			String localCoverImg = downloadImg(dir, coverImg);
//			item.setLocalCoverImg(localCoverImg);
//			String localDetailImg = downloadImg(dir, detailImg);
//			item.setLocalDetailImg(localDetailImg);
			
			//TODO: save
//			System.out.println(JsonUtils.toJson(item));
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private String getTitle(Document doc) throws Exception {
		return doc.select("#J_DetailMeta").select(".tb-detail-hd").text();
	}
	
	private String getCoverImg(Document doc) throws Exception {
		String imgSrc = doc.select("#J_UlThumb").select("li").get(1).select("img").attr("src");
		if (imgSrc.startsWith("//")) {
			imgSrc = "https:" + imgSrc;
		}
		imgSrc = imgSrc.replace("_60x60", "_430x430");
		return imgSrc;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String getDetailImg(Document doc) throws Exception {
		String json = getMiddleStr(doc.html(), "TShop.Setup(", ")").trim();
		Map bean = JsonUtils.toBean(json, Map.class);
		Map<String, String> api = (Map<String, String>)bean.get("api");
		String descUrl = api.get("descUrl");
		if (StringUtils.isBlank(descUrl)) {
			descUrl = api.get("httpsDescUrl");
		}
		if (StringUtils.isBlank(descUrl)) {
			throw new RuntimeException("没有找到详情图片");
		}
		
		if (descUrl.startsWith("//")) {
			descUrl = "https:" + descUrl;
		}
		
		Map<String, String> header = getHeader(null);
		header.put("Host", "descnew.taobao.com");
		String descJson = WebUtil.doGet(descUrl, header);
		String detailImg = getMiddleStr(descJson, "src=\"", "\"");
		return detailImg;
	}
	
	private String downloadImg(String dir, String url) throws Exception {
		if (!dir.startsWith("/")) {
			dir = "/" + dir;
		}
		String fileName = UUID.randomUUID().toString().replace("-", "") + "_" + System.currentTimeMillis() + ".jpg";
		String filePath = dir + "/" + fileName;
		FileOutputStream output = new FileOutputStream(new File(BASEDIR + filePath));
		WebUtil.download(url, output);
		output.flush();
		output.close();
		return filePath;
	}

	private Map<String, String> getHeader(String cookies) {
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
	
	private String getMiddleStr(String s, String startStr, String endStr) {
		int startIndex = s.indexOf(startStr);
		int endIndex = s.indexOf(endStr, startIndex + startStr.length());
		return s.substring(startIndex + startStr.length(), endIndex);
	}
	
	private String unicodeToStr(String url) {
		Pattern pattern = Pattern.compile("\\\\u([a-f0-9A-F]{1,4})");
		Matcher m = pattern.matcher(url);
		while (m.find()) {
			String s = (char)Integer.parseInt(m.group(1), 16) + "";
			url = url.replace(m.group(), s);
		}
		return url;
	}
}
