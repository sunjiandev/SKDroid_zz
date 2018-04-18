package com.sunkaisens.skdroid.Utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.model.NgnHistorySMSEvent;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.utils.MyLog;

import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.Screens.ScreenChat;
import com.sunkaisens.skdroid.adapter.ScreenChatAdapter;
import com.sunkaisens.skdroid.model.ModelContact;
//import com.sunkaisens.skdroid.Screens.ScreenChat.ScreenChatAdapter;

public class FileHttpUpLoadClient {

	public static String TAG = FileHttpUpLoadClient.class.getCanonicalName();

	// http://appserver.test.com:8010
	public String serverRoot = "http://"
			+ NgnEngine
					.getInstance()
					.getConfigurationService()
					.getString(NgnConfigurationEntry.FILE_SERVER_URL,
							NgnConfigurationEntry.DEFAULT_FILE_SERVER_URL);

	public String fLocalPath = "";
	public String fSaveMame = "";
	public String result = "";
	public long sumLength = 0;// sum length of the file
	public long uploadLength = 0;// upload length of the file
	private NgnHistorySMSEvent chatEvent = null;
	private boolean isGroup = false;

	public boolean isUploading = false;

	private INgnHistoryService mHistoryService;

	private Handler mRefreshHandler = null;

	private RoundProgressBar roundProgressBar;

	private ProgressBar progressBar;

	private Handler mFileUploadProgressHandler = null;

	// upload map,wangds added 2014.7.12.
	public static HashMap<String, FileHttpUpLoadClient> uploadMap = new HashMap<String, FileHttpUpLoadClient>();

	public volatile static List<String> uploadList = new ArrayList<String>();

	// final int MAX_UPLOAD_FILE_NUM=10;

	public FileHttpUpLoadClient() {
		mHistoryService = Engine.getInstance().getHistoryService();
	}

	public void setmFileUploadProgressHandler(Handler mFileUploadProgressHandler) {
		this.mFileUploadProgressHandler = mFileUploadProgressHandler;
	}

	public NgnHistorySMSEvent getChatEvent() {
		return chatEvent;
	}

	ScreenChatAdapter chatAdapter = null;
	private boolean bCancel = false;

	private HttpURLConnection connection;

