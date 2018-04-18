package com.sunkaisens.skdroid.app.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.doubango.ngn.NgnApplication;
import org.doubango.utils.MyLog;

import com.sunkaisens.skdroid.util.GlobalSession;
import com.sunkaisens.skdroid.util.GlobalVar;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.storage.StorageManager;
import android.util.Log;

public class LogService extends Service {

	private String TAG = LogService.class.getCanonicalName();

	private Process logProcess = null;

	private InputStreamReader isr = null;
	private FileWriter filerWriter = null;

	// 用于判断进程是否运行
	@Override
	public void onCreate() {
		Log.d(TAG, "LogService onCreate()...");
		// initFilePath();
		// writeSystemLogToFile();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return startS;
	}

	/**
	 * 启动Service2
	 */
	private IBinder startS = new LogServiceProcess.Stub() {

		@Override
		public void startLogging() throws RemoteException {
			Log.d(TAG, "startLogging()");
			if (logProcess != null) {
				try {
					if (isr != null) {
						isr.close();
					}
				} catch (Exception e2) {
				}
				try {
					if (filerWriter != null) {
						filerWriter.close();
					}
				} catch (Exception e2) {
				}
				logProcess.destroy();
				Log.d(TAG, "logProcess.destroy()");
			}
			initFilePath();
			writeSystemLogToFile();
		};

		@Override
		public void stopProcess() throws RemoteException {
			Log.d(TAG, "stopService");
			System.exit(0);
			MyLog.d(TAG, "exit(0)");
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand()");
		return startId;

	};

	public void onDestroy() {
		Log.d(TAG, "LogService onDestroy!");

	}

	private String MYLOG_PATH_SDCARD_DIR = null;

	public void writeSystemLogToFile() {
		if (GlobalSession.bSocketService) {
			MyLog.d("", "大终端版本，不保存系统日志");
			return;
		} else {
			MyLog.d("", "保存系统日志");
		}

		MYLOG_PATH_SDCARD_DIR = sdcardMyDir + "/syslog/";

		new Thread(new Runnable() {

			@Override
			public void run() {
				String comm = "logcat -v threadtime";

				Log.d(TAG, "comm=" + comm);
				SimpleDateFormat appStartTime = new SimpleDateFormat(
						"yyyy-MM-dd-HH-mm-ss");
				try {
					logProcess = Runtime.getRuntime().exec(comm);
					isr = new InputStreamReader(logProcess.getInputStream());
					BufferedReader br = new BufferedReader(isr);
					String res = "";
					File dir = new File(MYLOG_PATH_SDCARD_DIR);
					if (!dir.exists()) {
						dir.mkdirs();
					}
					int fileNum = 0;
					int loglines = 0;
					if (GlobalVar.mAppStartTime == null) {
						GlobalVar.mAppStartTime = new Date();
					}
					File file = new File(MYLOG_PATH_SDCARD_DIR,
							appStartTime.format(GlobalVar.mAppStartTime)
									+ "_systemLogs_0.log");
					// MyLog.d("", "系统日志路径 : "+file.getAbsolutePath());
					filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
					BufferedWriter bufWriter = new BufferedWriter(filerWriter);
					while ((res = br.readLine()) != null) {
						if (loglines > 15000) {
							fileNum++;
							loglines = 0;
							file = new File(MYLOG_PATH_SDCARD_DIR,
									appStartTime
											.format(GlobalVar.mAppStartTime)
											+ "_systemLogs_" + fileNum + ".log");
							filerWriter = new FileWriter(file, true);
							bufWriter = new BufferedWriter(filerWriter);
						}

						bufWriter.write(res);
						bufWriter.newLine();
						loglines++;
					}

					bufWriter.close();
				} catch (FileNotFoundException f) {
					MyLog.d("", "系统日志文件路径不存在，停止写入系统日志");
					f.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						if (isr != null) {
							isr.close();
						}
					} catch (Exception e2) {
					}
					try {
						if (filerWriter != null) {
							filerWriter.close();
						}
					} catch (Exception e2) {
					}
				}
			}
		}, "SystemLog").start();

	}

	private String sdcardMyDir = null;

	public void initFilePath() {
		Log.d(TAG, "initFilePath()");
		StorageManager sm = (StorageManager) NgnApplication.getContext()
				.getSystemService(Context.STORAGE_SERVICE);
		String[] paths;
		try {
			paths = (String[]) sm.getClass().getMethod("getVolumePaths", null)
					.invoke(sm, null);
			if (paths != null && paths.length > 1) {

				for (String s : paths) {
					File f2 = new File(s + "/SKDroid/");
					boolean b = false;
					if (!f2.exists()) {
						b = f2.mkdir();
					} else {
						b = true;
					}

					Log.i(TAG, "sdcard file=" + s + "  existed?" + b);
					if (b) {
						sdcardMyDir = f2.getPath();
						Log.i(TAG, "sdcard sdcardDir=" + sdcardMyDir);
					}
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
