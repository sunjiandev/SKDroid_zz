package com.sunkaisens.skdroid.app.service;

import org.doubango.utils.MyLog;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.RemoteViews;

import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.update.SKDroidUpdate;

public class UpdateService extends Service {

	public final static int UPDATE_ID = 19833900;

	private SKDroidUpdate mUpdate;

	private Handler mProgressHandler;

	private Notification notification;

	private NotificationManager notificationManager;

	private RemoteViews mView;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mUpdate = SKDroidUpdate.getSkDroidUpdate();
	}

	@SuppressLint("NewApi")
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		mProgressHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				int progress = msg.getData().getInt("progress");
				MyLog.d("", "UpdateService Progress=" + progress);
				if (progress == 100) {
					mView.setTextViewText(
							R.id.update_status,
							getApplicationContext().getString(
									R.string.notif_kx_download_finished));
					mView.setViewVisibility(R.id.update__progress, View.GONE);
					SKDroidUpdate.getSkDroidUpdate().update();
				} else {
					mView.setTextViewText(
							R.id.update_status,
							getApplicationContext().getString(
									R.string.notif_kx_isdownloading_with_left)
									+ progress + "%)");
					mView.setProgressBar(R.id.update__progress, 100, progress,
							false);
				}

				notificationManager
						.notify(R.layout.update_notify, notification);
			}
		};
		mUpdate.downFile(mProgressHandler);

		createNotifyView();

		Intent updateIntent = new Intent(SKDroid.getContext(), Main.class);
		updateIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		updateIntent.putExtra("action", Main.ACTION_UPDATE_VERSION);

		PendingIntent contentIntent = PendingIntent.getActivity(
				SKDroid.getContext(), UPDATE_ID, updateIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		Builder builder = new Builder(SKDroid.getContext());
		builder.setContentIntent(contentIntent);
		builder.setSmallIcon(R.drawable.icon);
		builder.setTicker(getApplicationContext().getString(
				R.string.notif_kx_isdownloading));
		builder.setWhen(System.currentTimeMillis());
		builder.setContent(mView);

		notification = builder.build();
		notification.flags = Notification.FLAG_ONGOING_EVENT;

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(R.layout.update_notify, notification);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void createNotifyView() {

		mView = new RemoteViews(getPackageName(), R.layout.update_notify);
		mView.setTextViewText(R.id.update_status, getApplicationContext()
				.getString(R.string.notif_kx_isdownloading_zero));
		mView.setProgressBar(R.id.update__progress, 100, 0, false);
	}

}
