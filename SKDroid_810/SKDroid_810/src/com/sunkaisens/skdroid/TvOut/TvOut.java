package com.sunkaisens.skdroid.TvOut;

import org.doubango.ngn.NgnApplication;

import com.sunkaisens.skdroid.util.GlobalSession;

public class TvOut {

	private static final String LIBS_FOLDER = "/system/lib/";
	
	/**
	 * ���ؿ�
	 */
	static {
		if (NgnApplication.isHxSabresd()) { //�Ѷ���ն�
			System.load(LIBS_FOLDER + "libvidout.so");
		}
		if (NgnApplication.isl8848a_l1860() && GlobalSession.bSocketService) { //��о���ն�
			System.load(LIBS_FOLDER + "libmediaout.so");
		}
	}
	
	/**
	 * ������豸��
	 * ��format��Ĭ��yuv420��
	 */
	public static int native_tvout_open(int format) {
		return TvOutJNI.nativeTvoutOpen(format);
	}
	
	/**
	 * ������豸д���ݣ�
	 * ����buffer�Ĵ�СΪָ����͸ߡ�
	 * �����ڲ�ʵ���Զ������720x576�ߴ磬
	 * �����Ļ�����������豸���Ը����ױ�������ʹ�ã������豸��ͨ���ԡ�
	 * �����ڲ�ʵ�ֿ��Կ���ʹ��opengl���ż�����
	 */
	public static int native_tvout_write(byte[] buffer, int width, int height) {
		return TvOutJNI.nativeTvoutWrite(buffer, width, height);
	}
	
	/**
	 * �ر�����豸
	 */
	public static int native_tvout_close() {
		return TvOutJNI.nativeTvoutClose();
	}
	
}
