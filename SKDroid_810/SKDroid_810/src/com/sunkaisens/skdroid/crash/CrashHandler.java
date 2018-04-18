package com.sunkaisens.skdroid.crash;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.utils.MyLog;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.util.GlobalSession;
import com.sunkaisens.skdroid.util.GlobalVar;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告.
 * 
 * @author user
 * 
 */
public class CrashHandler implements UncaughtExceptionHandler {

	public static final String TAG = "CrashHandler";

	// 系统默认的UncaughtException处理类
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	// CrashHandler实例
	private static CrashHandler INSTANCE = new CrashHandler();
	// 程序的Context对象
	private Context mContext;
	// 用来存储设备信息和异常信息
	private Map<String, String> infos = new HashMap<String, String>();

	// 用于格式化日期,作为日志文件名的一部分
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	/** 保证只有一个CrashHandler实例 */
	private CrashHandler() {
	}

	/** 获取CrashHandler实例 ,单例模式 */
	public static CrashHandler getInstance() {
		return INSTANCE;
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		mContext = context;
		// 获取系统默认的UncaughtException处理器
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		// 设置该CrashHandler为程序的默认处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 当UncaughtException发生时会转入该函数来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {

			// 如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			// 退出程序
			// android.os.Process.killProcess(android.os.Process.myPid());
			// System.exit(1); //异常退出
		}
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
	 * 
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false.
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}

		// 使用Toast来显示异常信息
		// new Thread() {
		// @Override
		// public void run() {
		// Looper.prepare();
		// // Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出.",
		// Toast.LENGTH_LONG).show();
		// // Toast.makeText(mContext, "程序出现异常,日志已保存",
		// Toast.LENGTH_LONG).show();
		// Looper.loop();
		// }
		// }.start();
		// 收集设备参数信息
		collectDeviceInfo(mContext);
		// 保存日志文件
		saveCrashInfo2File(ex);
		return true;
	}

	/**
	 * 收集设备参数信息
	 * 
	 * @param ctx
	 */
	public void collectDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();

			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null"
						: pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {
			Log.e(TAG, "an error occured when collect package info", e);
		}
		// 不再收集设备信息
		// Field[] fields = Build.class.getDeclaredFields();
		// for (Field field : fields) {
		// try {
		// field.setAccessible(true);
		// infos.put(field.getName(), field.get(null).toString());
		// Log.d(TAG, field.getName() + " : " + field.get(null));
		// } catch (Exception e) {
		// Log.e(TAG, "an error occured when collect crash info", e);
		// }
		// }
	}

	/**
	 * 保存错误信息到文件中
	 * 
	 * @param ex
	 * @return 返回文件名称,便于将文件传送到服务器
	 */
	private String saveCrashInfo2File(Throwable ex) {
		Log.d(TAG, "saveCrashInfo2File()");
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : infos.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n");
		}
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		Log.d(TAG, "saveCrashInfo2File(): result = " + result);
		sb.append(result);
		FileOutputStream fos = null;
		try {
			long timestamp = System.currentTimeMillis();
			String time = formatter.format(new Date());
			String fileName = "crash-" + time + "-" + timestamp + ".log";
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				// String path = "/sdcard/crash/";

				String path = SystemVarTools.crashPath;
				if (GlobalSession.bSocketService == true) {
					path = "/data/data/crash/";
				}
				File dir = new File(path);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				fos = new FileOutputStream(path + fileName);
				fos.write(sb.toString().getBytes());
				fos.flush();
			}
			return fileName;
		} catch (Exception e) {
			Log.e(TAG, "an error occured while writing file...", e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * 网络是否可用
	 * 这些信息对于开发者来说帮助极大，所以我们需要将此日志文件上传到服务器，有关文件上传的技术，请参照Android中使用HTTP服务相关介绍。
	 * 不过在使用HTTP服务之前，需要确定网络畅通，我们可以使用下面的方式判断网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager mgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] info = mgr.getAllNetworkInfo();
		if (info != null) {
			for (int i = 0; i < info.length; i++) {
				if (info[i].getState() == NetworkInfo.State.CONNECTED) { // WIFI
																			// 1
																			// 手机
																			// wifi
																			// 1
																			// 手持台
					return true;
				}
				if (info[i].getType() == ConnectivityManager.TYPE_ETHERNET) { // ETHERNET
																				// 9
																				// 大终端车载台
																				// 正样PAD
																				// 手持台
					return true;
				}
				if (info[i].getTypeName().equals("LTE")) { // LTE 14 正样PAD 手持台
					return true;
				}
			}
		}
		return false;
	}

	private static boolean mNetworkSign = true;

	public static boolean isNetworkAvailable() {

		if (GlobalVar.bADHocMode) {
			return true;
		}
		ConnectivityManager cm = SKDroid.getConnectivityManager();
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null && ni.isConnected()) {
			MyLog.d(TAG, "网络已连接");
			Engine.getInstance().getNetworkService().setNetworkEnable(true);
			return true;
		} else {
			INgnConfigurationService mConfigurationService = Engine
					.getInstance().getConfigurationService();

			String cscf = mConfigurationService.getString(
					NgnConfigurationEntry.NETWORK_PCSCF_HOST,
					NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_HOST);
			String AppServer = mConfigurationService.getString(
					NgnConfigurationEntry.NETWORK_PCSCF_HOST,
					NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_HOST);
			Log.d(TAG, "isNetworkAvailable cscf:" + cscf);
			mNetworkSign = true;
			try {
				if (cscf != null) {
					final Process process = Runtime.getRuntime().exec(
							"ping -c 3 " + cscf);
					int status = process.waitFor();
					process.destroy();
					Log.d(TAG, "isNetworkAvailable status:" + status);
					if (mNetworkSign && status == 0) {
						Engine.getInstance().getNetworkService()
								.setNetworkEnable(true);
						return true;
					} else {
						Engine.getInstance().getNetworkService()
								.setNetworkEnable(false);
						return false;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return false;
	}

	public static boolean isNetworkAvailableDomain() {

		if (GlobalVar.bADHocMode) {
			return true;
		}
		ConnectivityManager cm = SKDroid.getConnectivityManager();
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null && ni.isConnected()) {
			MyLog.d(TAG, "网络已连接");
			Engine.getInstance().getNetworkService().setNetworkEnable(true);
			return true;
		} else {
			INgnConfigurationService mConfigurationService = Engine
					.getInstance().getConfigurationService();
			mNetworkSign = true;
			InputStreamReader isr = null;
			BufferedReader br = null;
			try {

				if (GlobalVar.pcscfIp != null) {
					final Process process = Runtime.getRuntime().exec(
							"ping -c 3 " + GlobalVar.pcscfIp);
					isr = new InputStreamReader(process.getInputStream());
					br = new BufferedReader(isr);

					int status = process.waitFor();
					Log.d(TAG, "isNetworkAvailable status:" + status
							+ "  cscf=" + GlobalVar.pcscfIp);

					// 如果链路是通的，则从ping返回的结果中截取IP
					if (mNetworkSign && status == 0) {
						Engine.getInstance().getNetworkService()
								.setNetworkEnable(true);
						// 如果配置的cscf地址不是IP 则从PING的返回结果中截取IP 并写入配置项 以供注册时使用
						// if (!NgnUriUtils.checkIPAddress(GlobalVar.pcscfIp)) {
						// // 从PING返回的结果中截取cscf的IP地址
						// // 正常 PING test.com (192.168.1.192) 56(84) bytes of
						// // data.
						// try {
						// String str1 = br.readLine();
						// String ip = str1.substring(str1.indexOf("(") + 1,
						// str1.indexOf(")"));
						// MyLog.d(TAG, "Process ip: " + ip + "  str=" + str1);
						// if (ip != null && NgnUriUtils.checkIPAddress(ip)) {
						// GlobalVar.pcscfIp = ip;
						// MyLog.d(TAG, "pcscf=" + GlobalVar.pcscfIp);
						// } else {
						// MyLog.e(TAG, "IP格式不合法.");
						// }
						// } catch (Exception e) {
						// e.printStackTrace();
						// }
						// }
						return true;
					} else {
						// 链路不通时
						Engine.getInstance().getNetworkService()
								.setNetworkEnable(false);
						// GlobalVar.pcscfIp = mConfigurationService
						// .getString(
						// NgnConfigurationEntry.NETWORK_PCSCF_HOST,
						// NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_HOST);
						// MyLog.d(TAG, "pcscf=" + GlobalVar.pcscfIp);

						return false;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (isr != null) {
						isr.close();
					}
					if (br != null) {
						br.close();
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}

			}

		}
		return false;
	}

	public static void checkDomain() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				isNetworkAvailable();
			}
		}).start();
	}

	public static boolean isCdmaNetwork() {
		if (!SystemVarTools.useCdmaNetwork) {
			return false;
		}
		ConnectivityManager mgr = (ConnectivityManager) NgnApplication
				.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] infos = mgr.getAllNetworkInfo();
		for (NetworkInfo info : infos) {
			Log.d(TAG, "NetworkInfo typeName = " + info.getTypeName()
					+ "    type = " + info.getType());
			if (info.getType() == TelephonyManager.NETWORK_TYPE_CDMA
					&& info.isAvailable()) {
				return true;
			}
		}
		for (NetworkInfo info : infos) {
			if (info.getTypeName().contains("mobile") && info.isAvailable()) {
				return true;
			}
		}
		return false;
	}

	public static boolean isNetworkAvailable2() {
		// TODO Auto-generated method stub
		if (Engine.getInstance().getNetworkService() == null) {
			return false;
		}
		if (GlobalVar.bADHocMode == true) {
			return true;
		}
		return Engine.getInstance().getNetworkService().isNetworkEnable();
	}
}