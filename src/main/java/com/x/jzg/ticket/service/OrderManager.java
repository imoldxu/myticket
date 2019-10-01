package com.x.jzg.ticket.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.x.jzg.ticket.context.Ticket;
import com.x.jzg.ticket.listener.OrderListener;

@Service
public class OrderManager {

	private static Logger logger = LoggerFactory.getLogger(OrderManager.class); 
	
	private Map<String, OrderListener> map = new ConcurrentHashMap<String, OrderListener>();
	
	public void registerOrder(String idno, OrderListener order) {
		OrderListener oldorder = map.putIfAbsent(idno, order);
		if(order != oldorder) {
			logger.info("相同的身份证号不可重复抢");
		}
	}
	
	public void removeOrder(String idno) {
		map.remove(idno);
	}
	
	public void clear() {
		map.clear();
	}
	
}
