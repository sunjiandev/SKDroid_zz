package com.sunkaisens.skdroid.Screens;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.utils.MyLog;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Utils.RoundProgressBar;
import com.sunkaisens.skdroid.Utils.SystemVarTools;

public class Screen_VideoRecorder extends Activity implements
		SurfaceHolder.Callback {

	private String TAG = Screen_VideoRecorder.class.getCanonicalName();

	private ImageButton record;
	private File videofile;
	private MediaRecorder mRecorder;
	private SurfaceView sView;

	// private FrameLayout sViewFrameLayout;
	// private CameraSurfacePreview mCameraSurfacePreview = null;
	private Camera mCamera;
	private MediaPlayer mPlayer = null;

	private boolean isCancel = false;

	private TextView toast_text;

	private float weizhix;
	private float weizhiy;

	private boolean isrecorder = false;

	private GestureDetector mGestureDetector;
	private NgnTimer timer = new NgnTimer();
	// private NgnTimer timer2 = new NgnTimer();

	public static final int BEGIN_RECORDER = 1110;
	public static final int CANCEL = 1111;
	public static final int LONG_PRESS = 1112;
	public static final int RECORDER = 1113;
	public static final int GONE_TOAST_TEXT = 1114;

	private TextView mTimerTextView;
	private RoundProgressBar roundProgressBar, roundProgressBar2;

	public static List<Size> previewSizes;

	private CountDownTimer mTimer = new CountDownTimer(15000, 1) { // ����ʱ����Ϊ15��

		@Override
		public void onTick(long millisUntilFinished) {
			
			mTimerTextView.setText("" + millisUntilFinished / 1000);
			int a = (int) millisUntilFinished;
			roundProgressBar.setProgress(a);
		}

		@Override
		public void onFinish() {
			// mTimerTextView.setText("20");
			MyLog.d(TAG, "mTimer:15�뵽��");
			mTimerTextView.setText("0");
			roundProgressBar.setProgress(15000);
			// 15��֮���������¼����ֱ�ӷ���
			if (isrecorder) {
				if (!isCancel) {
					record.setEnabled(false);
					// SystemClock.sleep(800);
					// //startMeadiaRecorder��stopMeadiaRecorderҪ���1������
					MyLog.d(TAG, "mTimer:15�뵽�ˣ��ͷ�MediaRecorder������ͷ");
					releaseMediaRecorder();
					releaseCamera();

					MyLog.d(TAG, "mTimer:15�뵽��,�ͷ�MediaRecorder������ͷ��ϣ����ͣ�");
					Intent intent = Screen_VideoRecorder.this.getIntent();
					Bundle extras = intent.getExtras();
					String mobileNo = (String) extras.get("usernum");

					new ScreenChat().sendIMvideo(videofile.getPath(), mobileNo);
					finish();

				}
			}

		}
	};
	
	private Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case BEGIN_RECORDER:
					toast_text.setText(NgnApplication.getContext().getString(
							R.string.recording));
					record.setBackgroundResource(R.drawable.takephoto2);
					roundProgressBar2.setVisibility(View.GONE);
					roundProgressBar.setVisibility(View.VISIBLE);
					break;
				case CANCEL:
					toast_text.setText(NgnApplication.getContext().getString(
							R.string.has_canceled));
					record.setBackgroundResource(R.drawable.takephoto);
					mTimerTextView.setText("15");
					roundProgressBar.setProgress(0);
					roundProgressBar.setVisibility(View.GONE);
					roundProgressBar2.setVisibility(View.VISIBLE);
					break;
				case LONG_PRESS:
					toast_text.setText(NgnApplication.getContext().getString(
							R.string.long_pressed_and_cancel));
					record.setBackgroundResource(R.drawable.takephoto);
					roundProgressBar.setVisibility(View.GONE);
					roundProgressBar2.setVisibility(View.VISIBLE);
					break;
				case RECORDER:
					toast_text.setText(NgnApplication.getContext().getString(
							R.string.rerecord));
					record.setEnabled(true);
					break;
				case GONE_TOAST_TEXT:
					toast_text.setVisibility(View.GONE);
					break;
				default:
					break;
				}
			} catch (Exception e) {
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_chat_videorecorder);

		MyLog.d(TAG, "oncreat");

		record = (ImageButton) findViewById(R.id.screen_chat_video_record);

		toast_text = (TextView) findViewById(R.id.toast_text);

		sView = (SurfaceView) findViewById(R.id.screen_chat_video_sView);

		// ����sView����Ҫ�Լ�ά��������
		sView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		// ���÷ֱ���
		// sView.getHolder().setFixedSize(320, 320);

		// ������Ļ�����Զ��ر�
		// sView.getHolder().setKeepScreenOn(true);

		// CamcorderProfile
		// profile=CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);

		// sView.getHolder().setFixedSize(profile.videoFrameWidth,
		// profile.videoFrameHeight);

		sView.getHolder().addCallback(this);
		MyLog.d(TAG, "sView:" + sView.getWidth() + "&&&&" + sView.getHeight());

		toast_text.setText(NgnApplication.getContext().getString(
				R.string.long_pressed_and_cancel));

		mTimerTextView = (TextView) findViewById(R.id.screen_chat_video_time);
		roundProgressBar = (RoundProgressBar) findViewById(R.id.round_ProgressBar);
		roundProgressBar2 = (RoundProgressBar) findViewById(R.id.round_ProgressBar2);

		mGestureDetector = new GestureDetector(this,
				new SimpleOnGestureListener() {

					@Override
					public boolean onSingleTapUp(MotionEvent e) {
						weizhix = e.getX();
						weizhiy = e.getY();

						return super.onSingleTapUp(e);

					}

					@Override
					public void onLongPress(MotionEvent e) {
						super.onLongPress(e);
						weizhix = e.getX();
						weizhiy = e.getY();

					}

					@Override
					public boolean onScroll(MotionEvent e1, MotionEvent e2,
							float distanceX, float distanceY) {

						return super.onScroll(e1, e2, distanceX, distanceY);
					}

					@Override
					public boolean onFling(MotionEvent e1, MotionEvent e2,
							float velocityX, float velocityY) {

						return super.onFling(e1, e2, velocityX, velocityY);

					}

					@Override
					public void onShowPress(MotionEvent e) {
						super.onShowPress(e);
						weizhix = e.getX();
						weizhiy = e.getY();

					}

					@Override
					public boolean onDown(MotionEvent e) {
						weizhix = e.getX();
						weizhiy = e.getY();

						return super.onDown(e);
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						weizhix = e.getX();
						weizhiy = e.getY();

						return super.onDoubleTap(e);
					}

					@Override
					public boolean onDoubleTapEvent(MotionEvent e) {
						weizhix = e.getX();
						weizhiy = e.getY();

						return super.onDoubleTapEvent(e);
					}

					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						weizhix = e.getX();
						weizhiy = e.getY();

						return super.onSingleTapConfirmed(e);
					}

				});

		record.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mGestureDetector.onTouchEvent(event);

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (!isrecorder) {
						MyLog.d(TAG, "record onClick,������ʱ��");
						timer = new NgnTimer();
						timer.schedule(new TimerTask() {

							@Override
							public void run() {
								isrecorder = true;
								MyLog.d(TAG, "record onClick,500����󣬿�ʼ¼��");
								setFile();
								setMediaRecorderAndStart();
								Message msg = Message.obtain(myHandler,
										BEGIN_RECORDER);
								myHandler.sendMessage(msg);

							}
						}, 500);
					} else {
						MyLog.d(TAG, "record onClick,δ������ʱ��");
					}

					break;

				case MotionEvent.ACTION_UP:
					if (isrecorder) {
						if (!isCancel) {
							record.setEnabled(false);
							SystemClock.sleep(800); // startMeadiaRecorder��stopMeadiaRecorderҪ���1������
							MyLog.d(TAG,
									"record onClick,̧���ͷ�MediaRecorder������ͷ");
							releaseMediaRecorder();
							releaseCamera();

							MyLog.d(TAG,
									"record onClick,�ͷ�MediaRecorder������ͷ��ϣ����ͣ�");
							Intent intent = Screen_VideoRecorder.this
									.getIntent();
							Bundle extras = intent.getExtras();
							String mobileNo = (String) extras.get("usernum");

							new ScreenChat().sendIMvideo(videofile.getPath(),
									mobileNo);
							finish();

						} else {
							record.setEnabled(false);
							SystemClock.sleep(1000);
							MyLog.d(TAG, "record onClick,̧���ͷ�MediaRecorder");
							releaseMediaRecorder();
							Message msg = Message.obtain(myHandler, CANCEL);
							myHandler.sendMessage(msg);
							isrecorder = false;
							record.setEnabled(true);
						}
					} else {
						MyLog.d(TAG, "record onClick,����500����̧��Timer cancer");
						timer.cancel();
						Message msg = Message.obtain(myHandler, LONG_PRESS);
						myHandler.sendMessage(msg);
					}
					isCancel = true;
					break;

				default:
					break;

				}

				if (((Math.abs(event.getY() - weizhiy)) > 30)
						|| ((Math.abs(event.getX() - weizhix)) > 30)) {
					if (isrecorder) {
						MyLog.d(TAG, "record onClick,����ȡ��������preview");
						isCancel = true;
					} else {
						MyLog.d(TAG, "record onClick,�����������30");
						isCancel = false;
					}
				} else {
					isCancel = false;
				}
				return true;
			}
		});

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Message msg = myHandler.obtainMessage(GONE_TOAST_TEXT);
		myHandler.sendMessageDelayed(msg, 3000L);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MyLog.d(TAG, "onDestroy()");
		releaseMediaRecorder();
		releaseCamera();

	}

	@Override
	protected void onPause() {
		super.onPause();
		MyLog.d(TAG, "onPause()");
		releaseMediaRecorder();
		releaseCamera();
	}

	// private void setCamera() {
	// if (mCamera == null) {
	// try {
	// // mCamera = Camera.open();
	// mCamera.setPreviewDisplay(sView.getHolder());
	//
	// Configuration mConfiguration = getApplicationContext()
	// .getResources().getConfiguration(); // ��ȡ���õ�������Ϣ
	// int ori = mConfiguration.orientation; // ��ȡ��Ļ����
	//
	// if (ori == mConfiguration.ORIENTATION_LANDSCAPE)
	// mCamera.setDisplayOrientation(0);
	// if (ori == mConfiguration.ORIENTATION_PORTRAIT)
	// mCamera.setDisplayOrientation(90);
	// } catch (Exception e) {
	// // TODO: handle exception
	// MyLog.d(TAG, "" + e.getMessage());
	// }
	// }
	// }

	private void setFile() {
		try {
			// ��������¼����Ƶ����Ƶ�ļ�
			String saveDir = SystemVarTools.downloadPath;
			File dir = new File(saveDir);
			if (!dir.exists()) {
				dir.mkdir();
			} // ������ʱ��������Ƭ
			String fileName = "";
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyyMMdd_HHmmss'_VideoRecorder'");
			fileName = dateFormat.format(date) + ".mp4";
			videofile = new File(saveDir, fileName);
			videofile.delete();
			if (!videofile.exists()) {
				try {
					videofile.createNewFile();
					// MyLog.d(TAG, "video file :" + videofile.getPath());
				} catch (IOException e3) {
					MyLog.d(TAG, "" + e3.getMessage());
				}
			}
		} catch (Exception e) {
			MyLog.d(TAG, "" + e.getMessage());

		}
	}

	private void setMediaRecorderAndStart() {

		MyLog.d(TAG, "setMediaRecorderAndStart(),begin to set MediaRecorder");

		// ����MediaRecorder����
		mRecorder = new MediaRecorder();

		if (mCamera != null) {
			try {
				mCamera.unlock();
			} catch (IllegalStateException e) {
				MyLog.d(TAG, "" + e.getMessage());
			} catch (RuntimeException e) {
				MyLog.d(TAG, "" + e.getMessage());
			} catch (Exception e) {
				MyLog.d(TAG, "" + e.getMessage());
			}

			mRecorder.setCamera(mCamera);

			mRecorder.reset();

			// ���ô���˷�ɼ�����
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

			// ���ô�����ͷ�ɼ�ͼ��
			mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

			// ������Ƶ�ļ��������ʽ���������������������ʽ��ͼ������ʽ֮ǰ���ã�
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			// �������������ʽ
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			// mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

			// ����ͼ������ʽ

			if (NgnApplication.isBh03()) { // �ֳ�̨
				mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263); // ���人Ѹ�ֳ�
			} else { // Pad
				mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
			}

			CamcorderProfile profile = CamcorderProfile
					.get(CamcorderProfile.QUALITY_HIGH);

			if (previewSizes != null) {
				if (NgnApplication.isl8848a_l1860()) { // ����Pad��������
					MyLog.d(TAG, "setMediaRecorderAndStart(),���Ǵ���Pad");
					mRecorder.setVideoSize(previewSizes.get(1).width,
							previewSizes.get(1).height);
				} else {
					mRecorder.setVideoSize(profile.videoFrameWidth,
							profile.videoFrameHeight);
				}
			}

			// mRecorder.setProfile(profile);
			// ÿ֡4��
			mRecorder.setVideoFrameRate(30);
			mRecorder.setVideoEncodingBitRate(8 * 1024 * 1024);

			mRecorder.setOutputFile(videofile.getAbsolutePath());

			// ָ��ʹ��SurfaceView��Ԥ����Ƶ
			mRecorder.setPreviewDisplay(sView.getHolder().getSurface());

			Configuration mConfiguration = getApplicationContext()
					.getResources().getConfiguration(); // ��ȡ���õ�������Ϣ
			int ori = mConfiguration.orientation; // ��ȡ��Ļ����

			if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
				mRecorder.setOrientationHint(0);
				// mCamera.setDisplayOrientation(0);
			}
			if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
				mRecorder.setOrientationHint(90);
				// mCamera.setDisplayOrientation(90);
			}
			// mCamera.setDisplayOrientation(90);

			mTimer.start();

			// ��ʼ¼��
			try {
				mRecorder.prepare();
				mRecorder.start();
			} catch (IllegalStateException e) {
				MyLog.d(TAG, " start recorder: " + e.getMessage());
			} catch (IOException e) {
				MyLog.d(TAG, " start recorder: " + e.getMessage());
			} catch (RuntimeException e) {
				MyLog.d(TAG, " start recorder: " + e.getMessage());
			}

		}

	}

	private void releaseMediaRecorder() {

		if (mRecorder != null) {

			mRecorder.setOnErrorListener(null);
			mRecorder.setPreviewDisplay(null);

			// mTimer.onFinish();
			mTimer.cancel();
			try {
				mRecorder.stop();
			} catch (IllegalStateException e) {
				MyLog.d(TAG, " stop recorder: " + e.getMessage());
			} catch (RuntimeException e) {
				MyLog.d(TAG, " stop recorder: " + e.getMessage());
			} catch (Exception e) {
				MyLog.d(TAG, " stop recorder: " + e.getMessage());
			}

			mRecorder.reset();
			mRecorder.release();
			mRecorder = null;

			try {
				mCamera.lock();
			} catch (RuntimeException e) {
				MyLog.d(TAG, "" + e.getMessage());
			}

		}
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;

		}
	}

	private void openCamera(SurfaceHolder holder) {

		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
			MyLog.d(TAG, "openCamera(),Before start Camera,mCamera is not null");
		}

		try {
			MyLog.d(TAG, "openCamera(),start Camera");
			mCamera = Camera.open();

			mCamera.setPreviewDisplay(holder);

			Configuration mConfiguration = this.getResources()
					.getConfiguration(); // ��ȡ���õ�������Ϣ
			int ori = mConfiguration.orientation; // ��ȡ��Ļ����

			MyLog.d(TAG, "openCamera(),��Ļ���� " + ori);

			if (ori == mConfiguration.ORIENTATION_LANDSCAPE)
				mCamera.setDisplayOrientation(0);
			if (ori == mConfiguration.ORIENTATION_PORTRAIT)
				mCamera.setDisplayOrientation(90);

			// Camera�Զ��Խ�
			Camera.Parameters parameters = mCamera.getParameters();
			List<String> mFocusModes = parameters.getSupportedFocusModes();
			boolean focusModeSupported = false;
			if (mFocusModes
					.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {

				MyLog.d(TAG, "openCamera(),֧���Զ��Խ�");
				focusModeSupported = true;
				parameters
						.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

			} else {
				MyLog.d(TAG, "openCamera(),��֧���Զ��Խ�");
				focusModeSupported = false;

			}

			previewSizes = parameters.getSupportedPreviewSizes();

			if (previewSizes != null) {

				if (NgnApplication.isl8848a_l1860()) {
					MyLog.d(TAG, "openCamera(),���Ǵ���Pad"); // ����Pad��������

					parameters.setPreviewSize(previewSizes.get(1).width,
							previewSizes.get(1).height);
				} else {

					// parameters.setPreviewSize(previewSizes.get(1).width,
					// previewSizes.get(1).height);
				}

			}

			mCamera.setParameters(parameters);
			if (focusModeSupported) {
				mCamera.cancelAutoFocus();
			}

			mCamera.startPreview();

		} catch (Exception e) {
			MyLog.d(TAG, "Error setting camera :" + e.getMessage());
			if (mCamera != null) {
				mCamera.release();
				mCamera = null;
			}

			record.setEnabled(false);

			SystemVarTools.showToast(NgnApplication.getContext().getString(
					R.string.camera_open_failed));

		}

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		MyLog.d(TAG, "surfaceCreated() is called");
		openCamera(holder);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		MyLog.e(TAG, "surfaceChanged()");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		releaseCamera();
	}

}
