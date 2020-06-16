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

	@RequestMapping("/pg")
	public ResultModel spiderPG() throws Exception {
		logger.info("spider pg...");
		spiderGundamService.spiderPG();
		return new ResultModel();
	}
	
	@RequestMapping("/mg")
	public ResultModel spiderMG() throws Exception {
		logger.info("spider mg...");
		spiderGundamService.spiderMG();
		return new ResultModel();
	}
	
	@RequestMapping("/rg")
	public ResultModel spiderRG() throws Exception {
		logger.info("spider rg...");
		spiderGundamService.spiderRG();
		return new ResultModel();
	}
	
	@RequestMapping("/sd")
	public ResultModel spiderSD() throws Exception {
		logger.info("spider sd...");
		spiderGundamService.spiderSD();
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
				try {
					Thread.sleep(random.nextInt(3000) + 2000);
					spiderGundamService.spiderItem(Long.valueOf(id));
				} catch (Exception e) {
					logger.error("spider one in list error", e);
				}
			}
		});
		
		return new ResultModel();
	}
	
	@RequestMapping("/spiderItem")
	public ResultModel spiderItem(Long id) throws Exception {
		if (null == id) {
			return new ResultModel(ErrorCode.PARAM_ILLEGAL.getCode(), ErrorCode.PARAM_ILLEGAL.getMsg());
		}
		spiderGundamService.spiderItem(id);
		return new ResultModel();
	}
	
	@RequestMapping("/spiderItemSimple")
	public ResultModel spiderItemSimple(Long id) throws Exception {
		if (null == id) {
			return new ResultModel(ErrorCode.PARAM_ILLEGAL.getCode(), ErrorCode.PARAM_ILLEGAL.getMsg());
		}
		spiderGundamService.spiderItemSimple(id);
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
}
