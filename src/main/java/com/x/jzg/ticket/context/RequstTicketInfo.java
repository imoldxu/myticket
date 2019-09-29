package com.x.jzg.ticket.context;

import java.util.List;

/**
   *   请求票信息* 
 * @author 老徐
 *
 */
public class RequstTicketInfo {

	private String date;
	private int ticketType;
	private List<Tourist> tourists;
	
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

	public List<Tourist> getTourists() {
		return tourists;
	}

	public void setTourists(List<Tourist> tourists) {
		this.tourists = tourists;
	}
	
}
