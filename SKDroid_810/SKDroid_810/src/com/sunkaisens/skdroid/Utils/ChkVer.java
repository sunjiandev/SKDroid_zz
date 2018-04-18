package com.sunkaisens.skdroid.Utils;

import com.sunkaisens.skdroid.R;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class ChkVer {
	
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
