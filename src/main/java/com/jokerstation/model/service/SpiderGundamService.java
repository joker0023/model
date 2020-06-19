package com.jokerstation.model.service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jokerstation.common.exception.BizException;
import com.jokerstation.common.util.JsonUtils;
import com.jokerstation.common.util.WebUtil;
import com.jokerstation.model.mapper.ItemImgMapper;
import com.jokerstation.model.mapper.ModelItemMapper;
import com.jokerstation.model.pojo.ItemImg;
import com.jokerstation.model.pojo.ModelItem;

@Service
public class SpiderGundamService {
	
	private static Logger logger = LoggerFactory.getLogger(SpiderGundamService.class);
	
	@Autowired
	private ModelItemMapper modelItemMapper;
	
	@Autowired
	private ItemImgMapper itemImgMapper;
	
	@Value("${spider.url.pg}")
	private String pgPageUrl;
	@Value("${spider.url.mg}")
	private String mgPageUrl;
	@Value("${spider.url.rg}")
	private String rgPageUrl;
	@Value("${spider.url.sd}")
	private String sdPageUrl;
	
	
	private static final String BASEDIR = "html";
	
	public void spiderPG() throws Exception {
		spiderOneType("pg", pgPageUrl);
	}
	
	public void spiderMG() throws Exception {
		spiderOneType("mg", mgPageUrl);
	}
	
	public void spiderRG() throws Exception {
		spiderOneType("rg", rgPageUrl);
	}
	
	public void spiderSD() throws Exception {
		spiderOneType("sd", sdPageUrl);
	}
	
	private void spiderOneType(String type, String url) throws Exception {
		Random random = new Random();
		List<ModelItem> allItemList = new ArrayList<>();
		for (int i = 1; i < 50; i++) {
			String pageUrl = url;
			if (i > 1) {
				pageUrl = url + "?pageNo=" + i;
			}
			Thread.sleep(random.nextInt(3000) + 2000);
			List<ModelItem> itemList = spiderPage(pageUrl);
			logger.info("itemList-size: " + itemList.size());
			if (itemList.size() == 0) {
				break;
			}
			
			allItemList.addAll(itemList);
		}
		
		Collections.reverse(allItemList);
		for (ModelItem item : allItemList) {
			if (item.getTitle().contains("预定") || item.getTitle().contains("贴纸") || item.getTitle().contains("预约")) {
				continue;
			}
			
			ModelItem exists = getModelItemByTitle(item.getTitle());
			if (null != exists) {
				continue;
			}
			
			item.setType(type);
			item.setOpen(false);
			item.setCreated(new Date());
			modelItemMapper.insert(item);
			logger.info("insert item: " + item.getTitle());
		}
		
		logger.info("爬取一个类型完成type: " + type);
	}
	
