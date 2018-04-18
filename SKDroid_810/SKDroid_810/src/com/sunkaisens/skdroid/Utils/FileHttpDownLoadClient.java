package com.sunkaisens.skdroid.Utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.model.NgnHistorySMSEvent;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.utils.MyLog;

import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Screens.ScreenChat;
import com.sunkaisens.skdroid.adapter.ScreenChatAdapter;
import com.sunkaisens.skdroid.model.ModelFileTransport;
//import com.sunkaisens.skdroid.Screens.ScreenChat.ScreenChatAdapter;

public class FileHttpDownLoadClient {

	private static String TAG = FileHttpDownLoadClient.class.getCanonicalName();

	public String fRemoteURL = "";
	public String fSavePath = "";
	public boolean result = false;
	public long sumLength = 0;// sum length of the file
	public long downloadLength = 0;// download length of the file
	NgnHistorySMSEvent chatEvent = null;
	ScreenChatAdapter chatAdapter = null;
	private boolean bCancel = false;

	private INgnHistoryService mHistoryService;

	private Handler mProgressHandler = null; // ������ˢ��
	private RoundProgressBar roundProgressBar; // �Զ���Բ�ν�����
	private ProgressBar progressBar; // ϵͳ�Դ�������

	private Handler mRefreshHandler = null; // ����ˢ��

	public boolean isDowbloading = false;

	public int position = -1;

	private HttpURLConnection conn;

	// download map,wangds added 2014.7.12
	public static HashMap<String, FileHttpDownLoadClient> downloadMap = new HashMap<String, FileHttpDownLoadClient>();

	public volatile static List<String> downloadList = new ArrayList<String>();

	final int MAX_DOWNLOAD_FILE_NUM = 10;

	public Thread fileDownloadThreadAddToExcetive = null;

	public FileHttpDownLoadClient() {
		mHistoryService = Engine.getInstance().getHistoryService();
	}

	public void setmProgressHandler(Handler mProgressHandler) {
		this.mProgressHandler = mProgressHandler;
	}

	public void httpDownloadFileInThread(ModelFileTransport fileModel,
			NgnHistorySMSEvent event, ScreenChatAdapter mAdapter) {
		MyLog.i(TAG, "httpDownloadFileInThread()");
		// fRemoteURL = remoteURL;
		// 192.168.0.139:13000/files/19811205001_UE64752a52-018b-42e4-9c0e-01c43165f64a_ʯ��ׯ54��
		// �����ձ�_2 0140707.docx
		// 192.168.0.139:13000/files/19811205001_UE64752a52-018b-42e4-9c0e-01c43165f64a_%E7%9F%B3%E5%AE%B6%E5%BA%8454%E6%89%80++%E8%81%94%E8%AF%95%E6%97%A5%E6%8A%A5_2++0140707.docx
		// MyLog.d(TAG, "&&& �����ļ� fileModel:"+fileModel.toString_recv());
		isDowbloading = true;
		fRemoteURL = fileModel.url;
		fSavePath = fileModel.savePath;
		chatEvent = event;
		chatAdapter = mAdapter;
		bCancel = false;

		downloadList.add(fSavePath);

		MyLog.d(TAG, "�ļ������أ�"+fileModel.toString());
		fileDownloadThreadAddToExcetive = new Thread(new Runnable() {
			public void run() {

				if (!bCancel) { // �����ļ����ع�����ȡ���˻�δִ�е��̣߳����ý����̴߳��̳߳���ȥ������û����Ӧ�ӿڣ�ֻ������ʲôҲ����

					String content = chatEvent.getContent();
					if (content != null) {
						if (content.contains("receive")) {
							chatEvent.setContent(content.replace(
									"status:receive", "status:receiving"));
						} else if (content.contains("failed")) {
							chatEvent.setContent(content.replace(
									"status:failed", "status:receiving"));
						} else if (content.contains("cancel")) {
							chatEvent.setContent(content.replace(
									"status:cancel", "status:receiving"));
						}
						mHistoryService.updateEvent(chatEvent);

						sendRefreshMsg();

					}
					//
					result = FileHttpDownLoadClient.this.httpDownloadFile(
							fRemoteURL, fSavePath);
					if (result) {
						content = chatEvent.getContent();
						if (content != null) {
							chatEvent.setContent(content.replace(
									"status:receive", "status:ok"));
							chatEvent.setContent(content.replace(
									"status:receiving", "status:ok"));

							// ��������download����
							chatAdapter.clearDownload(chatEvent.getLocalMsgID());

							MyLog.d(TAG,
									"httpDownloadFileInThread &&&& �ļ����سɹ�  content:"
											+ chatEvent.getContent().toString());

						}
					} else {
						content = chatEvent.getContent();
						if (content != null) {
							if (bCancel == true)
								chatEvent.setContent(content.replace(
										"status:receiving", "status:cancel"));
							else
								chatEvent.setContent(content.replace(
										"status:receiving", "status:failed"));
						}
					}

					sendRefreshMsg();
					mHistoryService.updateEvent(chatEvent);
					isDowbloading = false;

				}
			}
		});
		// t.start();
		ScreenChat.singleDownloadThreadExecutorService
				.submit(fileDownloadThreadAddToExcetive);

	}

