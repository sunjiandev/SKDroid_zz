package com.sunkaisens.skdroid.Screens;

//add by jgc 2014.11.27

import java.util.List;

import org.doubango.ngn.NgnApplication;
import org.doubango.utils.MyLog;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Utils.SystemVarTools;

public class CameraSurfacePreview extends SurfaceView implements
		SurfaceHolder.Callback {
	private SurfaceHolder mHolder;
	private Camera mCamera;

	public static String TAG = CameraSurfacePreview.class.getCanonicalName();

	public CameraSurfacePreview(Context context) {
		super(context);

		mHolder = getHolder();
		mHolder.addCallback(this);
		// deprecated setting ,but required on Android version prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		MyLog.d(TAG, "surfaceCreated() is called");

		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
			MyLog.d(TAG, "Before start Camera,mCamera is not null");
		}

		try {
			MyLog.d(TAG, "start Camera");

			mCamera = Camera.open();
			mCamera.setPreviewDisplay(holder);

			Configuration mConfiguration = this.getResources()
					.getConfiguration(); // 获取设置的配置信息
			int ori = mConfiguration.orientation; // 获取屏幕方向

			MyLog.d(TAG, "SurfaceCreated 屏幕方向: " + ori);

			if (ori == mConfiguration.ORIENTATION_LANDSCAPE)
				mCamera.setDisplayOrientation(0);
			if (ori == mConfiguration.ORIENTATION_PORTRAIT)
				mCamera.setDisplayOrientation(90);

			// Camera增加自动对焦功能
			Camera.Parameters parameters = mCamera.getParameters();
			boolean focusModeSupported = false;
			List<String> mFocusModes = parameters.getSupportedFocusModes();

			if (mFocusModes
					.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {

				MyLog.d(TAG, "支持自动对焦");

				focusModeSupported = true;
				parameters
						.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

			} else {
				MyLog.d(TAG, "不支持自动对焦");

				focusModeSupported = false;
			}

			List<Size> previewSizes = parameters.getSupportedPreviewSizes();

			if (previewSizes != null) {
				if (NgnApplication.isl8848a_l1860()) { // 为大唐Pad进行适配

					MyLog.d(TAG, "setPreviewSize,我是大唐Pad");

					parameters.setPreviewSize(previewSizes.get(1).width,
							previewSizes.get(1).height);

				} else {
					// 其余的使用默认的预览尺寸
				}
			}

			// 设置拍照的清晰度 注：不是预览的
			// CamcorderProfile profile = CamcorderProfile
			// .get(CamcorderProfile.QUALITY_480P);
			Size mSize = getCameraBestPictureSize(mCamera);

			if (mSize != null) {
				// Log.e("", "拍照尺寸   ：  "+mSize.width+"   "+mSize.height);
				parameters.setPictureSize(mSize.width, mSize.height);
			} else {
				parameters.setPictureSize(480, 720);
			}

			mCamera.setParameters(parameters);
			if (focusModeSupported) {
				mCamera.cancelAutoFocus();
			}
			mCamera.startPreview();

		} catch (Exception e) {
			MyLog.d(TAG, "Error setting camera preview:" + e.getMessage());

			if (mCamera != null) {
				mCamera.release();
				mCamera = null;
			}

			SystemVarTools.showToast(NgnApplication.getContext().getString(
					R.string.camera_open_failed));
		}

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		MyLog.d(TAG, "surfaceChanged() is called");

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
			MyLog.d(TAG, "摄像头释放！");

		}
	}

	public void takePicture(PictureCallback imageCallback) {
		if (mCamera != null) {
			mCamera.takePicture(null, null, imageCallback);
			MyLog.d(TAG, "takePicture():mCamera is not null");
		} else {
			MyLog.d(TAG, "takePicture():mCamera is  null");

		}
	}

	private Size getCameraBestPictureSize(Camera camera) {

		// 获取摄像头最接近640*480的尺寸
		int mWidth = 480;
		int mHeight = 720;

		final List<Size> prevSizes = camera.getParameters()
				.getSupportedPictureSizes();

		Size minSize = null;
		int minScore = Integer.MAX_VALUE;
		for (Size size : prevSizes) {
			final int score = Math.abs(size.width - mWidth)
					+ Math.abs(size.height - mHeight);
			if (minScore > score) {
				minScore = score;
				minSize = size;
			}
		}
		return minSize;
	}

}