	public void httpSendFileInThread(String localpath, String savename,
			NgnHistorySMSEvent event, ScreenChatAdapter mAdapter) {

		MyLog.d(TAG, "httpSendFileInThread()");
		MyLog.d("ywh", "�ļ���·��---"+localpath+"------"+savename);
		isUploading = true;
		if (bCancel) {
			bCancel = false;
		}

		// serverRoot
		// =NgnEngine.getInstance().getConfigurationService().getString(NgnConfigurationEntry.FILE_SERVER_URL,NgnConfigurationEntry.DEFAULT_FILE_SERVER_URL);
		if (!serverRoot.startsWith("http://")) {
			serverRoot = "http://" + serverRoot;
		}
		fLocalPath = localpath;
		// fSaveMame = savename;
		try {
			fSaveMame = URLEncoder.encode(savename, "UTF-8");
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		chatEvent = event;
		chatAdapter = mAdapter;

		ModelContact mc = SystemVarTools.createContactFromRemoteParty(event
				.getRemoteParty());
		if (mc.isgroup) {
			isGroup = true;
		} else {
			isGroup = false;
		}
		//
		Thread t = new Thread(new Runnable() {
			public void run() {

				result = FileHttpUpLoadClient.this.httpSendFile(fLocalPath,
						fSaveMame, chatEvent, isGroup);
				uploadLength = 0;
				MyLog.d(TAG, "upload result : " + result);
				if (chatEvent == null)
					return;
				String content = null;
				if (result != null && result.startsWith("true")) {
					content = chatEvent.getContent();
					chatAdapter.clearUpload(chatEvent.getLocalMsgID());

					if (content != null) {
						result = result.replace("\n", "");
						content = content.replace("status:sending",
								"status:send/success");
						chatEvent.setContent(content);
						MyLog.d(TAG,
								"httpSendFileInThread $$$$ 文件上传成功  content :"
										+ content);
						sendRefreshMsg();
						mHistoryService.updateEvent(chatEvent);

						int readlength = 0;
						byte[] buffer = new byte[1024];
						StringBuffer sb = new StringBuffer("");
						try {
							while ((readlength = connection.getInputStream()
									.read(buffer)) > 0) {
								sb.append(new String(buffer, 0, readlength));
							}

							MyLog.d(TAG,
									"uploadPersent response : " + sb.toString());
							connection.getInputStream().close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						connection.disconnect();
						connection = null;

						String contentTmp = chatEvent.getContent();
						contentTmp = contentTmp.replace("url:" + fSaveMame, sb
								.toString().trim());
						chatEvent.setContent(contentTmp);
						MyLog.e(TAG,
								"httpSendFileInThread $$$$ 文件上传成功  收到响应  content :"
										+ contentTmp);

						String[] res = sb.toString().split("/");
						String fileId = res[res.length - 1];
						chatEvent.setLocalMsgID(fileId);
						MyLog.e("",
								"httpSendFileInThread &&&&&&  文件上传  响应 msgId:"

								+ chatEvent.getLocalMsgID());

					}

				} else { // false
					content = chatEvent.getContent();
					if (content != null) {
						if (bCancel == true) {
							chatEvent.setContent(content.replace(
									"status:sending", "status:send/cancel"));
							MyLog.d(TAG, "Upload cancel.");
						} else {
							chatEvent.setContent(content.replace(
									"status:sending", "status:send/failed\r\n"
											+ serverRoot + "Upload failed."));
							MyLog.d(TAG, "Upload failed.");
						}
					}
				}
				sendRefreshMsg();
				mHistoryService.updateEvent(chatEvent);
				isUploading = false;

			}
		});

		ScreenChat.singleUploadThreadExecutorService.submit(t);

	}

	public void cancel() {
		bCancel = true;
		isUploading = false;
		String content = chatEvent.getContent();
		if (content != null) {
			if (bCancel == true)
				chatEvent.setContent(content.replace("status:sending",
						"status:send/cancel"));
			else
				chatEvent.setContent(content.replace("status:sending",
						"status:send/failed"));
		}
		mHistoryService.updateEvent(chatEvent);
		sendRefreshMsg();
	}

	public String httpSendFile(String localpath, String savename,
			NgnHistorySMSEvent event, boolean isGroup) {

		MyLog.e(TAG, "httpSendFile()");

		String fromUserNo = event.getmLocalParty();
		String toUserNo = event.getRemoteParty();
		// POST
		// http://192.168.1.192:8010/files/[filename];from=[from];to=[to];isGroup=[]
		String urlTmp = serverRoot + "/files/" + savename + ";from="
				+ fromUserNo + ";to=" + toUserNo + ";isgroup=" + isGroup;
		MyLog.d(TAG, "httpSendFile &&&& 上传URL:" + urlTmp);

		String tag = savename + toUserNo;

		// if (uploadList != null && uploadList.size() >= MAX_UPLOAD_FILE_NUM) {
		// sendNotify(Main.FILEUPLOAD_EXTRA);
		// cancel();
		// return "false";
		// } else {
		if (uploadList.contains(tag)) {
			MyLog.d(TAG, "File[" + savename + "] is uploading.");
			sendNotify(Main.FILEUPLOAD_INPROGRESS);
			cancel();
			return "false";
		} else {
			uploadList.add(tag);
		}
		// }

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
			connection.setRequestProperty("Content-Type",
					"application/octet-stream");

			File file = new File(localpath);
			MyLog.d(TAG, "File exist?" + file.exists());
			if (!file.exists()) {
				sendNotify(Main.FILE_NO_EXIST);
				if (uploadList.contains(tag)) {
					uploadList.remove(tag);
				}
				return "false";
			}
			connection.setRequestProperty("Content-Length", "" + file.length());
			sumLength = file.length();

			connection.setFixedLengthStreamingMode(((int) sumLength));

			// if (sumLength == 0) {
			// if (uploadList.contains(tag)) {
			// uploadList.remove(tag);
			// }
			// return "false";
			// }

			connection.connect();
			if (bCancel == true) {
				connection.disconnect();
				connection = null;

				if (uploadList.contains(tag)) {
					uploadList.remove(tag);
				}
				return "false";
			}
			//
			DataInputStream fileinput = new DataInputStream(
					new FileInputStream(localpath));
			byte[] buffer = new byte[1024];
			int readlength = 0;
			int persent = 0;
			int lastPercent = persent;

			OutputStream out = connection.getOutputStream();
			MyLog.d(TAG, "UploadStream  open(" + out + ")");

			refreshProgress(persent, chatEvent);

			while ((readlength = fileinput.read(buffer)) > 0) {
				if (bCancel == true) {
					out.close();
					fileinput.close();
					connection.disconnect();
					if (uploadList.contains(tag)) {
						uploadList.remove(tag);
					}
					return "false";
				}
				out.write(buffer, 0, readlength);
				out.flush();
				uploadLength += readlength;
				float persent1 = (float) ((float) uploadLength / (float) sumLength);
				persent = (int) (persent1 * 100);
				if (persent > lastPercent) {
					lastPercent = persent;
					refreshProgress(persent, chatEvent);
				}

				// if (persent >= 100) {
				// String content = chatEvent.getContent();
				// if (content != null) {
				// content = content.replace("status:sending",
				// "status:send/success");
				// chatEvent.setContent(content);
				// }
				// MyLog.e(TAG, "$$$$ 文件上传成功  content :" + content);
				// }

			}
			out.close();
			MyLog.d(TAG, "UploadStream close(" + out + ")");
			fileinput.close();
			if (uploadList.contains(tag)) {
				uploadList.remove(tag);
			}
			return "true";
		} catch (MalformedURLException e) {
			e.printStackTrace();
			if (uploadList.contains(tag)) {
				uploadList.remove(tag);
			}
			return "false";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			if (uploadList.contains(tag)) {
				uploadList.remove(tag);
			}
			return "false";
		} catch (IOException e) {
			e.printStackTrace();
			if (uploadList.contains(tag)) {
				uploadList.remove(tag);
			}
			return "false";
		}

	}

	public static void httpSendFeedbackBody(String uri, String fb,
			Handler handler) {

		final String uriTmp = uri;
		final String info = fb;

		final Handler handlerT = handler;

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					// String u =
					// uriTmp+"?feedbacker="+info.getUser()+"&phone="+info.getPhone_num()+"&info="+info.getContent();
					// MyLog.d(TAG, "BUG uri=");
					HttpClient client = new DefaultHttpClient();
					HttpPost request = new HttpPost(uriTmp);
					request.addHeader("Content-Type", "application/text-plain");
					// request.addHeader("Content-Length",""+info.length());
					StringEntity se = new StringEntity(info);
					request.setEntity(se);
					HttpResponse response = client.execute(request);
					if (response != null
							&& response.getStatusLine().getStatusCode() == 200) {
						handlerT.sendEmptyMessage(9000);
					} else {
						handlerT.sendEmptyMessage(9001);
					}
				} catch (Exception e) {
					e.printStackTrace();
					handlerT.sendEmptyMessage(9001);
				}
			}
		}).start();

	}

	public static void httpSendFeedbackBodyNotJason(String uri, String info,
			Handler handler) {

		final String uriTmp = uri;
		final String infoTmp = info;

		final Handler handlerT = handler;

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					// String u =
					// uriTmp+"?feedbacker="+info.getUser()+"&phone="+info.getPhone_num()+"&info="+info.getContent();
					// String uriT = uriTmp +
					// "?feedbacker="+Uri.encode(info.getUser())+"&phone="+info.getPhone_num()+"&info="+Uri.encode(info.getContent());
					MyLog.d(TAG, "BUG1 uri=" + uriTmp);
					MyLog.d(TAG, "BUG1 info=" + infoTmp);

					HttpClient client = new DefaultHttpClient();
					HttpPost request = new HttpPost(uriTmp);
					request.addHeader("Content-Type", "application/text-plain");
					StringEntity se = new StringEntity(infoTmp, "utf8");
					request.setEntity(se);
					HttpResponse response = client.execute(request);
					if (response != null
							&& response.getStatusLine().getStatusCode() == 200) {
						InputStream in = response.getEntity().getContent();
						if (in == null) {
							handlerT.sendEmptyMessage(9001);
							return;
						}
						byte[] buf = new byte[10];
						in.read(buf);
						String result = new String(buf);
						MyLog.d("", "BUG result=" + result);
						if (result != null && result.trim().equals("ok")) {
							handlerT.sendEmptyMessage(9000);
						} else {
							handlerT.sendEmptyMessage(9001);
						}
					} else {
						handlerT.sendEmptyMessage(9001);
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (e.getCause().toString()
							.contains("failed to connect to")) {
						handlerT.sendEmptyMessage(9002);
					} else {
						handlerT.sendEmptyMessage(9001);
					}
				}
			}
		}).start();

	}

	private void refreshProgress(int progress, NgnHistorySMSEvent event) {
		try {
			MyLog.d(TAG, "refreshProgress()");

			if (mFileUploadProgressHandler != null) {
				Message msg = Message.obtain(mFileUploadProgressHandler,
						Main.FILEUPLOADPROGRESS);
				msg.getData().putInt("fileTransferProgress", progress);
				msg.getData().putString("msgid", event.getLocalMsgID());
				// msg.getData().putInt("position", position);
				mFileUploadProgressHandler.sendMessage(msg);
				// Log.e("", "更新进度");
			} else {
				MyLog.d(TAG, "&&&  上传进度Handler为空");
			}
		} catch (Exception e) {
			MyLog.d(TAG, "fileUploadException : " + mFileUploadProgressHandler);
		}
	}

	private void sendNotify(int what) {
		try {
			if (mFileUploadProgressHandler != null) {
				Message msg = Message.obtain(mFileUploadProgressHandler, what);
				mFileUploadProgressHandler.sendMessage(msg);
			} else {
				MyLog.d(TAG, "&&&  上传进度Handler为空");
			}
		} catch (Exception e) {
			MyLog.d(TAG, "fileUploadException : " + mFileUploadProgressHandler);
		}
	}

	// add by jgc 2014.12.1閼惧嘲绶辨稉濠佺炊閺傚洣娆㈤惃鍕拱閸︽澘婀撮崸锟�
	public static String getlocalpath(String ftcontent) {

		if (ftcontent == null)
			return null;
		String[] items = ftcontent.split("\r\n");
		String localpath = items[items.length - 1];
		MyLog.d(TAG, "getlocalpath:" + localpath);

		return localpath;

	}

	private void sendRefreshMsg() {
		// Engine.getInstance().getHistoryService().reflush();
		MyLog.e(TAG, "sendRefreshMsg()");
		if (mRefreshHandler != null) {
			Message msg = Message.obtain(mRefreshHandler,
					ScreenChat.REFRESH_CHAT);
			mRefreshHandler.sendMessage(msg);
		} else {
			MyLog.e(TAG, "&&&&& chat刷新Handler为空");
		}
	}

	public RoundProgressBar getRoundProgressBar() {
		return roundProgressBar;
	}

	public ProgressBar getProgressBar() {

		return progressBar;
	}

	public void setRoundProgressBar(RoundProgressBar progressBar) {
		this.roundProgressBar = progressBar;
	}

	public void setProgressBar(ProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	public void setmRefreshHandler(Handler mRefreshHandler) {
		this.mRefreshHandler = mRefreshHandler;
	}

	public static void clearUploadMap() {
		if (uploadMap != null) {
			uploadMap.clear();
		}
	}
}
