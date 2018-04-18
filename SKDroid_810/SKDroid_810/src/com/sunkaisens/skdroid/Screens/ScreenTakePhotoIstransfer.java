package com.sunkaisens.skdroid.Screens;

//add by jgc 2014.11.27
import java.io.IOException;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.utils.MyLog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.model.ModelContact;

public class ScreenTakePhotoIstransfer extends BaseScreen {
	private static String TAG = ScreenTakePhotoIstransfer.class
			.getCanonicalName();

	private ImageView photoimage;

	private ImageButton bTn_picture_transfer;
	private ImageButton bTn_picture_cancel;

	private String takephotoUri;
	private String usernum;
	public static Bitmap showBitmap;
	public static boolean canTransfer = false;

	public final INgnHistoryService mHistorytService;

	public ScreenTakePhotoIstransfer() {
		super(SCREEN_TYPE.CHAT_T, TAG);

		mHistorytService = getEngine().getHistoryService();
	}

	private long findMaxHistoryMsgTime() {
		long time = 0;
		try {
			if (mHistorytService != null
					&& mHistorytService.getEvents() != null
					&& mHistorytService.getEvents().size() > 0) {
				for (NgnHistoryEvent e1 : mHistorytService.getEvents()) {
					if (e1.getStartTime() > time
							&& !e1.getRemoteParty().equals("1000")) {
						time = e1.getStartTime();
					}
				}
			}
		} catch (Exception e) {
			MyLog.d(TAG, "findMaxHistoryMsgTime:" + e.getMessage());
		}
		return ++time;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyLog.d(TAG, "onCreate");
		// canTransfer=false;

		setContentView(R.layout.screen_takephoto_istransfer);
		Intent intent = this.getIntent();
		if (intent != null) {
			Bundle extras = intent.getExtras();
			takephotoUri = (String) extras.get("takephotoUri");
			usernum = (String) extras.get("usernum");
			// showBitmap = intent.getParcelableExtra("bitmap");
		}
		photoimage = (ImageView) findViewById(R.id.screen_chat_takephoto);

		MyLog.d(TAG, takephotoUri);
		MyLog.d(TAG, usernum);
		//
		// BitmapFactory.Options options = new BitmapFactory.Options();
		// options.inSampleSize = 1;
		// Bitmap bmpBitmap = BitmapFactory.decodeFile(takephotoUri, options);

		photoimage.setImageBitmap(showBitmap);

		bTn_picture_transfer = (ImageButton) findViewById(R.id.screen_chat_takephoto_transfer);
		bTn_picture_cancel = (ImageButton) findViewById(R.id.screen_chat_takephoto_cancel);

		// 设置背景颜色为透明
		bTn_picture_cancel.getBackground().setAlpha(0);
		bTn_picture_transfer.getBackground().setAlpha(0);

		bTn_picture_transfer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (canTransfer) {
					MyLog.e(TAG, "可以发送 " + usernum);

					if (!NgnStringUtils.isNullOrEmpty(usernum)) {
						String[] idArray = usernum.split(";");
						String idStartChat = "";
						// final String content =
						// mEtCompose.getText().toString();
						for (int i = 0; i < idArray.length; i++) {
							if (idArray[i] != null && !idArray[i].isEmpty()) {
								ModelContact info = SystemVarTools
										.createContactFromPhoneNumber(idArray[i]);
								if (info.name == null
										&& ScreenNewSMS
												.isContainCharacter(info.mobileNo)) {
									SystemVarTools.showToast(String
											.format(NgnApplication
													.getContext()
													.getString(
															R.string.your_input)
													+ "\"%s\""
													+ NgnApplication
															.getContext()
															.getString(
																	R.string.error_mark),
													info.mobileNo));
									continue;
								}
								if (takephotoUri == null
										|| takephotoUri.isEmpty()) {
									return;
								}

								new ScreenChat().sendtakephoto(takephotoUri,
										usernum);
								if (ScreenChat.mAdapter != null) {
									ScreenChat.mAdapter.refresh();
								}

								if (idStartChat.isEmpty()) {
									idStartChat = info.mobileNo;
								}
							}
						}
						if (!idStartChat.isEmpty()) {
							ScreenChat.startChat(idStartChat, true);
						}
					}

					finish();

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
				intent.putExtra("usernum", usernum);
				intent.setClass(ScreenTakePhotoIstransfer.this,
						Screen_takephoto_camera.class);
				startActivity(intent);
				finish();
			}
		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MyLog.d(TAG, "onDestroy");
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
