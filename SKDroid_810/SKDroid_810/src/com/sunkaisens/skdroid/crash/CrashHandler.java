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
 * UncaughtException������,��������Uncaught�쳣��ʱ��,�и������ӹܳ���,����¼���ʹ��󱨸�.
 * 
 * @author user
 * 
 */
public class CrashHandler implements UncaughtExceptionHandler {

	public static final String TAG = "CrashHandler";

	// ϵͳĬ�ϵ�UncaughtException������
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	// CrashHandlerʵ��
	private static CrashHandler INSTANCE = new CrashHandler();
	// �����Context����
	private Context mContext;
	// �����洢�豸��Ϣ���쳣��Ϣ
	private Map<String, String> infos = new HashMap<String, String>();

	// ���ڸ�ʽ������,��Ϊ��־�ļ�����һ����
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	/** ��ֻ֤��һ��CrashHandlerʵ�� */
	private CrashHandler() {
	}

	/** ��ȡCrashHandlerʵ�� ,����ģʽ */
	public static CrashHandler getInstance() {
		return INSTANCE;
	}

	/**
	 * ��ʼ��
	 * 
	 * @param context
	 */
	public void init(Context context) {
		mContext = context;
		// ��ȡϵͳĬ�ϵ�UncaughtException������
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		// ���ø�CrashHandlerΪ�����Ĭ�ϴ�����
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * ��UncaughtException����ʱ��ת��ú���������
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {

			// ����û�û�д�������ϵͳĬ�ϵ��쳣������������
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			// �˳�����
			// android.os.Process.killProcess(android.os.Process.myPid());
			// System.exit(1); //�쳣�˳�
		}
	}

	/**
	 * �Զ��������,�ռ�������Ϣ ���ʹ��󱨸�Ȳ������ڴ����.
	 * 
	 * @param ex
	 * @return true:��������˸��쳣��Ϣ;���򷵻�false.
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}

		// ʹ��Toast����ʾ�쳣��Ϣ
		// new Thread() {
		// @Override
		// public void run() {
		// Looper.prepare();
		// // Toast.makeText(mContext, "�ܱ�Ǹ,��������쳣,�����˳�.",
		// Toast.LENGTH_LONG).show();
		// // Toast.makeText(mContext, "��������쳣,��־�ѱ���",
		// Toast.LENGTH_LONG).show();
		// Looper.loop();
		// }
		// }.start();
		// �ռ��豸������Ϣ
		collectDeviceInfo(mContext);
		// ������־�ļ�
		saveCrashInfo2File(ex);
		return true;
	}

	/**
	 * �ռ��豸������Ϣ
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
		// �����ռ��豸��Ϣ
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
	 * ���������Ϣ���ļ���
	 * 
	 * @param ex
	 * @return �����ļ�����,���ڽ��ļ����͵�������
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
	 * �����Ƿ����
	 * ��Щ��Ϣ���ڿ�������˵������������������Ҫ������־�ļ��ϴ������������й��ļ��ϴ��ļ����������Android��ʹ��HTTP������ؽ��ܡ�
	 * ������ʹ��HTTP����֮ǰ����Ҫȷ�����糩ͨ�����ǿ���ʹ������ķ�ʽ�ж������Ƿ����
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
																			// �ֻ�
																			// wifi
																			// 1
																			// �ֳ�̨
					return true;
				}
				if (info[i].getType() == ConnectivityManager.TYPE_ETHERNET) { // ETHERNET
																				// 9
																				// ���ն˳���̨
																				// ����PAD
																				// �ֳ�̨
					return true;
				}
				if (info[i].getTypeName().equals("LTE")) { // LTE 14 ����PAD �ֳ�̨
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
			MyLog.d(TAG, "����������");
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
			MyLog.d(TAG, "����������");
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

					// �����·��ͨ�ģ����ping���صĽ���н�ȡIP
					if (mNetworkSign && status == 0) {
						Engine.getInstance().getNetworkService()
								.setNetworkEnable(true);
						// ������õ�cscf��ַ����IP ���PING�ķ��ؽ���н�ȡIP ��д�������� �Թ�ע��ʱʹ��
						// if (!NgnUriUtils.checkIPAddress(GlobalVar.pcscfIp)) {
						// // ��PING���صĽ���н�ȡcscf��IP��ַ
						// // ���� PING test.com (192.168.1.192) 56(84) bytes of
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
						// MyLog.e(TAG, "IP��ʽ���Ϸ�.");
						// }
						// } catch (Exception e) {
						// e.printStackTrace();
						// }
						// }
						return true;
					} else {
						// ��·��ͨʱ
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