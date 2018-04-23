package com.sunkaisens.skdroid.Utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.doubango.ngn.NgnApplication;
import org.doubango.utils.MyLog;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.SKDroid;

import android.R.integer;
import android.content.Context;

/**
 * ��������
 * 
 * @author zh
 * 
 */
public class Tools_data {

	private static String TAG = Tools_data.class.getCanonicalName();
	public static final String FILENAME = "SKDroid";
	public static final String MsgIDTimeFile = "SKDroidMsgIDTime";
	
	//�Ѷ���ն� ҵ������汾�Ŵ��λ��
	private static String VERSION_FILE = "/tmp/kx_service_version";
	
	//����pad���ֳ�̨���ܿ��ļ����״̬Ŀ¼
	private static String SECURITY_CARD = "/proc/card_exist";
	
	// /**
	// * ����汾��Ϣ��vector guid versionCode versionName
	// */
	// public HashMap<String, Object> datamap = new HashMap<String, Object>();

	/**
	 * д�ļ�
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	public static void writeData(HashMap<String, Object> datamap)
			throws IOException {

		NgnApplication.getContext().deleteFile(FILENAME);

		FileOutputStream fos = NgnApplication.getContext().openFileOutput(FILENAME,
				Context.MODE_PRIVATE);
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); // ����һ���ֽ������
		ObjectOutputStream oos = new ObjectOutputStream(baos); // ����һ���������

		// if (fileName.equals(FILENAME)) {
		oos.writeObject(datamap);
		// }

		byte[] buf = baos.toByteArray(); // ������ز��ֽ����аѴ���������һ���µ�����
		oos.flush();


		fos.write(buf);
		fos.close();
		oos.close();
		baos.close();
	}

	/**
	 * ���ļ�
	 * 
	 * @param fileName
	 */
	public static HashMap<String, Object> readData() {
		HashMap<String, Object> datamap = new HashMap<String, Object>();
		FileInputStream fos = null;
		try {
			fos = NgnApplication.getContext().openFileInput(FILENAME);
		} catch (FileNotFoundException e1) {
			try {
				if(fos != null){
					fos.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return datamap;
		}

		ObjectInputStream ois = null;
		try {
			if (fos.available() < 0) {

				fos.close();

				return datamap;
			}
			ois = new ObjectInputStream(fos);

			datamap = (HashMap<String, Object>) ois.readObject();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(fos != null){
					fos.close();
				}
				if(ois != null){
					ois.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return datamap;
	}

	public static void clearAllData() {
		NgnApplication.getContext().deleteFile(FILENAME);
		NgnApplication.getContext().deleteFile(MsgIDTimeFile);
	}
	
	public static HashMap<String, Object> readIDHashMap() {
		HashMap<String, Object> datamap = null;
		FileInputStream fos = null;
		ObjectInputStream ois = null;
		try {
			fos = NgnApplication.getContext().openFileInput(MsgIDTimeFile);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			return null;
		}finally{
			if(fos != null){
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		try {
			if(fos!=null) {
				ois = new ObjectInputStream(fos);
	
				datamap = (HashMap<String, Object>) ois.readObject();

			}


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(ois != null){
				try {
					ois.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return datamap;
	}
	public static void writeIDHashMap(HashMap<String, Object> datamap)
			throws IOException {

		NgnApplication.getContext().deleteFile(MsgIDTimeFile);

		FileOutputStream fos = NgnApplication.getContext().openFileOutput(MsgIDTimeFile,
				Context.MODE_PRIVATE);
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); // ����һ���ֽ������
		ObjectOutputStream oos = new ObjectOutputStream(baos); // ����һ���������

		// if (fileName.equals(FILENAME)) {
		oos.writeObject(datamap);
		// }

		byte[] buf = baos.toByteArray(); // ������ز��ֽ����аѴ���������һ���µ�����
		oos.flush();


		fos.write(buf);
		fos.close();
		oos.close();
		baos.close();
	}
	
	/**
	 * ������汾��д���ض��ļ�
	 */
	public static void writeVersion() {
		
		FileOutputStream fos = null;
		
		try {
			File file = new File(VERSION_FILE);
			if(!file.exists()){
				file.mkdir();
			}
			String version = SKDroid.getVersionName();
			MyLog.d("VERSION", "VERSION = "+version);
			fos = new FileOutputStream(file+"/version.txt");
			fos.write(version.getBytes());
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(fos != null){
				try {
					fos.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}
public static void writeVersion(String version) {
		
		FileOutputStream fos = null;
		
		try {
			String sdcardDir = Engine.getInstance().getStorageService().getSdcardDir();
			MyLog.d(TAG, "sdcardDir:"+sdcardDir);
			File file = new File(sdcardDir);
			if(!file.exists()){
				file.mkdir();
			}
			MyLog.d("VERSION", "VERSION = "+version);
			fos = new FileOutputStream(file+"/inner_version.txt");
			fos.write(version.getBytes());
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(fos != null){
				try {
					fos.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}
	
	
	public static boolean getSecurityCard()
	{
		boolean securityCardExit = false;
		BufferedReader bufferedReader = null;
		File mFile = new File(SECURITY_CARD);
		MyLog.d(TAG, "SECURITY_CARD: exists = " + mFile.exists());
		if(!mFile.exists()){
			MyLog.d(TAG, "SECURITY_CARD: " + mFile.exists());
			return false;
		}
		
		try {
			bufferedReader = new BufferedReader(new FileReader(SECURITY_CARD));
			if(null != bufferedReader) {
				String status = bufferedReader.readLine();
				if(status.trim().equals("1")) {
					securityCardExit = true;
				}
				MyLog.d(TAG, "SECURITY_CARD: " + status + ", securityCardExit: " + securityCardExit);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return securityCardExit;
	}
	
}
