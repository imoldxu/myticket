package com.x.jzg.ticket.context;

public enum PR{
		
		PR_QUANJIAPIAO("06001128","0600112803", "全价套票", 259, "0008", "&01&1&0008&0008&", "06001128&0600112803&259.0&01&1&0008&0008&06001&"),
		PR_LIXIUGANBU("06001143","0600114301","离休干部免门票套票", 90, "0008", "&01&1&0008&0008&", "06001143&0600114301&90.0&01&1&0008&0008&06001&"),
		PR_XUESHENGPIAO ("06001125","0600112503","学生优惠套票", 174.5, "0008","&01&1&0008&0008&", "06001125&0600112503&174.5&01&1&0008&0008&06001&"),
		PR_ZONGJIAO ("06001134","0600113403","宗教人士优惠套票", 174.5, "0008", "&01&1&0008&0008&", "06001134&0600113403&174.5&01&1&0008&0008&06001&"),
		PR_JUNREN("06001129","0600112902","军人免门票套票", 90, "0008", "&01&1&0008&0008&", "06001129&0600112902&90.0&01&1&0008&0008&06001&"),
		PR_CANJIREN("06001132","0600113202","残疾人票", 90, "0008", "&01&1&0008&0008&", "06001132&0600113202&90.0&01&1&0008&0008&06001&"),
		PR_LAORENPIAO("06001133","0600113302","（60岁以上老人）免门票套票",90, "0008", "&01&1&0008&0008&", "06001133&0600113302&90.0&01&1&0008&0008&06001&"),
		PR_DAOYOUPIAO("06001083","0600108302","导游票", 0, "0003", "&01&1&0003&0003&", "06001083&0600108302&0.0&01&1&0003&0003&06001&"),
		PR_ERTONGPIAO("06001082","0600108202","儿童免票 ", 0, "0001", "&01&1&0001&0001&", "06001082&0600108202&0.0&01&1&0001&0001&06001&");
		
		public String getNo() {
			return no;
		}

		public String getName() {
			return name;
		}

		public double getPrice() {
			return price;
		}

		public String getPcno() {
			return pcno;
		}


		public void setPcno(String pcno) {
			this.pcno = pcno;
		}
		
		String no;
		String pcno;
		String name;
		String jval;
		String oldval; //用oldval+时间，可以下订单时拼接处prno提交的值
		
		public String getOldval() {
			return oldval;
		}

		public void setOldval(String oldval) {
			this.oldval = oldval;
		}

		public String getJval() {
			return jval;
		}

		public void setJval(String jval) {
			this.jval = jval;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setPrice(double price) {
			this.price = price;
		}

		double price;
		String cpxh;
		
		public String getCpxh() {
			return cpxh;
		}

		public void setCpxh(String cpxh) {
			this.cpxh = cpxh;
		}

		PR(String no, String pcno, String name, double price, String cpxh, String jval, String oldval) {
			this.no = no;
			this.pcno = pcno;
			this.name = name;
			this.price = price;
			this.cpxh = cpxh;
			this.jval = jval;
			this.oldval = oldval;
		}
		
		public static PR getPR(int type) {
			PR pr = null;
			switch (type) {
			case 1:
				pr = PR.PR_QUANJIAPIAO;
				break;
			case 2:
				pr = PR.PR_XUESHENGPIAO;
				break;
			case 3:
				pr = PR.PR_LAORENPIAO;
				break;
			case 4:
				pr = PR.PR_ERTONGPIAO;
				break;
			case 5:
				pr = PR.PR_DAOYOUPIAO;
				break;
			case 6://离休干部票
				pr = PR.PR_LIXIUGANBU;
				break;
			case 7://军人票
				pr = PR.PR_JUNREN;
				break;
			case 8:
				pr = PR.PR_CANJIREN;
			default:
				pr = null;
				break;
			}
			return pr;
		}
	}