package com.sunkaisens.skdroid.Services;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.TimerTask;

import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.services.impl.NgnSipService;
import org.doubango.ngn.sip.NgnSipSession.ConnectionState;
import org.doubango.ngn.sip.NgnSipStack;
import org.doubango.ngn.sip.NgnSipStack.STACK_STATE;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.utils.MyLog;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.MessageTypes;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.crash.CrashHandler;
import com.sunkaisens.skdroid.util.GlobalVar;

import android.R.integer;
import android.R.string;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Debug.MemoryInfo;
import android.os.Handler;
import android.provider.Settings.Global;
import android.util.Log;
import android.view.inputmethod.InputBinding;

public class ServiceRegiste {

	private static final String TAG = ServiceRegiste.class.getCanonicalName();

	private static ServiceRegiste mInstance = null;

	private Handler mNetHandler = null;

	public static boolean neededreset = false;

	public static boolean isNeedRedownloadContacts = true;

	public static boolean isIpChange = false;

	public static int NET_REGISTE = 1001;
	public static int NET_STACK_STOP = 1002;

	public static int NET_DOWNLOAD_CONTACTS = 1003;

	private int checkedCount = 0;

	/**
	 * 协议栈关闭最大检测次数
	 */
	private final static int mStackCheckCountMax = 3;
	/**
	 * 协议栈关闭检测次数
	 */
	private int mStackCheckCount = 0;

	public static ServiceRegiste getservice() {
		if (mInstance == null) {
			mInstance = new ServiceRegiste();
		}
		return mInstance;
	}

