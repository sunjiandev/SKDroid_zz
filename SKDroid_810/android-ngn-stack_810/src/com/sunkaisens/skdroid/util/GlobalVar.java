package com.sunkaisens.skdroid.util;

import java.util.Date;

import org.doubango.ngn.utils.NgnConfigurationEntry;

public class GlobalVar {
	
	public static boolean bADHocMode = false;
	public static String displayname = "";
	public static String account = "";
	
	public static boolean bBackOrSwitch = false; //����Ƶͨ���У����»��˰�ť�������л������־
	
	public static boolean isVideoDisp = false; //����Ƶͨ���У�������ʾ��
	
	public static boolean isLandscap = true;
	
	public static boolean mSendVideo = true;
	
	public static boolean mLogout = false;
	
	public static String mLocalNum ;
	
	public static String videoMonitorPrefix = "815"; 
	
	public static boolean mCameraIsUsed = false;
	
	public static int mMyPid;
	
	//����㲥����
	public static boolean orderedbroadcastSign = false;
	
	public static String pcscfIp;
	
	public static String mCurrIp = "";
	
	//��������ʱ��
	public static Date mAppStartTime;
	
	public static boolean PTTHasLongClickedDown = false;
	
	public static boolean isSecuriteCardExist = false;
}
