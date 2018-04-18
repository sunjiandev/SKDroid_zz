package com.sks.net.socket.base;

import com.sks.net.socket.message.BaseSocketMessage;


public interface SocketMsgReceiver {
	
	/**
	 * received message;
	 */
	void onMsgReceived(SocketWorker worker,BaseSocketMessage message);

}
