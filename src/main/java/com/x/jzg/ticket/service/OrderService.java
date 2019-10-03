package com.x.jzg.ticket.service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.x.jzg.ticket.context.Ticket;
import com.x.jzg.ticket.listener.OrderListener;
import com.x.jzg.ticket.listener.TicketEvent;
import com.x.jzg.ticket.task.SingleRobTicket;

@Service
public class OrderService {

	private static Logger logger = LoggerFactory.getLogger(OrderService.class); 
	
	@Resource(name="singleBook")
	private ExecutorService singleBookService;
	
	@Autowired
	private OrderManager orderManager;
	
	public void rob() {
		
		Collection<List<List<Ticket>>> allOrderList = orderManager.getAll();
		if(allOrderList == null || allOrderList.isEmpty()) {
			return;
		}
		
		//logger.info("=========刷到"+date+"余票========");
		
		allOrderList.forEach(orderList->{
			orderList.forEach(order->{
				SingleRobTicket task = new SingleRobTicket(order);
				logger.info("下订单："+order.get(0).getTourists().get(0).getName()+order.get(0).getDate());
				singleBookService.submit(task);
			});
		});
	}

	
}
