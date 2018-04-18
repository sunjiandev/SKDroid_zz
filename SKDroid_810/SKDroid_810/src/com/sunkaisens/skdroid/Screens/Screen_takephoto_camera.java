package com.sunkaisens.skdroid.Screens;

//add by jgc 2014.11.27
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.doubango.utils.MyLog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Utils.SystemVarTools;

public class Screen_takephoto_camera extends Activity implements
		PictureCallback {
	private CameraSurfacePreview mCameraSurfacePreview = null;
	private ImageButton mCaptureButton = null;
	private String TAG = Screen_takephoto_camera.class.getCanonicalName();
	private File takephoto_tempfile = null;
	private GestureDetector mGestureDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_takephoto);

		MyLog.d(TAG, "onCreate");

		// Create our Preview View and set it as the content of our activity
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

		mCameraSurfacePreview = new CameraSurfacePreview(this);

		preview.addView(mCameraSurfacePreview);

		// ��Ļ�ϻ�ȡ������
		/*
		 * mGestureDetector = new GestureDetector(this, new
		 * MyGestureListener(this)); preview.setOnTouchListener(new
		 * OnTouchListener() {
		 * 
		 * @Override public boolean onTouch(View v, MotionEvent event) { // TODO
		 * Auto-generated method stub return
		 * mGestureDetector.onTouchEvent(event); // return false; } });
		 * 
		 * // �ؼ����룬View���л�����ʰʶ��������� preview.setLongClickable(true);
		 */
		// Add a listener to the Capture button
		mCaptureButton = (ImageButton) findViewById(R.id.screen_camera_button_capture);

		mCaptureButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MyLog.e(TAG, "mCaptureButton is clicked");

				mCaptureButton.setEnabled(false);
				mCaptureButton.setImageDrawable(getResources().getDrawable(
						R.drawable.takephoto2));
				mCameraSurfacePreview.takePicture(Screen_takephoto_camera.this); // �Ĕz��Ƭ

			}
		});

	}

	public class MyGestureListener extends SimpleOnGestureListener {

		private Context mContext;

		MyGestureListener(Context context) {
			mContext = context;
		}

		@Override
		// ���´���������ʱ���̴���
		public boolean onDown(MotionEvent e) {

			MyLog.e(TAG, "onDown");
			return false;
		}

		// �̰�������������Ƭ�̺�̧�𣬻ᴥ��������ƣ����Ѹ��̧���򲻻�
		@Override
		public void onShowPress(MotionEvent e) {

			MyLog.e(TAG, "onShowPress");

		}

		// �ͷţ���ָ�뿪������ʱ����(����������������ʱ�����ᴥ���������)
		@Override
		public boolean onSingleTapUp(MotionEvent e) {

			MyLog.e(TAG, "onSingleTapUp");
			return false;
		}

		// ���������º󻬶�
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			MyLog.e(TAG, "onScroll");
			return false;
		}

		// ���������������º�Ȳ�̧��Ҳ���ƶ�����һ��ʱ��󴥷�
		@Override
		public void onLongPress(MotionEvent e) {
			MyLog.e(TAG, "onLongPress");

		}

		// ���������������º�����ƶ���̧�𣬻��ȴ����������ƣ����Ŵ���һ����������
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {

			MyLog.e(TAG, "onFling");

			int dx = (int) (e2.getY() - e1.getY());
			if (Math.abs(dx) > 50) {

				mCameraSurfacePreview.surfaceDestroyed(mCameraSurfacePreview
						.getHolder());
				finish();

			}

			return false;
		}

		// ˫������ָ�ڴ�������Ѹ�ٵ���ڶ���ʱ����
		@Override
		public boolean onDoubleTap(MotionEvent e) {

			MyLog.e(TAG, "onDoubleTap");

			return false;
		}

		// ˫�����¸�̧�������һ��
		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {

			MyLog.e(TAG, "onDoubleTapEvent");
			return false;
		}

		// ����
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {

			MyLog.e(TAG, "onSingleTapConfirmed");

			return false;
		}
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {

		// save the picture to scard
		if (data != null && data.length != 0) {
			try {

				Bitmap bmpBitmap = BitmapFactory.decodeByteArray(data, 0,
						data.length);

				// ��ͼƬ��ת90�ȡ�
				Matrix matrix = new Matrix();

				Configuration mConfiguration = this.getResources()
						.getConfiguration(); // ��ȡ���õ�������Ϣ
				int ori = mConfiguration.orientation; // ��ȡ��Ļ����

				MyLog.d(TAG, "SurfaceCreated ��Ļ����: " + ori);

				if (ori == mConfiguration.ORIENTATION_LANDSCAPE)
					matrix.preRotate(270);
				if (ori == mConfiguration.ORIENTATION_PORTRAIT)
					matrix.preRotate(0);

				matrix.preRotate(90);

				final Bitmap newbmpBitmap = Bitmap.createBitmap(bmpBitmap, 0,
						0, bmpBitmap.getWidth(), bmpBitmap.getHeight(), matrix,
						true);

				// �첽��ͼƬ���SDCard��������������ת���Ƿ��ͽ���ʱ��
				Thread saveToSDCardThread = new Thread(new Runnable() {

					@Override
					public void run() {
						FileOutputStream newfos = null;
						try {

							File newpictureFlie = getOutputMediaFile();

							newfos = new FileOutputStream(newpictureFlie);
							newbmpBitmap.compress(CompressFormat.JPEG, 60,
									newfos); // ��60����100�����ʹ洢��Ƭ�Ĵ�С

							ScreenTakePhotoIstransfer.canTransfer = true;
							MyLog.d(TAG, "saveToSDCardThread end");

						} catch (Exception e) {
							MyLog.d(TAG, "save picture to sdcard ERROR");
						} finally {
							if (newfos != null) {
								try {
									newfos.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				});
				saveToSDCardThread.start();

				// �����Ƿ�����Ƭ����
				MyLog.d(TAG, "save photo and destory surfacepreview");

				mCameraSurfacePreview.surfaceDestroyed(mCameraSurfacePreview
						.getHolder());

				if (newbmpBitmap != null) {
					MyLog.d(TAG, "����ScreenTakePhotoIstransfer����");

					Intent intent = Screen_takephoto_camera.this.getIntent();
					Bundle extras = intent.getExtras();
					String mobileNo = (String) extras.get("usernum");
					MyLog.d(TAG, "usernum:" + mobileNo);
					Intent intentbitmap = new Intent();
					intentbitmap.putExtra("takephotoUri",
							takephoto_tempfile.getPath());
					// MyLog.d(TAG, "takephotoUri:" +
					// takephoto_tempfile.getPath());
					intentbitmap.putExtra("usernum", mobileNo);

					ScreenTakePhotoIstransfer.showBitmap = newbmpBitmap;
					intentbitmap.setClass(Screen_takephoto_camera.this,
							ScreenTakePhotoIstransfer.class);

					startActivity(intentbitmap);

				} else {
					SystemVarTools.showToast("��ʾ��\n��Ƭ�����ڣ����������ա�", false);
				}
				finish();

			} catch (Exception e) {
				MyLog.d(TAG, "onPictureTaken ERROR");

			}

		}

	}

	private File getOutputMediaFile() {

		String saveDir = SystemVarTools.downloadPath;
		File dir = new File(saveDir);
		if (!dir.exists()) {
			dir.mkdir();
		} // ������ʱ��������Ƭ
		String fileName = "";
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"'TakephotoIMG_'yyyyMMdd_HHmmss");
		fileName = dateFormat.format(date) + ".jpg";
		takephoto_tempfile = new File(saveDir, fileName);
		takephoto_tempfile.delete();
		if (!takephoto_tempfile.exists()) {
			try {
				takephoto_tempfile.createNewFile();

				// MyLog.d(TAG,
				// "takephoto_tempfile:" + takephoto_tempfile.getPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return takephoto_tempfile;

	}

}
