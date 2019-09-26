package com.x.jzg.ticket.context;

import java.util.List;

public class TicketInfo {

	private String data;
	private int ticketType;
	
	public int getTicketType() {
		return ticketType;
	}

	public void setTicketType(int ticketType) {
		this.ticketType = ticketType;
	}

	private List<Tourist> tourists;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public List<Tourist> getTourists() {
		return tourists;
	}

	public void setTourists(List<Tourist> tourists) {
		this.tourists = tourists;
	} 
	
	
}
