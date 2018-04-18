package com.sunkaisens.skdroid.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.util.ByteArrayBuffer;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.utils.MyLog;

import android.content.Context;
import android.util.Log;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.component.NodeResource;
import com.sunkaisens.skdroid.model.ModelCall;
import com.sunkaisens.skdroid.model.ModelContact;

public class FileTools {

	//write
	public static boolean writeFileData(Context inContext,String fileName,byte[] data)
	{
		Log.d("FileTools", "writeFileData()");
		if(data == null || inContext == null || fileName==null || fileName.isEmpty())
			return false;
		FileOutputStream fout = null;
		try {
//			FileOutputStream fout = inContext.openFileOutput(fileName,Context.MODE_PRIVATE);
			fout = new FileOutputStream(fileName);
			fout.write(data);
			fout.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			if(fout != null){
				try {
					fout.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			return false;
		}
	}
	//read
	public static byte[] readFileData(Context inContext,String fileName)
	{
		if(inContext == null || fileName==null || fileName.isEmpty())
			return null;
		File file = new File(fileName);
		int length = 0;
		if(file.exists()){
			length = (int) file.length();
		}
		int size = -1;
		FileInputStream fin = null;
		try {
			fin = inContext.openFileInput(fileName);
			byte[] data = new byte[length];
			size = fin.read(data);
			MyLog.d("", "read size="+size);
			fin.close();
			return data;
		} catch (Exception e) {
			if(fin != null){
				try {
					fin.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
			return null;
		}
	}
}
