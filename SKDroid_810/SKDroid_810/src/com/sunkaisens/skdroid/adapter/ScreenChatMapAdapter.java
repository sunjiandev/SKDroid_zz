package com.sunkaisens.skdroid.adapter;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.model.NgnHistoryEvent.StatusType;
import org.doubango.ngn.model.NgnHistorySMSEvent;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.utils.MyLog;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Screens.ScreenMap;
import com.sunkaisens.skdroid.Screens.ScreenMap.HistoryEventChatMapFilter;
import com.sunkaisens.skdroid.Services.ServiceLoginAccount;
import com.sunkaisens.skdroid.Utils.DateTimeUtils;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.Utils.Tools_data;
import com.sunkaisens.skdroid.model.ModelFileTransport;
import com.sunkaisens.skdroid.util.GlobalVar;

/**
 * ScreenChatMapAdapter
 */
public class ScreenChatMapAdapter extends BaseAdapter implements Observer {
	private static String TAG = ScreenChatMapAdapter.class.getCanonicalName();

	private List<NgnHistoryEvent> mEvents;
	private final LayoutInflater mInflater;
	private final Handler mHandler;
	private final Context mContext;

	private final INgnHistoryService mHistorytService;
	// private final INgnSipService mSipService;
	private static int DEFAULT_MESSAGE_TIME_OUT = 2 * 3600;// default 2 hours

	public ScreenChatMapAdapter(Context context) {
		mContext = context;
		mHandler = new Handler();
		mInflater = LayoutInflater.from(mContext);
		mHistorytService = Engine.getInstance().getHistoryService();
		// mSipService = Engine.getInstance().getSipService();
		mEvents = mHistorytService.getObservableEvents().filter(
				new HistoryEventChatMapFilter());
		Log.e(TAG, "mEvents====" + mEvents);
		mEvents = sortEvents(mEvents);
		mHistorytService.getObservableEvents().addObserver(this);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		mHistorytService.getObservableEvents().deleteObserver(this);
	}

