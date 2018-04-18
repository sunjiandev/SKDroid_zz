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

		// ��ȡϵͳ����POWER_SERVICE������һ��PowerManager����
		localPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// ��ȡPowerManager.WakeLock����,����Ĳ���|��ʾͬʱ��������ֵ,������LogCat���õ�Tag
		localWakeLock = this.localPowerManager.newWakeLock(32, "MyPower");

		// ��ȡϵͳ����SENSOR_SERVICE������һ��SensorManager����
		mManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		// ��ȡ�����Ӧ������
		mSensor = mManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		// ע���Ӧ���¼�
		mListener = new SensorEventListener() {

			@Override
			public void onSensorChanged(SensorEvent event) {

				float[] its = event.values;

				if (its != null && event.sensor.getType() == Sensor.TYPE_PROXIMITY) {

					Log.d("","its[0]:" + its[0]);

					// �������ԣ��������������Ӧ����ʱ��its[0]����ֵΪ0.0�������뿪ʱ����1.0
					if (its[0] == 0.0) {// �����ֻ�

						Log.d("sensor","�ַ���ȥ��...");

						if (localWakeLock.isHeld()) {
							Log.d("sersor","return");
							return;
						} else
							localWakeLock.acquire();// �����豸��Դ��

					} else {// Զ���ֻ�

						Log.d("sersor","���ÿ���...");

						if (localWakeLock.isHeld()) {
							Log.d("sersor","return");
							return;
						} else
							localWakeLock.setReferenceCounted(false);
						localWakeLock.release(); // �ͷ��豸��Դ��
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

		// ע�����
		mManager.registerListener(mListener, mSensor,
				SensorManager.SENSOR_DELAY_GAME);

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {

		// ȡ������
		mManager.unregisterListener(mListener);

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}