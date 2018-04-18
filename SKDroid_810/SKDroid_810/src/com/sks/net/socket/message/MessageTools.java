package com.sks.net.socket.message;

public class MessageTools {
	public static BaseSocketMessage createMessage(int type,byte[] data)
	{
		switch(type)
		{
		case BaseSocketMessage.MSG_UNDEFINED:
		case BaseSocketMessage.MSG_C_INIT_OK:
			return new BaseSocketMessage(type,data);
		//case BaseSocketMessage.MSG_S_INIT_CONTACT:
		//	return new ContactSocketMessage(type,data);
		default:
			return new BaseSocketMessage(type,data);
		}
	}
	
	/**
	* 操作符 << 的优先级比 & 高
	* intValue = (bytes[3] & 0xFF) << 24
	       | (bytes[2] & 0xFF) << 16
	       | (bytes[1] & 0xFF) <<  8
	       | (bytes[0] & 0xFF) <<  0
	* @param bytes
	* @return
	*/
	public static int bytesToInt (byte[] bytes){
	int length = Math.min(4,bytes.length);
	int intValue = 0;
        for (int i = 0; i < length; i++) {
         int offset = (length-1-i) * 8; 
         intValue |= (bytes[i] & 0xFF) << offset;
        }
       return intValue;
	}
	
	public static byte[] intToBytes(int value,int targlength){
		int length = Math.min(targlength,4);
		byte[] bytes = new byte[length];
		for (int i = 0; i <length; i++) {
		int offset = (length-1-i) * 8; //24, 16, 8
		bytes[i] = (byte) (value >> offset);
		}
		return bytes;
		}
	
	
	public static String bytes2HexString(byte[] b) {
		String ret = ""; 
		for (int i = 0; i < b.length; i++) { 
		String hex = Integer.toHexString(b[i]&0xFF); 
		if (hex.length() == 1) {
		  hex = '0' + hex; 
		  } 
		  ret += hex.toUpperCase(); 
		  } 
		  return ret; 
		} 
	

}
