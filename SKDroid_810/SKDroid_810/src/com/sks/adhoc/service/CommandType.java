package com.sks.adhoc.service;

public class CommandType {

	//自定义Action
	public static final String personOnLine = "com.sunkaisens.skdroid.personOnLine";
	public static final String personOffLine = "com.sunkaisens.skdroid.personOffLine";
	
	public static final String SessionIncomming = "com.sunkaisens.skdroid.SessionIncomming";
	public static final String SessionConnected = "com.sunkaisens.skdroid.SessionConnected";
	public static final String SessionHungUp = "com.sunkaisens.skdroid.SessionHungUp";
	
	public static final int bufferSize = 256;
	public static final int CMD_TYPE1 = 1;
	public static final int CMD_TYPE2 = 2;
	public static final int CMD_TYPE3 = 3;
	
	public static final int CMD_NORMAL_GROUP_AUDIOCALL = 10;
	public static final int CMD_NORMAL_AUDIOCALL_ACCEPT = 11;
	public static final int CMD_SUPER_GROUP_AUDIOCALL = 12;
	public static final int CMD_SUPER_AUDIOCALL_ACCEPT = 13;
	public static final int CMD_AUDIOCALL_REGECT = 14;
	
	public static final int CMD_NORMAL_SESSION_HUNGUP = 15;
	public static final int CMD_SUPER_SESSION_HUNGUP = 16;
	
	public static final int CMD_NORMAL_GROUP_VIDEOCALL = 20;
	public static final int CMD_SUPER_GROUP_VIDEOCALL = 21;
	
	public static final int CMD_PTT_REQUEST = 30;
	public static final int CMD_PTT_RELEASE = 31;
	
	public static final int GROUP_AUDIO_CALL = 40;
	public static final int NORMAL_GROUP_AUDIO_CALL = 41;
	public static final int SUPER_GROUP_AUDIO_CALL = 42;
	
	
	
	
	public static final String BROADCAST_IP = "255.255.255.255";
	public static final String MULTICAST_IP = "224.255.255.255";
	public static final int RECV_PORT = 8876;
	public static final int SEND_PORT = 8866;
	
	public static final int MAX_DELT = 15000;
	
	
	public CommandType() {
		// TODO Auto-generated constructor stub
	}

//	//int to ip转换
//	public static String intToIp(int i) {   
//		//String ip = ( (i >> 24) & 0xFF) +"."+((i >> 16 ) & 0xFF)+"."+((i >> 8 ) & 0xFF)+"."+(i & 0xFF );
//		
//		//return ip;
//		
//		 localIp = (i & 0xFF ) + "." + ((i >> 8 ) & 0xFF) + "." + ((i >> 16 ) & 0xFF) + "." +( i >> 24 & 0xFF) ;
//		 return localIp;
//	}
	
	public static byte[] short2ByteArray(short s) {
		byte[] shortBuf = new byte[2];
		for (int i = 0; i < 2; i++) {
			int offset = (shortBuf.length - 1 - i) * 8;
			shortBuf[i] = (byte) ((s >>> offset) & 0xff);
		}
		return shortBuf;
	}

	public static final int byteArray2Short(byte[] b) {
		return (b[0] << 8) + (b[1] & 0xFF);
	}

	public static byte[] int2ByteArray(int value) {
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++) {
			int offset = (b.length - 1 - i) * 8;
			b[i] = (byte) ((value >>> offset) & 0xFF);
		}
		return b;
	}
	
	public static byte[] int2ByteArray3(int value) {
		byte[] b = new byte[3];
		for (int i = 0; i < 3; i++) {
			int offset = (b.length - 1 - i) * 8;
			b[i] = (byte) ((value >>> offset) & 0xFF);
		}
		return b;
	}
	
	public static byte[] int2ByteArray1(int value) {
		byte[] b = new byte[1];
		for (int i = 0; i < 1; i++) {
			int offset = (b.length - 1 - i) * 8;
			b[i] = (byte) ((value >>> offset) & 0xFF);
		}
		return b;
	}
	
	public static byte int2Byte(int value) {
		byte b = 0;
			b = (byte) (value & 0xFF);
		return b;
	}
	
	public static final int byteArray2Int(byte[] b) {
		return (b[0] << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8)
				+ (b[3] & 0xFF);
	}
	
	public static final int byteArray2Int1(byte[] b) {
		return (b[0] & 0xFF);
	}
	
	public static final int byte2Int(byte b) {
		return (b & 0xFF);
	}
	
	public static final int byteArray2Int3(byte[] b) {
		return ((b[0] & 0xFF) << 16) + ((b[1] & 0xFF) << 8)+ (b[2] & 0xFF);
	}
	
	public static byte int2ByteArray11(int value) {
		byte[] b = new byte[1];
		for (int i = 0; i < 1; i++) {
			int offset = (b.length - 1 - i) * 8;
			b[i] = (byte) ((value >>> offset) & 0xFF);
		}
		return b[0];
	}

	public static byte[] longToByteArray(long a) {
		byte[] bArray = new byte[8];
		for (int i = 0; i < bArray.length; i++) {
			bArray[i] = new Long(a & 0XFF).byteValue();
			a >>= 8;
		}
		return bArray;
	}

	public static long byteArrayToLong(byte[] bArray) {
		long a = 0;
		for (int i = 0; i < bArray.length; i++) {
			a += (long) ((bArray[i] & 0XFF) << (8 * i));
		}
		return a;
	}
	
	
	public static void main(String[] args){
		byte b[] = int2ByteArray1(222);
		System.out.println(byteArray2Int1(b));
	}

}
