package com.x.jzg.ticket.listener;

import org.springframework.context.ApplicationEvent;

public class TicketEvent extends ApplicationEvent{

	private String date;
	private int num;
	
	public TicketEvent(Object source, String date, int num) {
		super(source);
		this.date = date;
		this.num = num;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

}
