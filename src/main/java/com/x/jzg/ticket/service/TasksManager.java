package com.x.jzg.ticket.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import org.springframework.stereotype.Service;

@Service
public class TasksManager {

	private CopyOnWriteArrayList<Future<?>> tasks = new CopyOnWriteArrayList<Future<?>>();

	public void registe(Future<?> task) {
		tasks.add(task);
	}
		
	public void cancleAll() {
		
		tasks.forEach(task->{
			if( null != task && !task.isCancelled()) {
				task.cancel(true);
			}
		});
		tasks.clear();
	}
}
