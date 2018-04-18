package com.sks.net.socket.base;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import org.doubango.utils.MyLog;

import android.util.Log;

import com.sks.net.socket.message.MessageTools;

public class SocketReader extends Thread {
	private DataInputStream reader;
	private SocketWorker worker = null;
	public static final int SOCKET_READ_ERROR = -101;

	/*
	 */
	public SocketReader(Socket insock, SocketWorker socketWorker)
			throws IOException {
		reader = new DataInputStream(insock.getInputStream());
		worker = socketWorker;
		start();
	}

	//
	public void close() throws IOException {
		reader.close();
		this.interrupt();
	}

	/*
	 * �߳�����ʽ�Ľ������?
	 */
	public void run() {

		while (true) {
			// read the type and length;
			byte[] typebytes = new byte[2];

			try {
				reader.readFully(typebytes);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				Log.e("HX-0328", "typebytes-Exception");
				e1.printStackTrace();
			}

			byte[] lengthbytes = new byte[2];

			try {
				reader.readFully(lengthbytes);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				Log.e("HX-0328", "lengthbytes-Exception");
				e1.printStackTrace();
			}

			//
			int type = MessageTools.bytesToInt(typebytes);
			int nLength = MessageTools.bytesToInt(lengthbytes);
			if (nLength > 0) {
				byte[] packContent = new byte[nLength];
				try {
					reader.readFully(packContent);
				} catch (IOException e) {
					Log.e("HX-0328", "packContent-Exception");
					e.printStackTrace();
				}
				worker.receiveMessage(type, packContent);
			} else {
				worker.receiveMessage(type, null);
			}
		}

	}

}
