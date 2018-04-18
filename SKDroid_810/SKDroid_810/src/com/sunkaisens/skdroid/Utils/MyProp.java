package com.sunkaisens.skdroid.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.doubango.ngn.NgnApplication;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * �����ļ�����
 * 
 * @author zh
 * @version 1.0
 * @data 20140428
 */
public class MyProp {
//	private static String MYPROP_PATH_SDCARD_DIR = Environment.getExternalStorageDirectory().getPath(); //�����ļ���sdcard�е�·�� /mnt/sdcard /storage/sdcard0
	private static String MYPROP_PATH_SDCARD_DIR = "/data/data/skdroid"; //�����ļ���sdcard�е�·�� /data/data/skdroid
	private static String MYPROP_FILE_NAME = "SocketConfig.properties"; //�����ļ�����
	
	//��ȡ�����ļ�
//	public static Properties loadConfig(Context context, String file) {
	public static Properties loadConfig() {
		Properties properties = new Properties();
		FileInputStream s = null;
		try {
//			FileInputStream s = new FileInputStream(file);
			s = new FileInputStream(MYPROP_PATH_SDCARD_DIR + "/" + MYPROP_FILE_NAME);
			properties.load(s);
			Log.d("SocketService", "�����ļ��������");
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
			if(s != null){
				try {
					s.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			return null;
		}
		return properties;
	}

	//���������ļ�
//	public static boolean saveConfig(Context context, String file, Properties properties) {
	public static boolean saveConfig(Properties properties) {
		FileOutputStream s = null;
		try {
//			File fil = new File(file);
//			File fil = new File(properties.getClass().getResourceAsStream(MYPROP_PATH_SDCARD_DIR + "/" + MYPROP_FILE_NAME));
			File fil = new File(MYPROP_PATH_SDCARD_DIR + "/" + MYPROP_FILE_NAME); ///data/data/skdroid/SocketConfig.properties
			if (!fil.exists())
				fil.createNewFile();
			s = new FileOutputStream(fil);
			properties.store(s, ";");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}finally{
			try {
				if(s != null)
					s.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return true;
	}

}
