package com.sunkaisens.skdroid.util;

import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnMediaSession;
import org.doubango.ngn.sip.NgnMediaSession.NgnMediaSessionState;

public class GlobalSession {
	public static NgnAVSession avSession;
	public static NgnMediaSession mediaSession;
	/*
	 * true: ���ն���̨����
	 */
	public static boolean bSocketService = false; //true/false ���������ҵ������ӿڷ��񿪹�
	//
	/**
	 * true: ����汾
	 * false: �����汾
	 */
	public static boolean isSocketServicePath = false; //true/false ���������ҵ������ӿڷ��񿪹� ���ֿ��ļ�·����ֵ Ϊ����о��˾���Զ�����
	
	//public static boolean isBigDevice = false;
	
}
