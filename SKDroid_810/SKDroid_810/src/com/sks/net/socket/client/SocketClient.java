package com.sks.net.socket.client;


import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.sks.net.socket.base.SocketMsgReceiver;
import com.sks.net.socket.base.SocketReader;
import com.sks.net.socket.base.SocketWorker;
import com.sks.net.socket.base.SocketWriter;
import com.sks.net.socket.message.BaseSocketMessage;
import com.sks.net.socket.server.SocketServer;



public class SocketClient extends Thread{

	public static final int SOCKET_RESTART = -102;
	
	public static final int SOCKET_FLAG_NOTCONNECT = 0;
	public static final int SOCKET_FLAG_CONNECT_READ = 1;
	public static final int SOCKET_FLAG_CONNECT_READWRITE = 2;
	
	private String host;
	private int port = 0;
	private Socket client = null;
	
	private int nextSleepTime = 1000;
	
	private SocketWorker worker = null;
	
	/*
	 */
	public SocketClient(String inhost,int inPort,SocketMsgReceiver receiver)// throws UnknownHostException, IOException
	{
		host = inhost;
		port = inPort;
		worker = new SocketWorker(false,receiver);
		worker.putErrorNo(SOCKET_RESTART);
		this.start();
	}
	/*
	 */
	public void run(){ 
	   while(true)
	    {
	    	try{ 
	    		int nErrorNo = worker.getNetErrorBlock();
	    		if(nErrorNo == SocketWriter.SOCKET_WRITE_ERROR 
	    				|| nErrorNo == SocketReader.SOCKET_READ_ERROR
	    				|| nErrorNo == SocketClient.SOCKET_RESTART
	    				|| client== null || client.isConnected() == false)
	    		{//
	    			System.out.println("connect start!");
	    			Thread.sleep(nextSleepTime);
	    			Connect();
	    		}
	    		Thread.sleep(10);
	    	}
		   catch (Exception  e) {//
			   nextSleepTime += nextSleepTime;
			   worker.putErrorNo(SOCKET_RESTART);
			   System.out.println(e);
		   }finally{
			   try{
				   close();
			   }catch (Exception e1){
				   
			   }
		   }
	    }
	}

	/*
	 */
	public void Connect() throws UnknownHostException, IOException
	{
		if(client != null)
			close();
		if(host == null || port == 0)
			return;
		//
		client = new Socket(host, port);
		worker.setSocket(client);
	}

	/*
	 */
	public void close() throws IOException
	{
		if(client != null)
		{
			worker.closeAll();
			client.close();
		}
		System.out.println("client closed..");
	}
	
    public static void main(String[] args)   
    {  
    	ClientMsgReceiver clientReceiver = new ClientMsgReceiver();
    	SocketClient client = new SocketClient("127.0.0.1",31000,clientReceiver);  
    	byte[] data = new byte[1];
    	data[0] = 0x03;
    	client.worker.sendMessage(new BaseSocketMessage(BaseSocketMessage.MSG_C_SET_SOUNDVOLUME,data));
    	int count =0;
		try {
    	while(count<10000)
    	{
				Thread.sleep(1000);
				client.worker.sendMessage(new BaseSocketMessage(BaseSocketMessage.MSG_C_SET_SOUNDVOLUME,data));
    	}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    } 

}
