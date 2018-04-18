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
 * 配置文件操作
 * 
 * @author zh
 * @version 1.0
 * @data 20140428
 */
public class MyProp {
//	private static String MYPROP_PATH_SDCARD_DIR = Environment.getExternalStorageDirectory().getPath(); //配置文件在sdcard中的路径 /mnt/sdcard /storage/sdcard0
	private static String MYPROP_PATH_SDCARD_DIR = "/data/data/skdroid"; //配置文件在sdcard中的路径 /data/data/skdroid
	private static String MYPROP_FILE_NAME = "SocketConfig.properties"; //配置文件名称
	
	//读取配置文件
//	public static Properties loadConfig(Context context, String file) {
	public static Properties loadConfig() {
		Properties properties = new Properties();
		FileInputStream s = null;
		try {
//			FileInputStream s = new FileInputStream(file);
			s = new FileInputStream(MYPROP_PATH_SDCARD_DIR + "/" + MYPROP_FILE_NAME);
			properties.load(s);
			Log.d("SocketService", "配置文件加载完成");
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

	//保存配置文件
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
