package com.jokerstation.model.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.jokerstation.model.job.HandleMessageJob;

@Component
public class MessageListHandle implements CommandLineRunner {
	
	@Autowired
	private HandleMessageJob handleMessageJob;

	@Override
	public void run(String... args) throws Exception {
		handleMessageJob.run();
	}
	
	
}