	public void networkCheck() {
		try {
			MyLog.d(TAG, "networkCheck()");
			
			MyLog.d(TAG, "bLogin=" + SystemVarTools.bLogin);

			MyLog.i(TAG, "Start checking network...");

			NgnTimer netCheckTimer = new NgnTimer();
			TimerTask netCheckTask = new TimerTask() {


				@Override
				public void run() {
					INgnSipService sipService = Engine.getInstance()
							.getSipService();

//					printMemoryInfos();

					// 发送网络检测消息
//					if (!isIpChange && CrashHandler.isNetworkAvailable()) {
//						MyLog.i(TAG, String.format("network connected, needreset: %s", neededreset));
//
//						if (Engine.getInstance().getNetworkService() != null)
//							Engine.getInstance().getNetworkService()
//									.setNetworkEnable(true);
//
//						if (neededreset) {
//							if(sipService.getSipStack()==null){
//								try {
//									boolean result = sipService.register(SKDroid.getContext());
//									if(result){
//										ServiceRegiste
//											.sendRegStatus(MessageTypes.MSG_REG_INPROGRESS);
//										SystemVarTools.isSubscribeSended = false;
//										neededreset = false;
//									}
//									MyLog.d(TAG, "updateStack  neededreset="+neededreset);
//								} catch (NullPointerException e) {
//									neededreset = true;
//									e.printStackTrace();
//								}
//							}else {
//								MyLog.d(TAG, "SipStack is not completely stooped.");
//								mStackCheckCount ++ ;
//							}
					
							
//						} else {
//							if (sipService.isRegisteSessionConnected() == true) {
//								ServiceRegiste
//										.sendRegStatus(MessageTypes.MSG_REG_OK);
//							} else {
//								ServiceRegiste
//										.sendRegStatus(MessageTypes.MSG_REG_NOK);
//							}
//						}
//						if (checkedCount > 0) {
//							checkedCount = 0;
//						}
						
					if(CrashHandler.isNetworkAvailable()) {	
						MyLog.i(TAG, "network is available");
						
						if (sipService.isRegisteSessionConnected() == true) {
							MyLog.i(TAG, "network is available: send MSG_REG_OK");
							ServiceRegiste.sendRegStatus(MessageTypes.MSG_REG_OK);
						} else {
							MyLog.i(TAG, "network is available: send MSG_REG_NOK");
							ServiceRegiste.sendRegStatus(MessageTypes.MSG_REG_NOK);
						}
						if (neededreset) {
							boolean result = sipService.register(SKDroid.getContext());
							MyLog.i(TAG, "network is available, register: " + result);
						}
						
					} else {
						MyLog.i(TAG, "network is not available");

						if (Engine.getInstance().getNetworkService() != null)
							Engine.getInstance().getNetworkService().setNetworkEnable(false);
						ServiceRegiste.sendRegStatus(MessageTypes.MSG_REG_NETWORK_ERROR);

//						checkedCount++;
//						NgnSipStack sipStack = sipService.getSipStack();
//						MyLog.d(TAG, "checkedCount=" + checkedCount + "  isIpChange=" + isIpChange);
//						if (checkedCount >= 3 || isIpChange) {
//							
////							if((checkedCount%3) == 0){
////								setMobileData(SKDroid.getContext(), true);
////							}
//							
////							neededreset = true;
//							isIpChange = false;
//							if (sipStack != null
//									&& sipStack.getState() != STACK_STATE.STOPPED
//									&& SystemVarTools.bLogin) {
//									MyLog.d(TAG, "network  008");
//									SystemVarTools.isNetChecking = true;
//									ServiceRegiste.sendStackStatus(MessageTypes.MSG_STACK_NEED_STOP);		
//							}
//						}
						
					}
				}
			};
			
			netCheckTimer.schedule(netCheckTask, 0, 5000);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void sendRegStatus(int RegStatus) {
		Intent reg = new Intent(MessageTypes.MSG_REG_EVENT);
		reg.putExtra(MessageTypes.MSG_REG_EVENT, RegStatus);
		if (GlobalVar.orderedbroadcastSign) {
			SKDroid.getContext().sendOrderedBroadcast(reg, null);
		} else {
			SKDroid.getContext().sendBroadcast(reg);
		}
	}

	public static void sendContactStatus(int ContactStatus) {
		Intent reg = new Intent(MessageTypes.MSG_CONTACT_EVENT);
		reg.putExtra(MessageTypes.MSG_CONTACT_EVENT, ContactStatus);
		if (GlobalVar.orderedbroadcastSign) {
			SKDroid.getContext().sendOrderedBroadcast(reg, null);
		} else {
			SKDroid.getContext().sendBroadcast(reg);
		}
	}
	public static void sendStackStatus(int ContactStatus) {
		Intent reg = new Intent(MessageTypes.MSG_STACK_EVENT);
		reg.putExtra(MessageTypes.MSG_STACK_EVENT, ContactStatus);
		if (GlobalVar.orderedbroadcastSign) {
			SKDroid.getContext().sendOrderedBroadcast(reg, null);
		} else {
			SKDroid.getContext().sendBroadcast(reg);
		}
	}

	public void sendControlCmd(int cmd) {
		if (mNetHandler != null) {
			mNetHandler.sendEmptyMessage(cmd);
		} else {
			MyLog.d(null, "The mNetHandler is not init.");
		}
	}

	public void setmNetHandler(Handler mNetHandler) {
		this.mNetHandler = mNetHandler;
	}

	/**
	 * 打印内存信息
	 */
	public void printMemoryInfos() {
		try {

			MemoryInfo[] mi = new MemoryInfo[1];
			ActivityManager am = (ActivityManager) SKDroid.getContext()
					.getSystemService(Context.ACTIVITY_SERVICE);
			int[] pids = new int[1];
			pids[0] = GlobalVar.mMyPid;
			mi = am.getProcessMemoryInfo(pids);
			int dalvikPss = mi[0].dalvikPss;
			int nativePss = mi[0].nativePss;
			int otherPss = mi[0].otherPss;
			int dalvikPrivateDirty = mi[0].dalvikPrivateDirty;
			int nativePrivateDirty = mi[0].nativePrivateDirty;
			int otherPrivateDirty = mi[0].otherPrivateDirty;

			int dalvikSharedDirty = mi[0].dalvikSharedDirty;
			int nativeSharedDirty = mi[0].nativeSharedDirty;
			int otherSharedDirty = mi[0].otherSharedDirty;

			int TotalPrivateDirty = mi[0].getTotalPrivateDirty();
			int TotalPss = mi[0].getTotalPss();
			int TotalSharedDirty = mi[0].getTotalSharedDirty();
			MyLog.d("内存信息", "====================================");
			MyLog.d("内存信息", "PSS总内存=" + TotalPss + "KB");
			MyLog.d("内存信息", "私有总内存=" + TotalPrivateDirty + "KB");
			MyLog.d("内存信息", "共享总内存=" + TotalSharedDirty + "KB");
			MyLog.d("内存信息", "PSS:虚拟机(" + dalvikPss + "KB)  本地(" + nativePss
					+ ")" + "其他(" + otherPss + ")");
			MyLog.d("内存信息", "私有:虚拟机(" + dalvikPrivateDirty + "KB)  " + "本地("
					+ nativePrivateDirty + ") 其他(" + otherPrivateDirty + ")");
			MyLog.d("内存信息", "共享:虚拟机(" + dalvikSharedDirty + "KB)  " + "本地("
					+ nativeSharedDirty + ") 其他(" + otherSharedDirty + ")");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 打开或关闭网络连接（非wifi）
	 * @param context
	 * @param pBoo
	 */
	public static void setMobileData(Context context,boolean pBoo){
		MyLog.d(TAG, "setMobileData()");
		try {
			ConnectivityManager cm = 
					(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			Class ownerClass = cm.getClass();
			Class[] args = new Class[1];
			args[0] = boolean.class;
			Method method = ownerClass.getMethod("setMobileDataEnabled", args);
			method.invoke(cm, pBoo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
