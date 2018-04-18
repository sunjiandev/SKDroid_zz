package com.sks.net.socket.message;

import java.math.BigInteger;


public class BCDTools {
	
	public static String BCD2Str(byte[] bytes) {
		StringBuffer temp = new StringBuffer(bytes.length*2);
		for(int i=0;i<bytes.length;i++)
		{
			if((bytes[i]&0x0f) == 0x0f)
				break;
			int nlow = (bytes[i]&0x0f);
			temp.append(Integer.toString(nlow));
			if((bytes[i]&0xf0) == 0xf0)
				break;
			int nhigh = ((bytes[i]&0xf0)>>4)&0x0f;
			temp.append(Integer.toString(nhigh));
		}
		return temp.toString();
	}


	public static byte[] Str2BCD(String value,int targLength) {
		if(targLength==0 || value == null || value.isEmpty())
			return null;
		if(targLength<0)
			targLength = value.length()/2;
		byte[] bcd = new byte[targLength];
		for(int i=0;i<targLength;i++)
		{
			bcd[i] = (byte) 0xff;
		}
		for(int i=0;i<targLength;i++)
		{
			try
			{
				if(value.length()<i*2+1)
					break;
				String num = value.substring(i*2,i*2+1);
				int nlow = Integer.parseInt(num)%10;
				bcd[i] = (byte)(nlow & 0x0f);
					//
				if(value.length()<i*2+2)
				{
					bcd[i] |=(byte)0xf0;
					break;
				}
				num = value.substring(i*2+1,i*2+2);
				int nhigh = Integer.parseInt(num)%10;
				bcd[i] |= (byte)((nhigh<<4) & 0xf0);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return bcd;
	}

}
