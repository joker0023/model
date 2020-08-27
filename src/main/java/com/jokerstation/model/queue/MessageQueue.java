package com.jokerstation.model.queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.jokerstation.common.util.JsonUtils;
import com.jokerstation.model.vo.MapMessage;

@Component
public class MessageQueue {
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	
	private final static String MESSAGE_QUEUE_KEY = "map_chat_message_queue_key";
	
	public void push(String msg) {
		stringRedisTemplate.opsForList().rightPush(MESSAGE_QUEUE_KEY, msg);
	}
	
	public MapMessage pop() {
		String json = stringRedisTemplate.opsForList().leftPop(MESSAGE_QUEUE_KEY);
		if (null != json) {
			return JsonUtils.toBean(json, MapMessage.class);
		}
		return null;
	}
	
	public long size() {
		return stringRedisTemplate.opsForList().size(MESSAGE_QUEUE_KEY);
	}
}
