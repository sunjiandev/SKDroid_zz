package com.sunkaisens.skdroid.app.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class SensorService extends Service {

	private SensorManager mManager;

	private Sensor mSensor = null;

	private SensorEventListener mListener = null;

	private PowerManager localPowerManager = null;
	private PowerManager.WakeLock localWakeLock = null;

	@Override
	public void onCreate() {

		// 获取系统服务POWER_SERVICE，返回一个PowerManager对象
		localPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
		localWakeLock = this.localPowerManager.newWakeLock(32, "MyPower");

		// 获取系统服务SENSOR_SERVICE，返回一个SensorManager对象
		mManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		// 获取距离感应器对象
		mSensor = mManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		// 注册感应器事件
		mListener = new SensorEventListener() {

			@Override
			public void onSensorChanged(SensorEvent event) {

				float[] its = event.values;

				if (its != null && event.sensor.getType() == Sensor.TYPE_PROXIMITY) {

					Log.d("","its[0]:" + its[0]);

					// 经过测试，当手贴近距离感应器的时候its[0]返回值为0.0，当手离开时返回1.0
					if (its[0] == 0.0) {// 贴近手机

						Log.d("sensor","手放上去了...");

						if (localWakeLock.isHeld()) {
							Log.d("sersor","return");
							return;
						} else
							localWakeLock.acquire();// 申请设备电源锁

					} else {// 远离手机

						Log.d("sersor","手拿开了...");

						if (localWakeLock.isHeld()) {
							Log.d("sersor","return");
							return;
						} else
							localWakeLock.setReferenceCounted(false);
						localWakeLock.release(); // 释放设备电源锁
					}
				}
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {

			}
		};
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// 注册监听
		mManager.registerListener(mListener, mSensor,
				SensorManager.SENSOR_DELAY_GAME);

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {

		// 取消监听
		mManager.unregisterListener(mListener);

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}