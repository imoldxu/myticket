package com.x.jzg.ticket.context;

public class OrderInfo{
//		{pd06001:
//			{
//				pd:'06001',
//				c:'%E4%B9%9D%E5%AF%A8%E6%B2%9F',
//				d:'2019-09-27',
//				pr:[{
//					c:'0600108302',
//					m:'%E5%AF%BC%E6%B8%B8%E7%A5%A8',
//					e:0,
//					n:1,
//					v:'&01&1&0003&0003&',
//					u:'%E4%BD%8D',
//					t:'3402400',
//					r:'06001083'
//					}]
//			}
//		}
		PrInfo pd06001;

		public PrInfo getPd06001() {
			return pd06001;
		}

		public void setPd06001(PrInfo pd06001) {
			this.pd06001 = pd06001;
		}
	}
