package com.jokerstation.model.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.jokerstation.common.data.ErrorCode;
import com.jokerstation.common.data.ResultModel;
import com.jokerstation.model.config.ThreadPoolConfig;
import com.jokerstation.model.pojo.ItemImg;
import com.jokerstation.model.pojo.ModelItem;
import com.jokerstation.model.service.SpiderGundamService;


@RestController
@RequestMapping("/console/spider")
public class SpiderController {
	
	private static Logger logger = LoggerFactory.getLogger(SpiderController.class);
	
	@Autowired
	private SpiderGundamService spiderGundamService;
	
	@Resource
	private ThreadPoolTaskExecutor downloadPool = new ThreadPoolConfig().downloadPool();

	@RequestMapping("/spiderList")
	public ResultModel spiderList(String type) throws Exception {
		logger.info("spider " + type + "...");
		if ("pg".equalsIgnoreCase(type)) {
			spiderGundamService.spiderPG();
		} else if ("mg".equalsIgnoreCase(type)) {
			spiderGundamService.spiderMG();
		} else if ("rg".equalsIgnoreCase(type)) {
			spiderGundamService.spiderRG();
		} else if ("sd".equalsIgnoreCase(type)) {
			spiderGundamService.spiderSD();
		}
		return new ResultModel();
	}
	
	@RequestMapping("/spiderItems")
	public ResultModel spiderItems(String ids) throws Exception {
		if (null == ids) {
			return new ResultModel(ErrorCode.PARAM_ILLEGAL.getCode(), ErrorCode.PARAM_ILLEGAL.getMsg());
		}
		downloadPool.execute(() -> {
			Random random = new Random();
			List<String> idLsit = Arrays.asList(ids.split(","));
			for (String id: idLsit) {
				spiderItemRetry(Long.valueOf(id), random.nextInt(3000) + 2000l);
			}
			logger.info("spiderItems over: " + ids);
		});
		
		return new ResultModel();
	}
	
	private void spiderItemRetry(Long id, Long sleepTime) {
		long maxTime = 180 * 1000;
		try {
			Thread.sleep(sleepTime);
			spiderGundamService.spiderItem(Long.valueOf(id));
		} catch (Exception e) {
			if (e.getMessage().contains("internal_error") && sleepTime < maxTime) {
				sleepTime += 60000;
				logger.warn("internal_error, sleep " + sleepTime + "s");
				spiderItemRetry(id, sleepTime);
			} else {
				logger.error("spider item error", e);
			}
		}
	}
	
	@RequestMapping("/spiderItem")
	public ResultModel spiderItem(Long id) throws Exception {
		if (null == id) {
			return new ResultModel(ErrorCode.PARAM_ILLEGAL.getCode(), ErrorCode.PARAM_ILLEGAL.getMsg());
		}
		spiderGundamService.spiderItem(id);
		return new ResultModel();
	}
	
	@RequestMapping("/spiderItemImg")
	public ResultModel spiderItemSimple(Long id) throws Exception {
		if (null == id) {
			return new ResultModel(ErrorCode.PARAM_ILLEGAL.getCode(), ErrorCode.PARAM_ILLEGAL.getMsg());
		}
		spiderGundamService.spiderItemImg(id);
		return new ResultModel();
	}
	
	@RequestMapping("/toggleOpen")
	public ResultModel toggleOpen(Long id) throws Exception {
		if (null == id) {
			return new ResultModel(ErrorCode.PARAM_ILLEGAL.getCode(), ErrorCode.PARAM_ILLEGAL.getMsg());
		}
		spiderGundamService.toggleOpen(id);
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
		PageInfo<ModelItem> pageInfo = spiderGundamService.getModelItems(type, page, size);
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
}
