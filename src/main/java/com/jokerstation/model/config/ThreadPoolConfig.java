package com.jokerstation.model.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadPoolConfig {

	@Bean
	public ThreadPoolTaskExecutor downloadPool() {
		int corePoolSize = (int)Math.ceil(50);
		ThreadPoolTaskExecutor scheduler = new ThreadPoolTaskExecutor();
		scheduler.setCorePoolSize(corePoolSize);
		scheduler.setMaxPoolSize(100);
		scheduler.setQueueCapacity(1000);
		scheduler.setKeepAliveSeconds(3600);
		scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		return scheduler;
	}
}
