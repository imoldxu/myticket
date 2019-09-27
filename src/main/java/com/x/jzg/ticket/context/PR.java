package com.x.jzg.ticket.context;

public enum PR{
		
		PR_QUANJIAPIAO("06001128","0600112803", "全价票", 259, "0008"),
		PR_LIXIUGANBU("06001143","0600114301","离休干部票", 90, "0008"),
		PR_XUESHENGPIAO ("06001125","0600112502","学生票", 174.5, "0008"),
		PR_ZONGJIAO ("06001134","0600113402","宗教人士票", 174.5, "0008"),
		PR_JUNREN("06001129","0600112901","军人票", 90, "0008"),
		PR_CANJIREN("06001132","0600113201","残疾人票", 90, "0008"),
		PR_LAORENPIAO("06001133","0600113301","老人票",90, "0008"),
		PR_DAOYOUPIAO("06001083","0600108300","导游票",0, "0003"),
		PR_ERTONGPIAO("06001082","0600108200","儿童票",0, "0001");
		
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

		PR(String no, String pcno, String name, double price, String cpxh) {
			this.no = no;
			this.pcno = pcno;
			this.name = name;
			this.price = price;
			this.cpxh = cpxh;
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