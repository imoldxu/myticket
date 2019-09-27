package com.x.jzg.ticket.context;

import java.util.List;

public class TicketInfo {

	private String date;
	private int ticketType;
	private Tourist tourist;
	
	public int getTicketType() {
		return ticketType;
	}

	public void setTicketType(int ticketType) {
		this.ticketType = ticketType;
	}


	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Tourist getTourist() {
		return tourist;
	}

	public void setTourist(Tourist tourist) {
		this.tourist = tourist;
	} 
	
	
}
