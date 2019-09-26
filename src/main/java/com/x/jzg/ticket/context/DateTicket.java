package com.x.jzg.ticket.context;

import java.util.List;

public class DateTicket {

		private String date;

		private List<LastTicket> numberList;

		private int type;

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public List<LastTicket> getNumberList() {
			return numberList;
		}

		public void setNumberList(List<LastTicket> numberList) {
			this.numberList = numberList;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}
	}