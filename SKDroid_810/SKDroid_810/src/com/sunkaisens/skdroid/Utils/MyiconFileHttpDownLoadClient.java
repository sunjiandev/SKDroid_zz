package com.sunkaisens.skdroid.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.model.NgnHistorySMSEvent;
import org.doubango.utils.MyLog;

import android.R.integer;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Screens.ScreenChat;
import com.sunkaisens.skdroid.Services.ServiceContact;
//import com.sunkaisens.skdroid.Screens.ScreenChat.ScreenChatAdapter;
import com.sunkaisens.skdroid.adapter.ScreenChatAdapter;
import com.sunkaisens.skdroid.model.ModelFileTransport;

public class MyiconFileHttpDownLoadClient {

	private static String TAG = MyiconFileHttpDownLoadClient.class
			.getCanonicalName();

	public String fRemoteURL = "";
	public String fSavePath = "";
	public boolean result = false;
	public long sumLength = 0;// sum length of the file
	public long downloadLength = 0;// download length of the file

	

	private Handler mRefreshHandler = null;

	public boolean isDowbloading = false;



	private HttpURLConnection conn;



	public void httpDownloadFileInThread(String url,String savepath) {
		MyLog.i(TAG, "httpDownloadFileInThread()");

		isDowbloading = true;
		fRemoteURL = url;
		
		int index = fRemoteURL.indexOf("8944");
		if(index == -1){
			Log.e(TAG, "icon'url is wrong.");
			return;
		}
		
		fRemoteURL = "http://101.200.188.211:"+fRemoteURL.substring(index);
//		
//	
//		
//		
		fSavePath = savepath;
		

		Thread t = new Thread(new Runnable() {
			public void run() {

				result = MyiconFileHttpDownLoadClient.this.httpDownloadFile(
						fRemoteURL, fSavePath);
				if (result) {
					sendRefreshMsg(1);
					MyLog.d(TAG, "&&&& 文件下载成功 ");
					
					ServiceContact.sendContactFrashMsg();

				} else {
					sendRefreshMsg(0);
					MyLog.d(TAG, "&&&& 文件下载失败 ");
				}

				isDowbloading = false;
			}

		});
		t.start();

	}

	/**
	 * 下载文件
	 * 
	 * @param remoteURL
	 *            文件url
	 * @param savepath
	 *            文件本地存储路径
	 * @return
	 */
	public boolean httpDownloadFile(String remoteURL, String savepath) {
		int len = 0;

		String tag = remoteURL + savepath;
		InputStream inStream = null;
		FileOutputStream outStream = null;
		try {
			result = false;
			URL url;
			if (!remoteURL.startsWith("http://")) {
				remoteURL = "http://" + remoteURL;
			}
			MyLog.d(TAG, "&&&&& 文件下载URL:" + remoteURL);
			url = new URL(remoteURL);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(120 * 1000);// 30秒超时
			conn.setReadTimeout(120 * 1000);
			conn.setRequestProperty("Connection", "Close");
			sumLength = conn.getHeaderFieldInt("Length", 0);
			MyLog.i(TAG, "down sumlength:" + sumLength);
			inStream = conn.getInputStream();
			outStream = new FileOutputStream(savepath);
			byte[] buffer = new byte[1024];
			int persent = 0;
			int lastPercent = persent;

			downloadLength = 0;

			while ((len = inStream.read(buffer)) != -1) {
				// MyLog.i(TAG, "Read1 length="+len);
				
				outStream.write(buffer, 0, len);
				downloadLength += len;
				float persent1 = (float) ((float) downloadLength / (float) sumLength);
				persent = (int) (persent1 * 100);
				if (persent > lastPercent) {
					lastPercent = persent;
					refreshProgress(persent);
				}

			}

			conn.disconnect();
			isDowbloading = false;
			result = true;

			return result;
		} catch (MalformedURLException e) {
			e.printStackTrace();

			return false;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();

			return false;
		} catch (FileNotFoundException e) {

			return false;
		} catch (IOException e) {

			e.printStackTrace();

			return false;
		}finally{
			try {
				if(inStream != null){
					inStream.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			try {
				if(outStream != null){
					outStream.close();
				}
			} catch (Exception e2) {
			}
		}

	}

	private void refreshProgress(int progress) {
		try {
			if (mRefreshHandler != null) {
				Message msg = Message.obtain(mRefreshHandler,
						Main.FILEUPLOAD_INPROGRESS);
				msg.getData().putInt("fileTransferProgress", progress);

				mRefreshHandler.sendMessage(msg);

			} else {
				MyLog.d(TAG, "&&&  下载进度Handler为空");
			}
		} catch (Exception e) {
			MyLog.d(TAG, "fileDownLoadException : " + mRefreshHandler);
		}
	}
	
	
	
	/**
	 * 1表示成功
	 * 0表示失败
	 * 
	 * */
	private void sendRefreshMsg(int result) {
		// Engine.getInstance().getHistoryService().reflush();
		MyLog.e(TAG, "sendRefreshMsg()");
		if (mRefreshHandler != null) {
			
			if(result==1){
			
			Message msg = Message.obtain(mRefreshHandler,
					Main.FILEDOWNLOAD_SUCCESS);
			mRefreshHandler.sendMessage(msg);
			}else {
				Message msg = Message.obtain(mRefreshHandler,
						Main.FILEDOWNLOAD_FAILED);
				mRefreshHandler.sendMessage(msg);
			}
			
			} else {
			MyLog.d(TAG, "&&&&& chat刷新Handler为空");
		}
	}

	public void setmRefreshHandler(Handler mRefreshHandler) {
		this.mRefreshHandler = mRefreshHandler;
	}

}
