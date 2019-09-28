package com.x.jzg.ticket.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.springframework.stereotype.Service;

@Service
public class TasksManager {

	private ConcurrentHashMap<String, Future<?>> tasks = new ConcurrentHashMap<String, Future<?>>();

	public void registe(String idno, Future<?> task) {
		tasks.put(idno, task);
	}
	
	public void remove(String idno) {
		tasks.remove(idno);
	}
	
	public void cancle(String idno) {
		Future<?> task = tasks.get(idno);
		
		task.cancel(true);
		
		tasks.remove(idno);
	}

	public void cancleAll() {
		
		tasks.forEachValue(4, task->{
			task.cancel(true);
		});
		tasks.clear();
	}
}
