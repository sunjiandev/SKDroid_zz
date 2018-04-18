package com.sunkaisens.skdroid.Screens;

//add by jgc 2014.11.27
import java.io.IOException;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.utils.MyLog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Utils.MyiconHttpUpLoadClient;
import com.sunkaisens.skdroid.Utils.RoundProgressBar;
import com.sunkaisens.skdroid.Utils.SystemVarTools;

public class ScreenTakePhotoIstransfer_myicon extends BaseScreen {
	private static String TAG = ScreenTakePhotoIstransfer_myicon.class
			.getCanonicalName();

	private ImageView photoimage;

	private ImageButton bTn_picture_transfer;
	private ImageButton bTn_picture_cancel;
	private RoundProgressBar rounProgress = null;

	private String takephotoUri;

	public static Bitmap showBitmap;
	public static boolean canTransfer = false;

	public final INgnHistoryService mHistorytService;

	public ScreenTakePhotoIstransfer_myicon() {
		super(SCREEN_TYPE.CHAT_T, TAG);

		mHistorytService = getEngine().getHistoryService();
	}

	private Handler progressHandler = new Handler() {
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case Main.FILEUPLOADPROGRESS:

					if (rounProgress != null) {

						int progress = msg.getData().getInt(
								"fileTransferProgress");

						if ((progress % 20) == 0) {
							Log.d(TAG, "upload progress=" + progress);
						}

						if (progress < 100) {
							rounProgress.setProgress(progress);

						} else {
							rounProgress.setVisibility(View.GONE);
						}

					}
					break;

				case Main.FILEUPLOAD_SUCCESS:
					if (rounProgress != null) {
						rounProgress.setVisibility(View.GONE);
					}

					SystemVarTools.showToast(NgnApplication.getContext()
							.getString(R.string.icon_update_success));

					ScreenTakePhotoIstransfer_myicon.this.finish();

					break;

				case Main.FILEUPLOAD_FAILED:
					if (rounProgress != null) {
						rounProgress.setVisibility(View.GONE);
					}
					SystemVarTools.showToast(NgnApplication.getContext()
							.getString(R.string.icon_update_failed));
					break;

				default:
					break;

				}
			} catch (Exception e) {
				MyLog.d(TAG, "ScreenChat Exception line 940" + e.getMessage());
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyLog.e(TAG, "ScreenTakePhotoIstransfer_myicon  onCreate");
		// canTransfer=false;

		setContentView(R.layout.screen_takephoto_istransfer);
		Intent intent = this.getIntent();
		if (intent != null) {
			Bundle extras = intent.getExtras();
			takephotoUri = (String) extras.get("takephotoUri");

		}
		photoimage = (ImageView) findViewById(R.id.screen_chat_takephoto);

		MyLog.e(TAG, "ScreenTakePhotoIstransfer_myicon  " + takephotoUri);

		photoimage.setImageBitmap(showBitmap);

		bTn_picture_transfer = (ImageButton) findViewById(R.id.screen_chat_takephoto_transfer);
		bTn_picture_cancel = (ImageButton) findViewById(R.id.screen_chat_takephoto_cancel);

		rounProgress = (RoundProgressBar) findViewById(R.id.screen_chat_takephoto_send_progress);

		// 设置背景颜色为透明
		bTn_picture_cancel.getBackground().setAlpha(0);
		bTn_picture_transfer.getBackground().setAlpha(0);

		bTn_picture_transfer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (canTransfer) {

					rounProgress.setVisibility(View.VISIBLE);

					MyiconHttpUpLoadClient myiconUpload = new MyiconHttpUpLoadClient();

					myiconUpload.setmFileUploadProgressHandler(progressHandler);
					myiconUpload.httpSendFileInThread(takephotoUri);

				} else {
					SystemVarTools.showToast(NgnApplication.getContext()
							.getString(R.string.saving_file));
				}

			}
		});

		bTn_picture_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();

				intent.setClass(ScreenTakePhotoIstransfer_myicon.this,
						Screen_takephoto_camera_myicon.class);
				startActivity(intent);
				finish();
			}
		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e("", "我结束了");
		canTransfer = false;
	}

	public static int readPictureDegree(String path) {
		int degree = 0;
		try {

			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;

			default:
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return degree;

	}

}
