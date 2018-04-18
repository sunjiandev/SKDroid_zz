package com.sks.net.socket.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.doubango.utils.MyLog;

import android.util.Log;

import com.sks.net.socket.base.SocketWorker;
import com.sks.net.socket.message.BaseSocketMessage;
import com.sunkaisens.skdroid.util.GlobalSession;

public class SocketServer
{
	private final static String TAG = SocketServer.class.getCanonicalName();
    private ServerSocket ss;  
    private ServerMsgReceiver sreceiver = new ServerMsgReceiver();
    public static List<SocketWorker> workerList = new ArrayList<SocketWorker>(2);
  
    public SocketServer(SocketEventCallback mCallback)
    {
		Log.d(TAG, "SocketServer()");
        try   
        {  
        	sreceiver.mSocketCallback = mCallback;
        	
            /**
             * ip: 127.0.0.1
             * port: 31000
             */
//        	ss = new ServerSocket(31000,0,InetAddress.getByName("127.0.0.1")); //非本机执行出错
        	ss = new ServerSocket(BaseSocketMessage.MSG_SOCKET_PORT);
        	System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> sksocket:The server is waiting ...");
    		Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> sksocket:The server is waiting ...");
            while(true)   
            {  
                try   
                {  
                	Socket socket = ss.accept();
					SocketWorker worker = new SocketWorker(true, socket,
							sreceiver);
                	workerList.add(worker);
                	MyLog.d(TAG, "create a worker");
                }catch (IOException e) {  
                     e.printStackTrace();
                }
          }  
        } catch (IOException e) {  
        	System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> sksocket:The server start error!");
    		MyLog.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> sksocket:The server start error!");            
            e.printStackTrace();
        }  
        finally
        {
        	if(ss != null){
	        	try {
					ss.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
    }
    
    /**
     * 业务软件向适配软件发送信息
     * @param msg
     */
    public static void sendMessage(BaseSocketMessage msg)
    {  
    	if(GlobalSession.bSocketService == false)
    		return;
    	for(int i=0;i<workerList.size();i++)
    	{
    		SocketWorker worker = workerList.get(i);
    		if(worker.isActive())
    		{
    			worker.sendMessage(msg);
    		}
    		else
    		{
    			workerList.remove(i);
    			i--;
    		}
    		
    	}
    }
  
//    public static void main(String[] args)   
//    {  
//        new SocketServer();  
//    } 
}
