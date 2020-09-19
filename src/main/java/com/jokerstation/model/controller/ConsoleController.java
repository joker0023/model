package com.jokerstation.model.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageInfo;
import com.jokerstation.common.data.ErrorCode;
import com.jokerstation.common.data.ResultModel;
import com.jokerstation.model.pojo.ItemImg;
import com.jokerstation.model.pojo.ModelItem;
import com.jokerstation.model.service.GundamService;
import com.jokerstation.model.service.SpiderGundamService;

@RestController
@RequestMapping("/console/item")
public class ConsoleController {
	
	@Autowired
	private GundamService gundamService;
	
	@Autowired
	private SpiderGundamService spiderGundamService;

	@RequestMapping("/toggleOpen")
	public ResultModel toggleOpen(Long id) throws Exception {
		if (null == id) {
			return new ResultModel(ErrorCode.PARAM_ILLEGAL.getCode(), ErrorCode.PARAM_ILLEGAL.getMsg());
		}
		gundamService.toggleOpen(id);
		return new ResultModel();
	}
	
	@RequestMapping("/getItems")
	public ResultModel getItems(String type, Integer page, Integer size) {
		if (null == page) {
			page = 1;
		}
		if (null == size) {
			size = 10;
		}
		PageInfo<ModelItem> pageInfo = gundamService.getModelItems(type, page, size);
		return new ResultModel(pageInfo);
	}
	
	@RequestMapping("/getItemImgs")
	public ResultModel getItemImgs(Long itemId) {
		if (null == itemId) {
			return new ResultModel(ErrorCode.PARAM_ILLEGAL.getCode(), ErrorCode.PARAM_ILLEGAL.getMsg());
		}
		List<ItemImg> itemImgs = spiderGundamService.listItemImg(itemId);
		return new ResultModel(itemImgs);
	}
	
	@RequestMapping("/addItem")
	public ResultModel addItem(MultipartFile coverImgFile, MultipartFile itemImgFile, ModelItem modelItem) throws Exception {
		gundamService.addItem(coverImgFile, itemImgFile, modelItem);
		
		return new ResultModel();
	}
	
	@RequestMapping("/delItem")
	public ResultModel delItem(Long itemId) {
		gundamService.delItem(itemId);
		return new ResultModel();
	}
	
	
	
	
}