	private List<ModelItem> spiderPage(String pageUrl) throws Exception {
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
				throw new BizException("cookies 过时了！");
			}
			result = JsonUtils.toBean(result, String.class);
		} else {
			result = "\"" + result + "\"";
			result = JsonUtils.toBean(result, String.class);
		}
		
		Document doc = Jsoup.parse(result);
		Elements jItems = doc.select(".J_TItems");
		if (jItems.size() == 0) {
			throw new BizException("cookies 过时了！");
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
				if (StringUtils.isBlank(itemUrl)) {
					continue;
				}
				if (itemUrl.startsWith("//")) {
					itemUrl = "https:" + itemUrl;
				}
				itemUrl = unicodeToStr(itemUrl);
				
				String title = itemNameEle.text();
				if (StringUtils.isBlank(title)) {
					continue;
				}
				
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
			throw new BizException("没有找到搜索url");
		}
	}
	
	public void spiderItem(Long id) throws Exception {
		ModelItem modelItem = modelItemMapper.selectByPrimaryKey(id);
		if (null == modelItem) {
			return;
		}
		try {
			logger.info("spider item: " + modelItem.getUrl());
			Map<String, String> header = getHeader(null);
			header.put("Host", "detail.tmall.com");
			String result = WebUtil.doGet(modelItem.getUrl(), header);
			Document doc = Jsoup.parse(result);
			
			String coverImg = getCoverImg(doc);
			List<String> detailImgs = getDetailImgs(doc);
			
			deleteOldLocalImg(modelItem);
			delItemImg(modelItem.getId());
			
			String dir = "/gundam/" + modelItem.getType();
			modelItem.setCoverImg(coverImg);
			String localCoverImg = downloadImg(dir, modelItem.getCoverImg());
			modelItem.setLocalCoverImg(localCoverImg);
			modelItem.setStatus(1);
			modelItemMapper.updateByPrimaryKey(modelItem);
			
			for (String detailImg : detailImgs) {
				ItemImg itemImg = new ItemImg();
				itemImg.setItemId(modelItem.getId());
				itemImg.setDetailImg(detailImg);
				String localDetailImg = downloadImg(dir, detailImg);
				itemImg.setLocalDetailImg(localDetailImg);
				itemImg.setCreated(new Date());
				itemImgMapper.insert(itemImg);
			}
			
			logger.info("spider item success: " + modelItem.getUrl());
		} catch (Exception e) {
			modelItem.setStatus(2);
			modelItemMapper.updateByPrimaryKey(modelItem);
			throw e;
		}
	}
	
	public void spiderItemImg(Long id) throws Exception {
		ModelItem modelItem = modelItemMapper.selectByPrimaryKey(id);
		if (null == modelItem) {
			return;
		}
		try {
			logger.info("spider item img: " + modelItem.getId());
			
			deleteOldLocalImg(modelItem);
			
			String dir = "/gundam/" + modelItem.getType();
			String localCoverImg = downloadImg(dir, modelItem.getCoverImg());
			modelItem.setLocalCoverImg(localCoverImg);
			modelItem.setStatus(1);
			modelItemMapper.updateByPrimaryKey(modelItem);
			
			List<ItemImg> itemImgs = listItemImg(modelItem.getId());
			for (ItemImg itemImg : itemImgs) {
				String localDetailImg = downloadImg(dir, itemImg.getDetailImg());
				itemImg.setLocalDetailImg(localDetailImg);
				itemImgMapper.updateByPrimaryKey(itemImg);
			}
		} catch (Exception e) {
			modelItem.setStatus(2);
			modelItemMapper.updateByPrimaryKey(modelItem);
			throw e;
		}
	}
	
	public void toggleOpen(Long id) throws Exception {
		ModelItem modelItem = modelItemMapper.selectByPrimaryKey(id);
		if (null == modelItem) {
			return;
		}
		Boolean open = modelItem.getOpen();
		if (null != open && open) {
			modelItem.setOpen(false);
		} else {
			modelItem.setOpen(true);
		}
		modelItemMapper.updateByPrimaryKey(modelItem);
	}
	
	private void deleteOldLocalImg(ModelItem modelItem) {
		if (StringUtils.isNotBlank(modelItem.getLocalCoverImg())) {
			File file = new File(BASEDIR + modelItem.getLocalCoverImg());
			if (file.exists()) {
				file.delete();
			}
			modelItem.setLocalCoverImg(null);
		}
		
		List<ItemImg> itemImgs = listItemImg(modelItem.getId());
		for (ItemImg itemImg : itemImgs) {
			if (StringUtils.isNotBlank(itemImg.getLocalDetailImg())) {
				File file = new File(BASEDIR + itemImg.getLocalDetailImg());
				if (file.exists()) {
					file.delete();
				}
//				itemImg.setLocalDetailImg(null);
			}
		}
	}
	
	@SuppressWarnings("unused")
	private String getTitle(Document doc) throws Exception {
		return doc.select("#J_DetailMeta").select(".tb-detail-hd").text();
	}
	
	private String getCoverImg(Document doc) throws Exception {
		String imgSrc = null;
		Elements liEles = doc.select("#J_UlThumb").select("li");
		if (liEles.size() > 1) {
			imgSrc = liEles.get(1).select("img").attr("src");
		} else {
			imgSrc = liEles.get(0).select("img").attr("src");
		}
		if (imgSrc.startsWith("//")) {
			imgSrc = "https:" + imgSrc;
		}
		imgSrc = imgSrc.replace("_60x60", "_430x430");
		return imgSrc;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<String> getDetailImgs(Document doc) throws Exception {
		String json = getMiddleStr(doc.html(), "TShop.Setup(", ");").trim();
		Map bean = JsonUtils.toBean(json, Map.class);
		Map<String, String> api = (Map<String, String>)bean.get("api");
		String descUrl = api.get("descUrl");
		if (StringUtils.isBlank(descUrl)) {
			descUrl = api.get("httpsDescUrl");
		}
		if (StringUtils.isBlank(descUrl)) {
			throw new BizException("没有找到详情图片");
		}
		
		if (descUrl.startsWith("//")) {
			descUrl = "https:" + descUrl;
		}
		
		Map<String, String> header = getHeader(null);
		header.put("Host", "descnew.taobao.com");
		String descJson = WebUtil.doGet(descUrl, header);
		
		List<String> imgList = new ArrayList<>();
		String html = getMiddleStr(descJson, "'", "'");
		Document descDoc = Jsoup.parse(html);
		Elements imgs = descDoc.select("img");
		for (Element img : imgs) {
			imgList.add(img.attr("src"));
		}
		return imgList;
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
	
	private ModelItem getModelItemByTitle(String title) {
		ModelItem example = new ModelItem();
		example.setTitle(title);
		return modelItemMapper.selectOne(example);
	}
	
	public PageInfo<ModelItem> getModelItems(String type, int page, int size) {
		PageHelper.startPage(page, size);
		PageHelper.orderBy("id desc");
		
		if (null == type) {
			return new PageInfo<>(modelItemMapper.selectAll());
		} else {
			ModelItem record = new ModelItem();
			record.setType(type);;
			return new PageInfo<>(modelItemMapper.select(record));
		}
	}
	
	public List<ItemImg> listItemImg(Long itemId) {
		ItemImg record = new ItemImg();
		record.setItemId(itemId);
		return itemImgMapper.select(record);
	}
	
	private void delItemImg(Long itemId) {
		ItemImg record = new ItemImg();
		record.setItemId(itemId);
		itemImgMapper.delete(record);
	}
}
