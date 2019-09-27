package com.x.jzg.ticket.listener;

import org.springframework.context.ApplicationEvent;

public class TicketEvent extends ApplicationEvent{

	public TicketEvent(Object source) {
		super(source);
	}

}
