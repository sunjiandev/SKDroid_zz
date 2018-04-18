package org.doubango.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.utils.NgnConfigurationEntry;

import com.sunkaisens.skdroid.util.GlobalSession;
import com.sunkaisens.skdroid.util.GlobalVar;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

/**
 * ����־�ļ�����ģ��ֿɿؿ��ص���־����
 * 
 * @author BaoHang
 * @version 1.0
 * @data 2012-2-20
 */
public class MyLog {
	private final static String TAG = MyLog.class.getCanonicalName();
	
	
	private static Boolean MYLOG_SWITCH = true; // ��־�ļ��ܿ���
	private static Boolean MYLOG_WRITE_TO_FILE = false;// ��־д���ļ�����
	private static Boolean MYLOG_WRITE_TO_FILE_SYS = true;// ϵͳ��־д���ļ�����

	private static char MYLOG_TYPE = 'v';// ������־���ͣ�w����ֻ����澯��Ϣ�ȣ�v�������������Ϣ
	public static String MYLOG_PATH_SDCARD_DIR = "/mnt/asec/skdroid/log"; // ��־�ļ���sdcard�е�·��
																			// /data/data/skdroid/log
																			// ���ն˻���
	private static int SDCARD_LOG_FILE_SAVE_DAYS = 0;// sd������־�ļ�����ౣ������
	private static String MYLOG_FILE_NAME = "SKSLog.txt";// �����������־�ļ�����
	private static SimpleDateFormat myLogSdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");// ��־�������ʽ
	private static SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd");// ��־�ļ���ʽ

	public MyLog() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * ��ʼ����־�洢·��
	 * 
	 * @param dir
	 */
	public static void init(String dir) {
		if (!GlobalSession.bSocketService) {
			MYLOG_PATH_SDCARD_DIR = dir + "/syslogs/"
					+ logfile.format(new Date()) + "/";
			MYLOG_WRITE_TO_FILE_SYS = NgnEngine
					.getInstance()
					.getConfigurationService()
					.getBoolean(
							NgnConfigurationEntry.LOGS_WRITE_TO_FILE_SYS_OPEN,
							true);
		} else {

		}
		MyLog.d(TAG, "��־�ļ�·����" + MYLOG_PATH_SDCARD_DIR);
		MyLog.d(TAG, "Open the syslog?" + MYLOG_WRITE_TO_FILE_SYS);

		//MYLOG_WRITE_TO_FILE_SYS = false;

		if (MYLOG_WRITE_TO_FILE_SYS) {
			writeSystemLogToFile();
		}

	}

	public static void w(String tag, Object msg) { // ������Ϣ
		log(tag, msg.toString(), 'w');
	}

	public static void e(String tag, Object msg) { // ������Ϣ
		log(tag, msg.toString(), 'e');
	}

	public static void d(String tag, Object msg) {// ������Ϣ
		log(tag, msg.toString(), 'd');
	}

	public static void i(String tag, Object msg) {//
		log(tag, msg.toString(), 'i');
	}

	public static void v(String tag, Object msg) {
		log(tag, msg.toString(), 'v');
	}

	public static void w(String tag, String text) {
		log(tag, text, 'w');
	}

	public static void e(String tag, String text) {
		log(tag, text, 'e');
	}

	public static void d(String tag, String text) {
		log(tag, text, 'd');
	}

	public static void i(String tag, String text) {
		log(tag, text, 'i');
	}

	public static void v(String tag, String text) {
		log(tag, text, 'v');
	}

	/**
	 * ����tag, msg�͵ȼ��������־
	 * 
	 * @param tag
	 * @param msg
	 * @param level
	 * @return void
	 * @since v 1.0
	 */
	private static void log(String tag, String msg, char level) {
		if (MYLOG_SWITCH) {
			if ('e' == level && ('e' == MYLOG_TYPE || 'v' == MYLOG_TYPE)) { // ���������Ϣ
				Log.e(tag, msg);
			} else if ('w' == level && ('w' == MYLOG_TYPE || 'v' == MYLOG_TYPE)) {
				Log.w(tag, msg);
			} else if ('d' == level && ('d' == MYLOG_TYPE || 'v' == MYLOG_TYPE)) {
				Log.d(tag, msg);
			} else if ('i' == level && ('d' == MYLOG_TYPE || 'v' == MYLOG_TYPE)) {
				Log.i(tag, msg);
			} else {
				Log.v(tag, msg);
			}
			// if (MYLOG_WRITE_TO_FILE)
			// writeLogtoFile(String.valueOf(level), tag, msg);
		}
	}