	public void refresh() {
		mEvents = mHistorytService.getObservableEvents().filter(
				new HistoryEventChatMapFilter());
		mEvents = sortEvents(mEvents);
		if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
			notifyDataSetChanged();
		} else {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					notifyDataSetChanged();
				}
			});
		}
	}

	@Override
	public int getCount() {
		return mEvents.size();
	}

	@Override
	public Object getItem(int position) {
		return mEvents.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		final NgnHistoryEvent event = (NgnHistoryEvent) getItem(position);
		if (event == null) {
			return null;
		}
		if (view == null) {
			switch (event.getMediaType()) {
			case Audio:

			case AudioVideo:

			case FileTransfer:

			default:
				Log.e(TAG, "Invalid media type");
				return null;
			case SMS:
				view = mInflater.inflate(R.layout.screen_chat_item, null);
				break;
			}
		}

		final NgnHistorySMSEvent hisSMSEvent = (NgnHistorySMSEvent) event;
		String content = hisSMSEvent.getContent();
		String remoteNumber = ScreenMap.getRemoteParty();
		if (hisSMSEvent.getGMMember() != null
				&& !hisSMSEvent.getGMMember().isEmpty()) {
			content = hisSMSEvent.getGMMember() + ":\n" + content;
			remoteNumber = hisSMSEvent.getGMMember();
		}

		// 改变自己和对方的位置，即自己在屏幕右侧，对方在屏幕左侧
		final boolean bIncoming = !(hisSMSEvent.getStatus() == StatusType.Incoming);

		final ProgressBar progressBar = (ProgressBar) view
				.findViewById(R.id.screen_chat_item_file_progress);
		final Button fileBtn = (Button) view
				.findViewById(R.id.screen_chat_item_file_btn);

		progressBar.setVisibility(View.GONE);
		fileBtn.setVisibility(View.GONE);

		RelativeLayout container = (RelativeLayout) view
				.findViewById(R.id.msg_body);
		int status = 0;
		// fileBtn.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View view) {
		// NgnHistorySMSEvent tagevent = (NgnHistorySMSEvent) fileBtn
		// .getTag();
		// String title = fileBtn.getText().toString();
		// if (tagevent.getStatus() == StatusType.Incoming) {
		// if (title.equals("接收")) {
		// String[] urlnnames = FileHttpDownLoadClient
		// .getURLnFileNameFromContent(tagevent
		// .getContent());
		//
		// MyLog.d(TAG,
		// "文件传输  接收 urlnnames:"+urlnnames+"|tagevent.getLocalMsgID():"+tagevent.getLocalMsgID());
		//
		// if (urlnnames == null)
		// return;
		// FileHttpDownLoadClient download = new FileHttpDownLoadClient();
		// mContext.downloadMap.put(
		// tagevent.getLocalMsgID(), download);
		// download.httpDownloadFileInThread(urlnnames[0],
		// urlnnames[2], tagevent,
		// mContext.mAdapter);
		// //gzc 20140818 接收文件传输进度，更新进度条
		// Handler progressHandler = new Handler() {
		// public void handleMessage(Message msg) {
		// try {
		// switch (msg.what) {
		// case Main.FILEDOWNLOADPROGRESS:
		// String msgType = msg.getData().getString("msgType");
		// if ("progress".equals(msgType)) {
		// int progress = Integer.parseInt(msg.getData().getString("msgData"));
		// MyLog.d(TAG,
		// "ScreenChat file download progress : "
		// + progress);
		// if (progress < 100) {
		// progressBar
		// .setProgress(progress);
		// progressBar
		// .setVisibility(View.VISIBLE);
		// } else {
		// progressBar
		// .setVisibility(View.GONE);
		// }
		// }
		// if("exception".equals(msgType)){
		// String reason = msg.getData().getString("msgData");
		// SystemVarTools.showToast("文件下载："+reason);
		// }
		// break;
		// default:
		// break;
		// }
		// } catch (Exception e) {
		// MyLog.d(TAG,
		// "ScreenChat Exception line 940"
		// + e.getMessage());
		// }
		// }
		// };
		// download.setmProgressHandler(progressHandler);
		//
		// } else if (title.equals("取消")) {
		// if (mContext.downloadMap.containsKey(tagevent
		// .getLocalMsgID())) {//
		// MyLog.d(TAG,
		// "文件传输  取消 tagevent.getLocalMsgID:"+tagevent.getLocalMsgID()+"|"
		// + "download:"+mContext.downloadMap.get(
		// tagevent.getLocalMsgID()));
		// mContext.downloadMap.get(
		// tagevent.getLocalMsgID()).cancel();
		// }
		// } else if (title.equals("打开")) {
		// String[] urlnnames = FileHttpDownLoadClient
		// .getURLnFileNameFromContent(tagevent
		// .getContent());
		// if (urlnnames == null)
		// return;
		// Intent intent = new Intent();
		// intent.setAction(Intent.ACTION_VIEW);
		// File file = new File(urlnnames[2]);
		// String type = "*/*";
		// // text/plain image/jpeg image/gif image/bmp
		// // image/png
		// if (urlnnames[2].endsWith(".jpg")
		// || urlnnames[2].endsWith(".jpeg")
		// || urlnnames[2].endsWith(".png")
		// || urlnnames[2].endsWith(".bmp")
		// || urlnnames[2].endsWith(".gif")) {
		// type = "image/*";
		// } else if (urlnnames[2].endsWith(".txt")) {
		// type = "text/*";
		// } else if (urlnnames[2].endsWith(".mp3")) {
		// type = "audio/*";
		// }
		// intent.setDataAndType(Uri.fromFile(file), type);
		// mContext.startActivity(intent);
		// }else if (title.equals("重新接收")) {
		// String[] urlnnames = FileHttpDownLoadClient
		// .getURLnFileNameFromContent(tagevent
		// .getContent());
		// if (urlnnames == null&&urlnnames.length < 3)
		// return;
		// if(urlnnames[0] == null){
		// SystemVarTools.showToast("文件传输：下载地址错误");
		// return;
		// }
		// if(urlnnames[2] == null){
		// SystemVarTools.showToast("文件传输：文件保存路径不存在");
		// return;
		// }
		//
		// MyLog.d(TAG,
		// "文件传输  重新接收 tagevent.getLocalMsgID:"+tagevent.getLocalMsgID()+"|"
		// + "download:"+mContext.downloadMap.get(tagevent.getLocalMsgID()));
		//
		// FileHttpDownLoadClient download =
		// mContext.downloadMap.get(tagevent.getLocalMsgID());
		// if(download == null){
		// return;
		// }
		// download.httpDownloadFileInThread(urlnnames[0],
		// urlnnames[2], tagevent,
		// mContext.mAdapter);
		// //gzc 20140818 接收文件传输进度，更新进度条
		// Handler progressHandler = new Handler() {
		// public void handleMessage(Message msg) {
		// try {
		// switch (msg.what) {
		// case Main.FILEDOWNLOADPROGRESS:
		// String msgType = msg.getData().getString("msgType");
		// if ("progress".equals(msgType)) {
		// int progress = Integer.parseInt(msg.getData().getString("msgData"));
		// MyLog.d(TAG,
		// "ScreenChat file download progress : "
		// + progress);
		// if (progress < 100) {
		// progressBar
		// .setProgress(progress);
		// progressBar
		// .setVisibility(View.VISIBLE);
		// } else {
		// progressBar
		// .setVisibility(View.GONE);
		// }
		// }
		// if("exception".equals(msgType)){
		// String reason = msg.getData().getString("msgData");
		// SystemVarTools.showToast("文件下载失败："+reason);
		// }
		// break;
		// default:
		// break;
		// }
		// } catch (Exception e) {
		// MyLog.d(TAG,
		// "ScreenChat Exception line 293"
		// + e.getMessage());
		// }
		// }
		// };
		// download.setmProgressHandler(progressHandler);
		// }
		// } else {
		// if (title.equals("取消")) {
		// // if (mContext.uploadMap.containsKey(tagevent.getLocalMsgID())) {//
		// 取消就删除掉该文件历史信息。
		// // mContext.uploadMap.get(tagevent.getLocalMsgID()).cancel();
		// if (ScreenChat.uploadMap.containsKey(tagevent.getLocalMsgID())) {//
		// 取消就删除掉该文件历史信息。
		// ScreenChat.uploadMap.get(tagevent.getLocalMsgID()).cancel();
		// Engine.getInstance().getHistoryService().deleteEvent(tagevent);
		// }
		// }
		// }
		//
		// }
		// });
		fileBtn.setTag(event);// 关联聊天事件。

		TextView textView = (TextView) view
				.findViewById(R.id.screen_chat_item_textView);
		textView.setText(content == null ? NgnStringUtils.emptyValue()
				: content);
		RelativeLayout msgBodySub = (RelativeLayout) view
				.findViewById(R.id.msg_body_sub);
		if (bIncoming) {
			msgBodySub.setBackgroundResource(R.drawable.chat_bg2);
			textView.setTextColor(mContext.getResources().getColor(
					R.color.color_text2));
		} else {
			msgBodySub.setBackgroundResource(R.drawable.chat_bg1);
			textView.setTextColor(mContext.getResources().getColor(
					R.color.color_text1));
		}

		if (!bIncoming) {
			// 判断是否为文件传输，ui做相应调整
			if (content != null && content.startsWith("type:file")) {
				// get the file base infomation.
				ModelFileTransport fileModel = new ModelFileTransport();
				fileModel.parseFileContent(content);
				MyLog.d(TAG, "文件传输  urlnnames:" + fileModel.toString_send());
				if (content.contains("status:waitreceive")) {
					progressBar.setVisibility(View.GONE);
					fileBtn.setVisibility(View.VISIBLE);
					fileBtn.setText(mContext.getString(R.string.receive));
					if (fileModel.name != null)
						textView.setText(mContext
								.getString(R.string.file_change_line)
								+ fileModel.name);
					else
						textView.setText(content);
				} else if (content.contains("status:receiving")) {
					// progressBar.setVisibility(View.VISIBLE);
					fileBtn.setVisibility(View.VISIBLE);
					fileBtn.setText(mContext.getString(R.string.cancel));
					if (fileModel.name != null)
						textView.setText(mContext
								.getString(R.string.file_change_line)
								+ fileModel.name);
					else
						textView.setText(content);
				} else if (content.contains("status:receivecancel")
						|| content.contains("status:receivefailed")) {
					progressBar.setVisibility(View.GONE);
					fileBtn.setVisibility(View.VISIBLE);
					fileBtn.setText(mContext.getString(R.string.rereceive));
					if (fileModel.name != null)
						textView.setText(mContext
								.getString(R.string.file_change_line)
								+ fileModel.name);
					else
						textView.setText(content);
				} else if (content.contains("status:receiveok")) {
					progressBar.setVisibility(View.GONE);
					fileBtn.setVisibility(View.VISIBLE);
					fileBtn.setText(mContext.getString(R.string.open_file));
					if (fileModel.name != null)
						textView.setText(mContext
								.getString(R.string.file_change_line)
								+ fileModel.name
								+ mContext
										.getString(R.string.receive_finished_change_line));
					else
						textView.setText(content);
				} else {
					textView.setText(content);
				}
			} else {
				progressBar.setVisibility(View.GONE);
				fileBtn.setVisibility(View.GONE);
			}

		} else { // bIncoming
			// if (content != null && content.contains("type:filetransfer")) {
			// if (content.contains("status:waitreceive")) {
			// String[] urlnnames =
			// FileHttpDownLoadClient.getURLnFileNameFromContent(content);
			// if (urlnnames != null)
			// textView.setText("文件\r\n" + urlnnames[1] + "\r\n发送成功！");
			// else
			// textView.setText("文件发送成功!\r\n" + content);
			//
			// fileBtn.setVisibility(View.GONE);
			// } else if (content.contains("status:sendfailed")) {
			// String[] urlnnames =
			// FileHttpDownLoadClient.getURLnFileNameFromContent(content);
			// if (urlnnames != null)
			// textView.setText("文件\r\n" + urlnnames[1] + "\r\n发送失败！" +
			// content.substring(content.indexOf("status:sendfailed") + 17));
			// else
			// textView.setText("文件发送失败!\r\n" + content);
			//
			// fileBtn.setVisibility(View.GONE);
			// } else { //status:sending
			// String[] urlnnames =
			// FileHttpDownLoadClient.getURLnFileNameFromContent(content);
			// // FileHttpUpLoadClient upload =
			// mContext.uploadMap.get(hisSMSEvent.getLocalMsgID());
			// FileHttpUpLoadClient upload =
			// ScreenChat.uploadMap.get(hisSMSEvent.getLocalMsgID());
			// Log.e(TAG, "zhaohua20141029 hisSMSEvent.getLocalMsgID() = " +
			// hisSMSEvent.getLocalMsgID());
			// Log.e(TAG, "zhaohua20141029 ScreenChat.uploadMap = " +
			// ScreenChat.uploadMap);
			// Log.e(TAG, "zhaohua20141029 upload = " + upload);
			// //接收文件上传进度，更新进度条
			// //gzc 20140818
			// Handler progressHandler = new Handler() {
			// public void handleMessage(Message msg) {
			// try {
			// switch (msg.what) {
			// case Main.FILEUPLOADPROGRESS:
			// int progress = msg.getData().getInt("fileTransferProgress");
			// MyLog.d(TAG, "ScreenChat file upload progress : "+progress);
			// if(progress < 100){
			// progressBar.setProgress(progress);
			// progressBar.setVisibility(View.VISIBLE);
			// }else{
			// progressBar.setVisibility(View.GONE);
			// }
			// break;
			// default:
			// break;
			// }
			// } catch (Exception e) {
			// MyLog.d(TAG,
			// "ScreenChat Exception line 940"
			// + e.getMessage());
			// }
			// }
			// };
			// upload.setmFileUploadProgressHandler(progressHandler);
			// if (urlnnames != null)
			// textView.setText("文件\r\n" + urlnnames[1] + "\r\n正在发送！");
			// else
			// textView.setText("文件正在发送!\r\n" + content);
			// fileBtn.setVisibility(View.VISIBLE);
			// fileBtn.setText("取消");
			// }
			// }
		}

		// 即时消息显示以本地时间为准
		((TextView) view.findViewById(R.id.screen_chat_item_textView_date))
				.setText(DateTimeUtils.getFriendlyDateString(new Date(event
						.getEndTime())));

		TextView textView_status = (TextView) view
				.findViewById(R.id.screen_chat_item_textView_status);
		textView_status.setText(mContext.getString(R.string.send_allready));
		if (ServiceLoginAccount.mMessageIDHashMap != null
				&& ServiceLoginAccount.mMessageIDHashMap
						.containsKey(hisSMSEvent.getLocalMsgID())
				&& GlobalVar.bADHocMode == false) {
			textView_status.setText(mContext.getString(R.string.send_arrived));
		}
		textView_status.setVisibility(!bIncoming ? View.GONE : View.VISIBLE);

		// remote icon
		ImageView remoteicon = (ImageView) view
				.findViewById(R.id.screen_chat_item_iconleft);
		remoteicon.setVisibility(bIncoming ? View.GONE : View.VISIBLE);
		remoteicon.setImageResource(SystemVarTools.getThumbID(SystemVarTools
				.getImageIDFromNumber(remoteNumber)));

		// my icon
		ImageView myicon = (ImageView) view
				.findViewById(R.id.screen_chat_item_iconright);
		myicon.setVisibility(bIncoming ? View.VISIBLE : View.GONE);
		myicon.setImageResource(SystemVarTools.getThumbID(SystemVarTools
				.getImageIDFromNumber(Engine
						.getInstance()
						.getConfigurationService()
						.getString(NgnConfigurationEntry.IDENTITY_IMPI,
								NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI))));

		// add by gle 20140605
		if (ServiceLoginAccount.mMessageIDHashMap != null) {
			boolean isRemove = false;
			Iterator keys = ServiceLoginAccount.mMessageIDHashMap.keySet()
					.iterator();
			while (keys.hasNext()) {
				long currentTime = System.currentTimeMillis();
				String key = (String) keys.next();
				String lastTime = ServiceLoginAccount.mMessageIDHashMap
						.get(key).toString();
				if (currentTime / 1000 - Long.valueOf(lastTime) > DEFAULT_MESSAGE_TIME_OUT) {
					keys.remove();
					isRemove = true;
					MyLog.d("", "currentTime Remove OK");
				}
			}
			if (isRemove) {
				try {
					Tools_data
							.writeIDHashMap(ServiceLoginAccount.mMessageIDHashMap);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return view;
	}

	@Override
	public void update(Observable observable, Object data) {
		refresh();
	}

	// 对短信记录进行升序排序 gzc 20141108
	private List<NgnHistoryEvent> sortEvents(List<NgnHistoryEvent> events) {
		if (events == null || events.size() < 1) {
			return events;
		}
		NgnHistoryEvent tmpEvent = null;
		for (int i = 0; i < events.size(); i++) {
			for (int j = 1; j < events.size() - i; j++) {
				if (events.get(j - 1).getStartTime() > events.get(j)
						.getStartTime()) {
					tmpEvent = events.get(j);
					events.set(j, events.get(j - 1));
					events.set(j - 1, tmpEvent);
				}
			}
		}

		return events;
	}

}
