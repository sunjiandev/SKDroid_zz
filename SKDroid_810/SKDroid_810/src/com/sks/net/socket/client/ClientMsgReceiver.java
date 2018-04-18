package com.sks.net.socket.client;


import org.doubango.ngn.NgnApplication;

import android.content.Context;
import android.content.Intent;

import com.sks.net.socket.base.SocketMsgReceiver;
import com.sks.net.socket.base.SocketWorker;
import com.sks.net.socket.message.BCDTools;
import com.sks.net.socket.message.BaseSocketMessage;
import com.sunkaisens.skdroid.util.GlobalVar;

public class ClientMsgReceiver implements SocketMsgReceiver {
	public final static String MESSAGE_SOCKET_CLIENT_INTENT = "com.sks.socket.clientmessage";
	
	//socket 事件处理接口
		public static void onSocketReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (MESSAGE_SOCKET_CLIENT_INTENT.equals(action)) {
				int type = intent.getIntExtra("type", 0);
				switch (type) {
				case BaseSocketMessage.MSG_S_AUDIO_INCOMING: { //语音呼入
					String incomingmobile = BCDTools.BCD2Str(intent.getByteArrayExtra("data"));
					System.out.print("来电号码 = " + incomingmobile);
					//ScreenAV.makeCall(soundVolume, NgnMediaType.Audio, SessionType.AudioCall);
				}
				break;
				
				}
			}
		}
	@Override
	public void onMsgReceived(SocketWorker worker,BaseSocketMessage message) {
		// TODO Auto-generated method stub
		System.out.println(message.description());
		Intent i;
		i = new Intent(MESSAGE_SOCKET_CLIENT_INTENT);
		i.putExtra("type", message.getType());
		i.putExtra("data", message.getData());
		//Application.this.sendBroadcast(i);
		if(GlobalVar.orderedbroadcastSign){
			NgnApplication.getContext().sendOrderedBroadcast(i, null);
		}else {
			NgnApplication.getContext().sendBroadcast(i);
		}
	}

}
