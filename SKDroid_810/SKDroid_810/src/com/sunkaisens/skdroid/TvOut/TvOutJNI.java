package com.sunkaisens.skdroid.TvOut;

public class TvOutJNI {
	
	/**
	 * 打开输出设备：
	 * （format，默认yuv420）
	 */
	public static native int nativeTvoutOpen(int format);
	
	/**
	 * 向输出设备写数据；
	 * 其中buffer的大小为指定宽和高。
	 * 函数内部实现自动适配成720x576尺寸，
	 * 这样的话，对于这个设备可以更容易被第三方使用，增加设备的通用性。
	 * 函数内部实现可以考虑使用opengl缩放技术。
	 */
	public static native int nativeTvoutWrite(byte[] buffer, int width, int height);
	
	/**
	 * 关闭输出设备
	 */
	public static native int nativeTvoutClose();
	
}