	/**
	 * ����־�ļ���д����־
	 * 
	 * @return
	 */
	private static void writeLogtoFile(String mylogtype, String tag, String text) { // �½������־�ļ�

		if (MYLOG_PATH_SDCARD_DIR == null
				|| MYLOG_PATH_SDCARD_DIR.startsWith("null")) {
			return;
		}

		Date nowtime = new Date();
		String needWriteFile = logfile.format(nowtime);
		String needWriteMessage = myLogSdf.format(nowtime) + "    " + mylogtype
				+ "    " + tag + "    " + text;
		// File file = new File(MYLOG_PATH_SDCARD_DIR, needWriteFile + "_" +
		// MYLOG_FILE_NAME);
		FileWriter filerWriter = null;
		try {
			File dir = new File(MYLOG_PATH_SDCARD_DIR);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File file = new File(MYLOG_PATH_SDCARD_DIR, needWriteFile + "_"
					+ MYLOG_FILE_NAME);
			filerWriter = new FileWriter(file, true);// ����������������ǲ���Ҫ�����ļ���ԭ�������ݣ������и���
			BufferedWriter bufWriter = new BufferedWriter(filerWriter);
			bufWriter.write(needWriteMessage);
			bufWriter.newLine();
			bufWriter.close();

		} catch (FileNotFoundException f) {
			MyLog.d(TAG, "��־�ļ������ڣ���־ֹͣд���ļ�");
			MYLOG_WRITE_TO_FILE = false;
			f.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (filerWriter != null) {
				try {
					filerWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void writeSystemLogToFile() {
		if (GlobalSession.bSocketService) {
			MYLOG_WRITE_TO_FILE_SYS = false;
			MyLog.d(TAG, "���ն˰汾��������ϵͳ��־");
		} else {
			MyLog.d(TAG, "����ϵͳ��־");
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String comm = "logcat";

				Process process;
				InputStreamReader isr = null;
				FileWriter filerWriter = null;
				try {
					process = Runtime.getRuntime().exec(comm);
					isr = new InputStreamReader(process.getInputStream());
					BufferedReader br = new BufferedReader(isr);
					String res = "";
					File dir = new File(MYLOG_PATH_SDCARD_DIR);
					if (!dir.exists()) {
						dir.mkdirs();
					}
					int fileNum = 0;
					int loglines = 0;
					SimpleDateFormat appStartTime = new SimpleDateFormat(
							"HHmmss");
					if (GlobalVar.mAppStartTime == null) {
						GlobalVar.mAppStartTime = new Date();
					}
					File file = new File(MYLOG_PATH_SDCARD_DIR,
							appStartTime.format(GlobalVar.mAppStartTime)
									+ "_systemLogs_0.log");
					// MyLog.d("", "ϵͳ��־·�� : "+file.getAbsolutePath());
					filerWriter = new FileWriter(file, true);// ����������������ǲ���Ҫ�����ļ���ԭ�������ݣ������и���
					BufferedWriter bufWriter = new BufferedWriter(filerWriter);
					while ((res = br.readLine()) != null) {
						if (!MYLOG_WRITE_TO_FILE_SYS) {
							return;
						}

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

						Date nowtime = new Date();
						String needWriteMessage = myLogSdf.format(nowtime)
								+ "-" + res;
						bufWriter.write(needWriteMessage);
						bufWriter.newLine();
						loglines++;
					}

					bufWriter.close();
				} catch (FileNotFoundException f) {
					MyLog.d(TAG, "ϵͳ��־�ļ�·�������ڣ�ֹͣд��ϵͳ��־");
					MYLOG_WRITE_TO_FILE_SYS = false;
					f.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				} finally {
					try {
						if (isr != null) {
							isr.close();
						}
					} catch (Exception e2) {
						// TODO: handle exception
					}
					try {
						if (filerWriter != null) {
							filerWriter.close();
						}
					} catch (Exception e2) {
						// TODO: handle exception
					}
				}
			}
		}, "SystemLog").start();

	}

	/**
	 * ɾ���ƶ�����־�ļ�
	 */
	public static void delFile() { // ɾ����־�ļ�
		String needDelFile = logfile.format(getDateBefore());
		File file = new File(MYLOG_PATH_SDCARD_DIR, needDelFile
				+ MYLOG_FILE_NAME);
		if (file.exists()) {
			file.delete();
		}
	}

	/**
	 * �õ�����ʱ��ǰ�ļ������ڣ������õ���Ҫɾ������־�ļ���
	 */
	private static Date getDateBefore() {
		Date nowtime = new Date();
		Calendar now = Calendar.getInstance();
		now.setTime(nowtime);
		now.set(Calendar.DATE, now.get(Calendar.DATE)
				- SDCARD_LOG_FILE_SAVE_DAYS);
		return now.getTime();
	}

	/**
	 * �Ƿ���־���浽�ļ��еĿ���
	 * 
	 * @param mYLOG_WRITE_TO_FILE
	 */
	public static void setMYLOG_WRITE_TO_FILE(Boolean mYLOG_WRITE_TO_FILE) {
		MYLOG_WRITE_TO_FILE = mYLOG_WRITE_TO_FILE;
	}

	/**
	 * ��־��ӡ�ܿ���
	 * 
	 * @param mYLOG_SWITCH
	 */
	public static void setMYLOG_SWITCH(Boolean mYLOG_SWITCH) {
		MYLOG_SWITCH = mYLOG_SWITCH;
	}

	public static Boolean getMYLOG_WRITE_TO_FILE_SYS() {
		return MYLOG_WRITE_TO_FILE_SYS;
	}

	public static void setMYLOG_WRITE_TO_FILE_SYS(Boolean mYLOG_WRITE_TO_FILE_SYS) {
		MYLOG_WRITE_TO_FILE_SYS = mYLOG_WRITE_TO_FILE_SYS;
	}

}
