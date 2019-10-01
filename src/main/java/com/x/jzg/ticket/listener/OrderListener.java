package com.x.jzg.ticket.listener;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.x.jzg.ticket.context.Ticket;

@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OrderListener implements ApplicationListener<TicketEvent>{

	private static Logger logger = LoggerFactory.getLogger(OrderListener.class); 
	
	@Resource(name="singleBook")
	private ExecutorService singleBookService;
	
	private List<Ticket> ticketList;
	
	public OrderListener(List<Ticket> ticketList) {
		this.ticketList = ticketList;
	}
	
	@Override
	public void onApplicationEvent(TicketEvent event) {
		String date = ticketList.get(0).getDate();
		if(event.getDate().equals(date)) {
			//日期匹配
			int expNum = 0;
			for(int i=0;i<ticketList.size();i++) {
				expNum += ticketList.get(0).getTourists().size();
			}
			if(event.getNum()>expNum) {
				//余票大于要购买的票数
				
			}
		}
	}

}
