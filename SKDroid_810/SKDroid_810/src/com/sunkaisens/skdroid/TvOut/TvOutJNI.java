package com.sunkaisens.skdroid.TvOut;

public class TvOutJNI {
	
	/**
	 * ������豸��
	 * ��format��Ĭ��yuv420��
	 */
	public static native int nativeTvoutOpen(int format);
	
	/**
	 * ������豸д���ݣ�
	 * ����buffer�Ĵ�СΪָ����͸ߡ�
	 * �����ڲ�ʵ���Զ������720x576�ߴ磬
	 * �����Ļ�����������豸���Ը����ױ�������ʹ�ã������豸��ͨ���ԡ�
	 * �����ڲ�ʵ�ֿ��Կ���ʹ��opengl���ż�����
	 */
	public static native int nativeTvoutWrite(byte[] buffer, int width, int height);
	
	/**
	 * �ر�����豸
	 */
	public static native int nativeTvoutClose();
	
}
