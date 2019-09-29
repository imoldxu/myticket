package com.x.jzg.ticket.context;

import java.util.List;

public class Ticket {

	private String Date;
	
	private PR pr;
	
	private List<Tourist> tourists;

	public String getDate() {
		return Date;
	}

	public void setDate(String date) {
		Date = date;
	}

	public PR getPr() {
		return pr;
	}

	public void setPr(PR pr) {
		this.pr = pr;
	}

	public List<Tourist> getTourists() {
		return tourists;
	}

	public void setTourists(List<Tourist> tourists) {
		this.tourists = tourists;
	}
	
}
