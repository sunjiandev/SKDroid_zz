package com.sunkaisens.skdroid.Screens;

//add by jgc 2014.11.27
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.doubango.utils.MyLog;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Utils.SystemVarTools;

public class Screen_takephoto_camera_myicon extends Activity implements
		PictureCallback

{
	private CameraSurfacePreview mCameraSurfacePreview = null;
	private ImageButton mCaptureButton = null;
	private String TAG = Screen_takephoto_camera_myicon.class
			.getCanonicalName();
	private File takephoto_tempfile = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_takephoto);

		MyLog.d(TAG, "onCreate");

		// Create our Preview View and set it as the content of our activity
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

		mCameraSurfacePreview = new CameraSurfacePreview(this);

		preview.addView(mCameraSurfacePreview);

		mCaptureButton = (ImageButton) findViewById(R.id.screen_camera_button_capture);

		mCaptureButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				MyLog.e(TAG, "mCaptureButton is clicked");

				mCaptureButton.setEnabled(false);
				mCaptureButton.setImageDrawable(getResources().getDrawable(
						R.drawable.takephoto2));
				mCameraSurfacePreview
						.takePicture(Screen_takephoto_camera_myicon.this); // 拍攝照片

			}
		});

	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {

		// save the picture to scard
		if (data != null && data.length != 0) {
			try {

				Bitmap bmpBitmap = BitmapFactory.decodeByteArray(data, 0,
						data.length);

				// 将图片旋转90度。
				Matrix matrix = new Matrix();

				Configuration mConfiguration = this.getResources()
						.getConfiguration(); // 获取设置的配置信息
				int ori = mConfiguration.orientation; // 获取屏幕方向

				MyLog.d("SurfaceCreated 屏幕方向", "" + ori);

				if (ori == mConfiguration.ORIENTATION_LANDSCAPE)
					matrix.preRotate(270);
				if (ori == mConfiguration.ORIENTATION_PORTRAIT)
					matrix.preRotate(0);

				matrix.preRotate(90);

				final Bitmap newbmpBitmap0 = Bitmap.createBitmap(bmpBitmap, 0,
						0, bmpBitmap.getWidth(), bmpBitmap.getHeight(), matrix,
						true);

				Matrix matrix2 = new Matrix();
				float scalewidth = ((float) 400)
						/ ((float) newbmpBitmap0.getWidth());
				float scaleheight = ((float) 400)
						/ ((float) newbmpBitmap0.getHeight());

				matrix2.postScale(scalewidth, scaleheight);

				final Bitmap newbmpBitmap = Bitmap.createBitmap(newbmpBitmap0,
						0, 0, newbmpBitmap0.getWidth(),
						newbmpBitmap0.getHeight(), matrix2, true);

				// 异步将图片存进SDCard，减少拍完照跳转到是否发送界面时间
				Thread saveToSDCardThread = new Thread(new Runnable() {

					@Override
					public void run() {
						FileOutputStream newfos = null;
						try {

							File newpictureFlie = getOutputMediaFile();

							newfos = new FileOutputStream(newpictureFlie);
							newbmpBitmap.compress(CompressFormat.JPEG, 100,
									newfos); // 用60不用100，降低存储照片的大小

							ScreenTakePhotoIstransfer_myicon.canTransfer = true;
							MyLog.e("", "saveToSDCardThread end");

						} catch (Exception e) {
							MyLog.e(TAG, "save picture to sdcard ERROR");
						} finally {
							if (newfos != null) {
								try {
									newfos.close();
								} catch (Exception e2) {
									e2.printStackTrace();
								}
							}
						}
					}
				});
				saveToSDCardThread.start();

				// 进入是否传送照片界面
				MyLog.e("Screen_takephoto_myicon",
						"save photo and destory surfacepreview");

				mCameraSurfacePreview.surfaceDestroyed(mCameraSurfacePreview
						.getHolder());

				if (newbmpBitmap != null) {
					MyLog.d("Screen_takephoto_myicon",
							SystemVarTools.downloadPath);

					Intent intent = Screen_takephoto_camera_myicon.this
							.getIntent();
					Bundle extras = intent.getExtras();

					Intent intentbitmap = new Intent();
					intentbitmap.putExtra("takephotoUri",
							takephoto_tempfile.getPath());

					// intentbitmap.putExtra("bitmap", newbmpBitmap);

					ScreenTakePhotoIstransfer_myicon.showBitmap = newbmpBitmap;
					intentbitmap.setClass(Screen_takephoto_camera_myicon.this,
							ScreenTakePhotoIstransfer_myicon.class);

					startActivity(intentbitmap);

				} else {
					SystemVarTools.showToast("提示：\n照片不存在，请重新拍照。", false);
				}
				finish();

			} catch (Exception e) {
				MyLog.e(TAG, "ERROR");

			}

		}

	}

	private File getOutputMediaFile() {

		String saveDir = SystemVarTools.downloadIconPath;
		File dir = new File(saveDir);
		if (!dir.exists()) {
			dir.mkdir();
		}
		String fileName = "myicon.jpg";
		takephoto_tempfile = new File(saveDir, fileName);
		takephoto_tempfile.delete();
		if (!takephoto_tempfile.exists()) {
			try {
				takephoto_tempfile.createNewFile();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return takephoto_tempfile;

	}

}
