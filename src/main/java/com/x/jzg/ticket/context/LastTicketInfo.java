package com.x.jzg.ticket.context;

import java.util.List;

/**
 * 每日票数
 * 
 * @author 老徐
 *
 */
public class LastTicketInfo {
	
	private List<DateTicket> dateList;
	private String iscenicid;
	private String szscenicname;
	private String szlasttime;
	
	public List<DateTicket> getDateList() {
		return dateList;
	}
	
	public void setDateList(List<DateTicket> dateList) {
		this.dateList = dateList;
	}
	public String getIscenicid() {
		return iscenicid;
	}
	public void setIscenicid(String iscenicid) {
		this.iscenicid = iscenicid;
	}
	public String getSzscenicname() {
		return szscenicname;
	}
	public void setSzscenicname(String szscenicname) {
		this.szscenicname = szscenicname;
	}
	public String getSzlasttime() {
		return szlasttime;
	}
	public void setSzlasttime(String szlasttime) {
		this.szlasttime = szlasttime;
	}
	
}
