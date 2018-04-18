package com.sks.net.socket.base;


import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import com.sks.net.socket.message.BaseSocketMessage;
import com.sks.net.socket.message.MessageTools;
import com.sks.net.socket.server.SocketEventCallback;


public class SocketWorker implements SocketMsgReceiver{

	private BlockingQueue<BaseSocketMessage> sendInstantQueue = new ArrayBlockingQueue<BaseSocketMessage>(1000);
	/**
	 */
	private BlockingQueue<Integer> socketErrorQueue = new ArrayBlockingQueue<Integer>(100);
	
	/**
	 */
	private final Semaphore writeAvailable = new Semaphore(1, true);
		

	/**
	 */
	public SocketMsgReceiver msgReceiver = null;
	/**
	 * socket reader writer
	 */
	private Socket socket = null;
	private SocketReader reader = null;
	private SocketWriter writer = null;
	private boolean servermode = true;
	
		
	/**
	 * msgRecv
	 * @throws IOException 
	 */
	public SocketWorker(boolean bServerMode,Socket insocket,
			SocketMsgReceiver msgRecv) throws IOException
	{
		msgReceiver = msgRecv;
		socket = insocket;
		servermode = bServerMode;
		reader = new SocketReader(socket,this);
		writer = new SocketWriter(socket,this);
	}

	public SocketWorker(boolean bServerMode,SocketMsgReceiver msgRecv)
	{
		servermode = bServerMode;
		msgReceiver = msgRecv;
	}

	public void setSocket(Socket insocket) throws IOException
	{
		socket = insocket;
		reader = new SocketReader(socket,this);
		writer = new SocketWriter(socket,this);
	}
	
	public void setReceiver(SocketMsgReceiver msgRecv) throws IOException
	{
		msgReceiver = msgRecv;
	}

	
	public boolean isActive()
	{
		if(socket != null)
			return true;
		else return false;
	}
	
	public void closeAll() throws IOException
	{
		socketErrorQueue.clear();
		sendInstantQueue.clear();
		if(socket != null)
		{
			socket.close();
			socket = null;
		}
		if(reader != null)
		{
			reader.close();
			reader = null;
		}
		if(writer != null)
		{
			writer.close();
			writer = null;
		}
	}

	/**
	 * 
	 */
	public boolean sendMessage(BaseSocketMessage message)
	{
		sendInstantQueue.offer(message);
		writeAvailable.release();
		return true;
	}

	/**
	 * 
	 */
	public boolean receiveMessage(int type,byte[] message)
	{
		if(msgReceiver != null)
			msgReceiver.onMsgReceived(this,MessageTools.createMessage(type,message)); 
		return true;
	}

	/**
	 *
	 */
	public void putErrorNo(int errorno)
	{
		if(servermode)
		{
			try {
				closeAll();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			socketErrorQueue.offer(errorno);
		}
	}
	/**
	 * 
	 */
	public BaseSocketMessage getMessageSend() 
	{
			return sendInstantQueue.poll();
	}

	/**
	 * 
	 */
	public int getNetErrorBlock() throws InterruptedException
	{
		return socketErrorQueue.take();
	}
	/**
	 * 
	 */
	public BaseSocketMessage getMessageSendBlock() throws InterruptedException 
	{
		return sendInstantQueue.take();
	}


	@Override
	public void onMsgReceived(SocketWorker worker,BaseSocketMessage message) {
		// TODO Auto-generated method stub
		
	}
}
