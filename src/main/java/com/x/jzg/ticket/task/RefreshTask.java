package com.x.jzg.ticket.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.x.jzg.ticket.service.InitService;

@Component
public class RefreshTask {

	@Autowired
	InitService initService;
	
	@Scheduled(fixedRate=5*60*1000)
	public void refreshSession() {
		//保持session刷新，避免session过期
		initService.refreshSesseion();
	}

	
}
