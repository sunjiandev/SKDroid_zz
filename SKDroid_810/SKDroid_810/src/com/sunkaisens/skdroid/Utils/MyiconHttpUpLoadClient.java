package com.sunkaisens.skdroid.Utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.utils.MyLog;

import android.os.Handler;
import android.os.Message;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.Services.ServiceContact;
//import com.sunkaisens.skdroid.Screens.ScreenChat.ScreenChatAdapter;

public class MyiconHttpUpLoadClient {

	public static String TAG = MyiconHttpUpLoadClient.class.getCanonicalName();

	private String end = "\r\n";
	private String twoHyphens = "--";
	private String boundary = "---------------------------";

	// http://test.com
	public String network_realm = Engine
			.getInstance()
			.getConfigurationService()
			.getString(NgnConfigurationEntry.NETWORK_REALM,
					NgnConfigurationEntry.DEFAULT_NETWORK_REALM);

	public String network_IP = Engine
			.getInstance()
			.getConfigurationService()
			.getString(NgnConfigurationEntry.NETWORK_GROUP_REALM,
					NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM);

	public String account = Engine
			.getInstance()
			.getConfigurationService()
			.getString(NgnConfigurationEntry.IDENTITY_IMPI,
					NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI);

	public String fLocalPath = "";

	public String result = "";
	public long sumLength = 0;// sum length of the file
	public long uploadLength = 0;// upload length of the file

	public boolean isUploading = false;

	private HttpURLConnection connection;

	private Handler mFileUploadProgressHandler = null;

	public void httpSendFileInThread(String localpath) {

		MyLog.i(TAG, "httpSendFileInThread()");
		isUploading = true;

		fLocalPath = localpath;

		Thread t = new Thread(new Runnable() {
			public void run() {

				result = MyiconHttpUpLoadClient.this.httpSendFile(fLocalPath);
				uploadLength = 0;
				MyLog.d(TAG, "upload result : " + result);

				if (result != null && result.startsWith("true")) {

					ServiceContact.sendContactFrashMsg();
					try {
						if (connection.getResponseCode() == 201) {
						} else {
							refreshUploadResult(0);
						}
					} catch (IOException e) {
						e.printStackTrace();
						refreshUploadResult(0);
					}

					connection.disconnect();
					connection = null;

					refreshUploadResult(1);

				} else { // false
					refreshUploadResult(0);
				}

				isUploading = false;

			}
		});
		t.start();

	}

	public String httpSendFile(String localpath) {

		MyLog.e(TAG, "httpSendFile()");

		// POST
		// http://192.168.1.192:8010/files/[filename];from=[from];to=[to];isGroup=[]

		// http://xcap.example.com/services/pic/sip:19866668029@test.com/portrait

		String urlTmp = "http://" + network_IP + ":8944/services/pic/sip:"
				+ account + "@" + network_realm + "/portrait";

		FileInputStream fis = null;
		DataInputStream fileinput = null;
		OutputStream out = null;

		try {
			URL url = new URL(urlTmp);
			connection = (HttpURLConnection) url.openConnection();
			MyLog.d(TAG,
					"UploadConnection(" + connection + ")"
							+ connection.hashCode());
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setConnectTimeout(120 * 1000);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Connection", "Close");
			// connection.setRequestProperty("Content-Type",
			// "application/octet-stream");

			connection.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + boundary);

			File file = new File(localpath);
			MyLog.d(TAG, "File exist?" + file.exists());
			if (!file.exists()) {

				return "false";
			}
			// connection.setRequestProperty("Content-Length", "" +
			// file.length());
			sumLength = file.length();

			// connection.setFixedLengthStreamingMode(((int) sumLength));

			if (sumLength == 0) {

				return "false";
			}
			connection.connect();
			fis = new FileInputStream(localpath);
			fileinput = new DataInputStream(fis);
			byte[] buffer = new byte[1024];
			int readlength = 0;
			int persent = 0;
			int lastPercent = persent;

			out = connection.getOutputStream();
			MyLog.d(TAG, "UploadStream  open(" + out + ")");

			out.write((twoHyphens + boundary + end).getBytes());
			out.write(("Content-Disposition: form-data; name=\"image\";filename=\""
					+ file.getName() + "\"" + end).getBytes());
			out.write(("Content-Type:image/jpeg" + end).getBytes());
			out.write((end).getBytes());

			while ((readlength = fileinput.read(buffer)) > 0) {

				out.write(buffer, 0, readlength);
				out.flush();
				uploadLength += readlength;
				float persent1 = (float) ((float) uploadLength / (float) sumLength);
				persent = (int) (persent1 * 100);
				if (persent > lastPercent) {
					lastPercent = persent;
					refreshProgress(persent);
				}

			}

			out.write(end.getBytes());
			out.write((twoHyphens + boundary + twoHyphens + end).getBytes());
			out.write(end.getBytes());

			MyLog.d(TAG, "UploadStream close(" + out + ")");

			return "true";
		} catch (MalformedURLException e) {
			e.printStackTrace();

			return "false";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();

			return "false";
		} catch (IOException e) {
			e.printStackTrace();

			return "false";
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (fileinput != null) {
					fileinput.close();
				}
			} catch (Exception e2) {
			}
		}

	}

	private void refreshProgress(int progress) {
		try {
			if (mFileUploadProgressHandler != null) {
				Message msg = Message.obtain(mFileUploadProgressHandler,
						Main.FILEUPLOADPROGRESS);
				msg.getData().putInt("fileTransferProgress", progress);

				mFileUploadProgressHandler.sendMessage(msg);

			} else {
			}
		} catch (Exception e) {
			MyLog.d(TAG, "fileUploadException : " + mFileUploadProgressHandler);
		}
	}

	private void refreshUploadResult(int result) {
		try {
			if (mFileUploadProgressHandler != null) {

				if (result == 1) {

					Message msg = Message.obtain(mFileUploadProgressHandler,
							Main.FILEUPLOAD_SUCCESS);

					mFileUploadProgressHandler.sendMessage(msg);
				} else if (result == 0) {
					Message msg = Message.obtain(mFileUploadProgressHandler,
							Main.FILEUPLOAD_FAILED);

					mFileUploadProgressHandler.sendMessage(msg);
				}
			} else {
			}
		} catch (Exception e) {
			MyLog.d(TAG, "fileUploadException : " + mFileUploadProgressHandler);
		}
	}

	public void setmFileUploadProgressHandler(Handler handler) {
		mFileUploadProgressHandler = handler;
	}

}
