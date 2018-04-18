package com.sks.net.socket.message;

public class ContactSocketMessage extends BaseSocketMessage {

	
	/*
	 * 此处可以加入自定义的变量
	 */
	
	public ContactSocketMessage(int type,byte[] indata)
	{	
		super(type,indata);
		//可以加入对indata的数据解析
	}
	
	
}
