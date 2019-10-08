package com.x.jzg.ticket.listener;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.x.jzg.ticket.context.Ticket;
import com.x.jzg.ticket.service.OrderManager;
import com.x.jzg.ticket.task.SingleRobTicket;

@Component
public class OrderListener implements ApplicationListener<TicketEvent>{

	private static Logger logger = LoggerFactory.getLogger(OrderListener.class); 
	
	@Resource(name="singleBook")
	private ExecutorService singleBookService;
	
	@Autowired
	private OrderManager orderManager;
	
	@Override
	public void onApplicationEvent(TicketEvent event) {
		
		String date = event.getDate();
		List<List<Ticket>> orderList = orderManager.get(date);
		if(orderList == null || orderList.isEmpty()) {
			return;
		}
		
		logger.info("=========刷到"+date+"余票:"+event.getNum()+"========");
		
		orderList.forEach(order->{
			int expNum = 0;
			for(int i=0; i<order.size(); i++) {
				expNum += order.get(i).getTourists().size();
			}
			if(event.getNum()>expNum) {
				//余票大于要购买的票数
				SingleRobTicket task = new SingleRobTicket(order);
				logger.info("下订单："+order.get(0).getTourists().get(0).getName());
				//立即移除该订单，避免重复提交该订单
				orderManager.removeOrder(order.get(0).getTourists().get(0).getIdno());
				singleBookService.submit(task);
			}
		});
	}

}
