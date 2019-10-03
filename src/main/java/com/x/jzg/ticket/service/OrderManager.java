package com.x.jzg.ticket.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.x.jzg.ticket.context.Ticket;

@Service
public class OrderManager {

	private static Logger logger = LoggerFactory.getLogger(OrderManager.class); 
	
	private Map<String, List<List<Ticket>>> dateMap = new ConcurrentHashMap<String, List<List<Ticket>>>();
	private Map<String, List<Ticket>> idMap = new ConcurrentHashMap<String, List<Ticket>>();
	
	public synchronized void registerOrder(String idno, List<Ticket> order) {
		idMap.putIfAbsent(idno, order);
		String date = order.get(0).getDate();
		List<List<Ticket>> dateList = dateMap.get(date);
		if(null == dateList) {
			dateList = new ArrayList<List<Ticket>>();
			dateMap.put(date, dateList);
		}
		dateList.add(order);
		logger.info(order.get(0).getTourists().get(0).getName()+date+"添加抢票成功");
	}
	
	public synchronized Collection<List<List<Ticket>>> getAll() {
		Collection<List<List<Ticket>>> orders = dateMap.values();
		return orders;
	}
	
	public synchronized void removeOrder(String idno) {
		List<Ticket> order = idMap.remove(idno);
		String date = order.get(0).getDate();
		List<List<Ticket>> dateList = dateMap.get(date);
		if(null == dateList) {
			dateList = new CopyOnWriteArrayList<List<Ticket>>();
			dateMap.put(date, dateList);
		}
		dateList.remove(order);
		logger.info("取消"+order.get(0).getTourists().get(0).getName()+date+"的抢票");
	}
	
	public synchronized void clear() {
		idMap.clear();
		dateMap.clear();
		logger.info("取消所有的抢票");
	}

	public synchronized List<List<Ticket>> get(String date) {
		return dateMap.get(date);
	}
	
}
