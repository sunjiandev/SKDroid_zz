package com.sunkaisens.skdroid.TvOut;

import org.doubango.ngn.NgnApplication;

import com.sunkaisens.skdroid.util.GlobalSession;

public class TvOut {

	private static final String LIBS_FOLDER = "/system/lib/";
	
	/**
	 * 加载库
	 */
	static {
		if (NgnApplication.isHxSabresd()) { //瀚讯大终端
			System.load(LIBS_FOLDER + "libvidout.so");
		}
		if (NgnApplication.isl8848a_l1860() && GlobalSession.bSocketService) { //联芯大终端
			System.load(LIBS_FOLDER + "libmediaout.so");
		}
	}
	
	/**
	 * 打开输出设备：
	 * （format，默认yuv420）
	 */
	public static int native_tvout_open(int format) {
		return TvOutJNI.nativeTvoutOpen(format);
	}
	
	/**
	 * 向输出设备写数据；
	 * 其中buffer的大小为指定宽和高。
	 * 函数内部实现自动适配成720x576尺寸，
	 * 这样的话，对于这个设备可以更容易被第三方使用，增加设备的通用性。
	 * 函数内部实现可以考虑使用opengl缩放技术。
	 */
	public static int native_tvout_write(byte[] buffer, int width, int height) {
		return TvOutJNI.nativeTvoutWrite(buffer, width, height);
	}
	
	/**
	 * 关闭输出设备
	 */
	public static int native_tvout_close() {
		return TvOutJNI.nativeTvoutClose();
	}
	
}
