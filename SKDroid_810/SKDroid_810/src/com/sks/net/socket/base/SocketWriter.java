package com.sks.net.socket.base;


import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.sks.net.socket.message.BaseSocketMessage;
import com.sks.net.socket.message.MessageTools;

public class SocketWriter extends Thread {
	
	public static final int SOCKET_WRITE_ERROR = -100;
	private DataOutputStream writer;
	private SocketWorker worker = null;
	
	/*
	 * ���캯��
	 */
	public SocketWriter(Socket insock, SocketWorker socketWorker) throws IOException
	{
		writer = new DataOutputStream(insock.getOutputStream());  
		worker = socketWorker;
		start();
	}
	public void close() throws IOException
	{
		writer.close();
		this.interrupt();
	}
	/**
	 * 
	 */
	public void run(){ 

		try
		{
			System.out.println("SKDroid SocketWriter: socket writer start!...");
			while(true)
			{
				BaseSocketMessage message = worker.getMessageSendBlock();
				while(message!=null)
				{//
					System.out.println("SKDroid SocketWriter: " + message.description());
					int datalength = 0;
					if(message.getData() != null )
						datalength = message.getData().length;
					byte[] data = new byte[2 + 2 + datalength];
					byte[] type = MessageTools.intToBytes(message.getType(),2);
					byte[] len = MessageTools.intToBytes(datalength,2);
					System.arraycopy(type, 0, data, 0, 2);
					System.arraycopy(len, 0, data, 2, 2);
					if(datalength>0)
					{
						System.arraycopy(message.getData(), 0, data, 4, datalength);
					}
					try {
						writer.write(data);
						writer.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
//					writer.write(data);
//					writer.flush();
					message = worker.getMessageSend();
					Thread.sleep(10);
				}
				Thread.sleep(10);
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		catch(Exception e)
		{
			worker.putErrorNo(SOCKET_WRITE_ERROR);
			e.printStackTrace();
		}
	}

}
