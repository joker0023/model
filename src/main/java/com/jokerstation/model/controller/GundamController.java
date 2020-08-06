package com.jokerstation.model.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jokerstation.common.data.ErrorCode;
import com.jokerstation.common.data.ResultModel;
import com.jokerstation.model.pojo.ItemImg;
import com.jokerstation.model.pojo.ModelItem;
import com.jokerstation.model.service.GundamService;
import com.jokerstation.model.service.SpiderGundamService;

@RestController
@RequestMapping("/gundam")
public class GundamController {
	
	@Autowired
	private GundamService gundamService;
	
	@Autowired
	private SpiderGundamService spiderGundamService;
	
	private static List<String> typeList = Arrays.asList("pg", "mg", "rg", "sd");

	@RequestMapping("/getModelItems")
	public ResultModel getModelItems(String type) {
		List<ModelItem> itemList = new ArrayList<ModelItem>();
		if (typeList.contains(type)) {
			itemList = gundamService.listOpenItems(type);
		}
		return new ResultModel(itemList); 
	}
	
	@RequestMapping("/getItemImgs")
	public ResultModel getItemImgs(Long itemId) {
		if (null == itemId) {
			return new ResultModel(ErrorCode.PARAM_ILLEGAL.getCode(), ErrorCode.PARAM_ILLEGAL.getMsg());
		}
		List<ItemImg> itemImgs = spiderGundamService.listItemImg(itemId);
		return new ResultModel(itemImgs);
	}
}
