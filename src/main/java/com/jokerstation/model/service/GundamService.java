package com.jokerstation.model.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jokerstation.model.mapper.ItemImgMapper;
import com.jokerstation.model.mapper.ModelItemMapper;
import com.jokerstation.model.pojo.ItemImg;
import com.jokerstation.model.pojo.ModelItem;

@Service
public class GundamService {

	@Autowired
	private SpiderGundamService spiderGundamService;
	
	@Autowired
	private ModelItemMapper modelItemMapper;
	
	@Autowired
	private ItemImgMapper itemImgMapper;
	
	private static final String BASEDIR = "html";
	
	public List<ModelItem> listOpenItems(String type) {
		PageHelper.orderBy("id desc");
		ModelItem record = new ModelItem();
		record.setOpen(true);
		record.setType(type);
		return modelItemMapper.select(record);
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
	
	private String downloadImg(String dir, InputStream in) throws Exception {
		if (!dir.startsWith("/")) {
			dir = "/" + dir;
		}
		if (!dir.endsWith("/")) {
			dir = dir + "/";
		}
		String fileName = UUID.randomUUID().toString().replace("-", "") + "_" + System.currentTimeMillis() + ".jpg";
		String filePath = dir + fileName;
		FileOutputStream output = new FileOutputStream(new File(BASEDIR + filePath));
		IOUtils.copy(in, output);
		output.flush();
		output.close();
		return filePath;
	}
	
	public void addItem(MultipartFile coverImgFile, MultipartFile itemImgFile, ModelItem modelItem) throws Exception {
		ModelItem dbModelItem = spiderGundamService.getModelItemByTitle(modelItem.getTitle());
		if (null != dbModelItem) {
			throw new Exception("已经存在的标题");
		}
		
		String dir = "/gundam/" + modelItem.getType();
		String coverImgSrc = downloadImg(dir, coverImgFile.getInputStream());
		String itemImgSrc = downloadImg(dir, itemImgFile.getInputStream());
		
		modelItem.setLocalCoverImg(coverImgSrc);
		modelItem.setCreated(new Date());
		modelItem.setOpen(false);
		modelItem.setStatus(1);
		modelItemMapper.insert(modelItem);
		
		dbModelItem = spiderGundamService.getModelItemByTitle(modelItem.getTitle());
		Long itemId = dbModelItem.getId();
		
		ItemImg itemImg = new ItemImg();
		itemImg.setCreated(new Date());
		itemImg.setItemId(itemId);
		itemImg.setLocalDetailImg(itemImgSrc);
		itemImgMapper.insert(itemImg);
	}
	
	public void delItem(Long itemId) {
		ModelItem modelItem = modelItemMapper.selectByPrimaryKey(itemId);
		if (null == modelItem) {
			return;
		}
		
		spiderGundamService.deleteOldLocalImg(modelItem);
		modelItemMapper.deleteByPrimaryKey(itemId);
		
		ItemImg itemImgRecord = new ItemImg();
		itemImgRecord.setItemId(itemId);
		itemImgMapper.delete(itemImgRecord);
	}
	
}
