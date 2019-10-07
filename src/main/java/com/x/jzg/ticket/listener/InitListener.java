package com.x.jzg.ticket.listener;

import java.util.concurrent.ExecutorService;

import javax.annotation.Resource;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.x.jzg.ticket.task.SingleCheckTask;

@Component
public class InitListener implements ApplicationListener<ApplicationReadyEvent> {

	@Resource(name="singleCheck")
	ExecutorService singleCheck;
	
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		singleCheck.submit(new SingleCheckTask());
	}

}
