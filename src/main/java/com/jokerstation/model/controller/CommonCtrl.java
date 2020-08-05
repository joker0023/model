package com.jokerstation.model.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class CommonCtrl {
	
	private static Logger logger = LoggerFactory.getLogger(CommonCtrl.class);
	
	@RequestMapping("/test")
	public String test() {
		logger.info("test in ...");
//		logger.debug("debug log test");
//		loggenir.info("info log test");
//		logger.warn("warn log test");
//		logger.error("error log test");
		return "success";
	}
}