	public void cancel() {
		MyLog.d(TAG, "cancel()");
		bCancel = true;
		isDowbloading = false;
		String content = chatEvent.getContent();
		if (content != null) {
			if (bCancel == true)
				chatEvent.setContent(content.replace("status:receiving",
						"status:cancel"));
			else
				chatEvent.setContent(content.replace("status:receiving",
						"status:failed"));
		}

		mHistoryService.updateEvent(chatEvent);
		sendRefreshMsg();

	}

	/**
	 * �����ļ�
	 * 
	 * @param remoteURL
	 *            �ļ�url
	 * @param savepath
	 *            �ļ����ش洢·��
	 * @return
	 */
	public boolean httpDownloadFile(String remoteURL, String savepath) {
		int len = 0;

		String tag = savepath;

		// if (downloadList != null
		// && downloadList.size() >= MAX_DOWNLOAD_FILE_NUM) { // �ļ��ϴ���������Ϊ10
		// MyLog.d(TAG, "�ļ��ϴ���������10��");
		// sendNotify(Main.FILEDOWN_EXTRA);
		// cancel();
		// return false;
		// }

		// downloadList.add(tag);

		InputStream inStream = null;
		FileOutputStream outStream = null;

		try {
			result = false;
			URL url;
			if (!remoteURL.startsWith("http://")) {
				remoteURL = "http://" + remoteURL;
			}
			MyLog.d(TAG, "httpDownloadFile &&&&& �ļ�����URL:" + remoteURL);

			url = new URL(remoteURL);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(120 * 1000);// 30�볬ʱ
			conn.setReadTimeout(120 * 1000);
			conn.setRequestProperty("Connection", "Close");
			sumLength = conn.getHeaderFieldInt("Length", 0);
			MyLog.d(TAG, "down sumlength:" + sumLength);
			inStream = conn.getInputStream();

			outStream = new FileOutputStream(savepath);
			byte[] buffer = new byte[1024];
			int persent = 0;
			int lastPercent = persent;

			sendHandlerMsg("progress", String.valueOf(persent));

			downloadLength = 0;

			while ((len = inStream.read(buffer)) != -1) {
				// MyLog.i(TAG, "Read1 length="+len);
				if (bCancel == true) {
					inStream.close();
					outStream.close();
					conn.disconnect();
					downloadList.remove(tag);
					return false;
				}
				outStream.write(buffer, 0, len);
				downloadLength += len;
				float persent1 = (float) ((float) downloadLength / (float) sumLength);
				persent = (int) (persent1 * 100);
				if (persent > lastPercent) {
					lastPercent = persent;

					sendHandlerMsg("progress", String.valueOf(persent));
				}
				// Log.e("persent", ""+persent);
			}
			conn.disconnect();
			isDowbloading = false;
			result = true;
			downloadList.remove(tag);
			return result;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			downloadList.remove(tag);
			return false;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			downloadList.remove(tag);
			return false;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			sendHandlerMsg(
					"exception",
					NgnApplication.getContext().getString(
							R.string.save_path_not_exist));
			downloadList.remove(tag);
			return false;
		} catch (IOException e) {
			sendHandlerMsg("exception", e.getMessage());
			e.printStackTrace();
			downloadList.remove(tag);
			return false;
		} finally {
			try {
				if (inStream != null) {
					inStream.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			try {
				if (outStream != null) {
					outStream.close();
				}
			} catch (Exception e2) {
			}
		}

	}

	// private void sendNotify(int what) {
	// try {
	// if (mProgressHandler != null) {
	// Message msg = Message.obtain(mProgressHandler, what);
	// mProgressHandler.sendMessage(msg);
	// } else {
	// MyLog.d(TAG, "&&&  ���ؽ���HandlerΪ��");
	// }
	// } catch (Exception e) {
	// MyLog.d(TAG, "fileDownLoadException : " + mProgressHandler);
	// }
	// }

	private void sendHandlerMsg(String msgType, String data) {
		MyLog.d(TAG, "sendHandlerMsg");

		if (mProgressHandler != null) {
			Message msg = Message.obtain(mProgressHandler,
					Main.FILEDOWNLOADPROGRESS);
			msg.getData().putString("msgType", msgType);
			msg.getData().putString("msgData", data);
			msg.getData().putInt("position", position);
			mProgressHandler.sendMessage(msg);
		} else {
			MyLog.d(TAG, "&&&&& �ļ����ؽ���HandlerΪ��");
		}
	}

	private void sendRefreshMsg() {

		MyLog.e(TAG, "sendRefreshMsg()");
		if (mRefreshHandler != null) {
			Message msg = Message.obtain(mRefreshHandler,
					ScreenChat.REFRESH_CHAT);
			mRefreshHandler.sendMessage(msg);
		} else {
			MyLog.d(TAG, "&&&&& chatˢ��HandlerΪ��");
		}
	}

	public RoundProgressBar getRoundProgressBar() {
		return roundProgressBar;
	}

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public void setRoundProgress(RoundProgressBar progress) {
		this.roundProgressBar = progress;
	}

	public void setProgress(ProgressBar progress) {
		this.progressBar = progress;
	}

	public Handler getmRefreshHandler() {
		return mRefreshHandler;
	}

	public void setmRefreshHandler(Handler mRefreshHandler) {
		this.mRefreshHandler = mRefreshHandler;
	}

	/**
	 * ��������б�
	 */
	public static void clearDownloadMap() {
		if (downloadMap != null) {
			downloadMap.clear();
		}
	}

}
