package com.sunkaisens.skdroid.update;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.impl.NgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.utils.MyLog;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.R;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class ChkVer {
	private static String UPDATE_SERVER = "http://124.205.124.82:8090/update_query"; //下载服务器地址
//	public static final String UPDATE_VERJSON = "version.json";
//	public static final String UPDATE_APKNAME = "estar360.apk";
	public static String UPDATE_SAVENAME = "";
	private static String UPDATE_URL = "http://124.205.124.82:8090/file/SKDriod.apk"; //下载链接地址

	
	public static String getUpdateUrl(){
		NgnEngine engine = (NgnEngine)Engine.getInstance();
		INgnConfigurationService conf = engine.getConfigurationService();
		String host = conf.getString(NgnConfigurationEntry.NETWORK_GROUP_REALM,
				NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM);
		UPDATE_SERVER = "http://"+host+":8090/update_query";
		MyLog.d("", "UPDATE_SERVER:"+UPDATE_SERVER);
		return UPDATE_SERVER;
	}
	
	public static String getDownloadUrl(){
		MyLog.e("", "UPDATE_URL:"+UPDATE_URL);
		return UPDATE_URL;
	}
	
	public static void setUPDATE_URL(String uPDATE_URL) {
		UPDATE_URL = uPDATE_URL;
	}
	
	public static int getVerCode(Context context) {
    	int verCode = -1;
    	try {
    		verCode = context.getPackageManager().getPackageInfo("com.sunkaisens.skdroid", 0).versionCode;
    	}
    	catch (NameNotFoundException e) {
    		Log.e("VerCode", e.toString());
    	}
    	return verCode;
    }
    
    public static String getVerName(Context context) {
    	String verName = "";
    	try {
    		verName = context.getPackageManager().getPackageInfo("com.sunkaisens.skdroid", 0).versionName;
    	}
    	catch (NameNotFoundException e) {
    		Log.e("VerName", e.toString());
    	}
    	return verName;
    }
    
    public static String getAppName(Context context) {
    	String appName = context.getResources().getText(R.string.app_name).toString();
    	return appName;
    }
}
