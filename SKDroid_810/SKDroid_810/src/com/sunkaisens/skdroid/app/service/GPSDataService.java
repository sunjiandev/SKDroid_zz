package com.sunkaisens.skdroid.app.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.utils.MyLog;

import com.sunkaisens.skdroid.Engine;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class GPSDataService extends Service {
	private static final String TAG = GPSDataService.class.getCanonicalName();

	private final INgnConfigurationService mConfigurationService = (Engine
			.getInstance()).getConfigurationService(); // ��ȡ������Ϣ��Preferences���ķ������;

	private LocationManager mLocationManager;

	private Boolean EnableSetGPSData = false; // �Ƿ���GPS������������

	private ServiceWorker worker = new ServiceWorker(); // �����߳�

	private DatagramSocket socket;

	Timer myTimer = new Timer(); // ��ʱ�����̣߳�����ģ��

	public void init() {
		Log.d("zhangjie:GPSDataService-init()", "init()");
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		worker.start();
	}

	public class ServiceWorker extends Thread {
		public void run() {
			Looper.prepare(); // sks

			mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			Location location = mLocationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER); // ��GPS��ȡ�����λ����Ϣ��
			if (mLocationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER)
					&& location != null) {
				Log.d("GPSDataService-ServiceWorker-run()",
						"location������ "
								+ String.format("%.5f", location.getLongitude())
								+ " γ��   = "
								+ String.format("%.5f", location.getLatitude()));

				// sendGPSInfo(location);
				// System.out.println("��һ�η���gps");

				mLocationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 500, 0,
						new LocationListener() {
							// ��ע������2��3���������3��Ϊ0�����Բ���3Ϊ׼������3Ϊ0����ͨ��ʱ������ʱ���£�����Ϊ0������ʱˢ��

							@Override
							public void onLocationChanged(Location location) {
								// sendGPSInfo(location);

								// Log.d("zhangjie:GPSDataService-ServiceWorker-run()",
								// "onLocationChanged()λ�ø���sendGPSInfo");
								// System.out.println("λ�ø���ʱ����gps");
								// Toast toast = new
								// Toast(getApplicationContext());
								// toast.setText("λ�ø���ʱ����gps");
								// toast.setGravity(MODE_PRIVATE, 0, 1500);
								// toast.show();

							}

							@Override
							public void onStatusChanged(String provider,
									int status, Bundle extras) {

							}

							@Override
							public void onProviderEnabled(String provider) {
								// sendGPSInfo(mLocationManager.getLastKnownLocation(provider));
								// Log.d("zhangjie:GPSDataService-ServiceWorker-run()",
								// "onProviderEnabled()λ�ø���sendGPSInfo");
								// System.out.println("Privider����ʱ����gps");

							}

							@Override
							public void onProviderDisabled(String provider) {

							}

						});
			}
			/*
			 * // �ж�GPS�Ƿ���������
			 * 
			 * if (mLocationManager
			 * .isProviderEnabled(LocationManager.GPS_PROVIDER) &&
			 * (mLocationManager
			 * .getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)) {
			 * //
			 * System.out.println("!!!!!!!!!!!!�ɹ���GPSProver���GPS���ݣ�����������������������������"
			 * ); Location location = mLocationManager
			 * .getLastKnownLocation(LocationManager.GPS_PROVIDER); //
			 * ��GPS��ȡ�����λ����Ϣ�� //sendGPSInfo(location);
			 * 
			 * // ���� GPS���� ��״̬�仯�� // lm.addGpsStatusListener(listener);
			 * 
			 * // ���� λ����Ϣ״̬��GPS�źŻ�ȡ�豸״̬ �ı仯�� // �󶨼�������4������ //
			 * ����1���豸����GPS_PROVIDER��NETWORK_PROVIDER���� // ����2��λ����Ϣ�������ڣ���λ���� //
			 * ����3��λ�ñ仯��С���룺��λ�þ���仯������ֵʱ��������λ����Ϣ // ����4������ //
			 * ��ע������2��3���������3��Ϊ0�����Բ���3Ϊ׼������3Ϊ0����ͨ��ʱ������ʱ���£�����Ϊ0������ʱˢ��
			 * 
			 * // 2�����һ�Σ�����Сλ�Ʊ仯����1�׸���һ�Σ� //
			 * ע�⣺�˴�����׼ȷ�ȷǳ��ͣ��Ƽ���service��������һ��Thread
			 * ����run��sleep(10000);Ȼ��ִ��handler.sendMessage(),����λ��
			 * LocationListener locationListener = new LocationListener() { //
			 * λ����Ϣ�仯ʱ����
			 * 
			 * @Override public void onLocationChanged(Location location) {
			 * sendGPSInfo(location); Log.i(TAG, "ʱ�䣺" + location.getTime());
			 * Log.i(TAG, "���ȣ�" + location.getLongitude()); Log.i(TAG, "γ�ȣ�" +
			 * location.getLatitude()); Log.i(TAG, "���Σ�" +
			 * location.getAltitude());
			 * 
			 * }
			 * 
			 * //GPS״̬�仯ʱ����
			 * 
			 * @Override public void onStatusChanged(String provider, int
			 * status, Bundle extras) { switch (status) { // GPS״̬Ϊ�ɼ�ʱ case
			 * LocationProvider.AVAILABLE: Log.i(TAG, "��ǰGPS״̬Ϊ�ɼ�״̬"); break; //
			 * GPS״̬Ϊ��������ʱ case LocationProvider.OUT_OF_SERVICE: Log.i(TAG,
			 * "��ǰGPS״̬Ϊ��������״̬"); break; // GPS״̬Ϊ��ͣ����ʱ case
			 * LocationProvider.TEMPORARILY_UNAVAILABLE: Log.i(TAG,
			 * "��ǰGPS״̬Ϊ��ͣ����״̬"); break; }
			 * 
			 * }
			 * 
			 * //GPS����ʱ����
			 * 
			 * @Override public void onProviderEnabled(String provider) {
			 * Location location = mLocationManager
			 * .getLastKnownLocation(provider); sendGPSInfo(location); }
			 * 
			 * //GPS����ʱ����
			 * 
			 * @Override public void onProviderDisabled(String provider) {
			 * Log.i(TAG, "GPS�����ã�");
			 * 
			 * }
			 * 
			 * }; mLocationManager
			 * .requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 8,
			 * locationListener); }
			 */
			else {
				MyLog.d(TAG, "����ģ��GPS");
				// System.out.println("!!!!!!!!!!!!����ģ��GPS������������������������������");
				// Toast.makeText( null, "�뿪��GPS����...",
				// Toast.LENGTH_SHORT).show();
				// Log.e(TAG,"�޷���ȡGPS��GPSδ������������");
				// ģ�⡣����������
				//sendSimulationGPSInfo(2000);
			}

			Looper.loop(); // sks
		}

		protected void sendGPSInfo(Location location) // ��UDP���ݱ�����GPS����
		{
			// Log.d("zhangjie:GPSDataService-ServiceWorker-sendGPSInfo()",
			// "location = " + location.toString());
			double longitude = location.getLongitude();
			double latitude = location.getLatitude();
			double altitude = location.getAltitude();
			float speed = location.getSpeed();
			long time = location.getTime();

			String name = mConfigurationService.getString(
					NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
					NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME);
			String ID = mConfigurationService.getString(
					NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
					NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME);

			String targetHost = mConfigurationService.getString(
					NgnConfigurationEntry.GPS_SENDTO_HOST,
					NgnConfigurationEntry.DEFAULT_GPS_SENDTO_HOST);
			int targetPort = mConfigurationService.getInt(
					NgnConfigurationEntry.GPS_SENDTO_PORT,
					NgnConfigurationEntry.DEFAULT_GPS_SENDTO_PORT);
			//
			// ���������GPS double�����ݣ�
			/*
			 * byte byteLatitude[] = new byte[8]; byte byteLongtitude[] = new
			 * byte[8];
			 * 
			 * long longLatitude=Double.doubleToLongBits(latitude); for(int
			 * i=0;i<8;i++) { byteLatitude[i]=new
			 * Long(longLatitude).byteValue(); longLatitude=longLatitude >> 8; }
			 * long longLongitude=Double.doubleToLongBits(longitude); for(int
			 * i=0;i<8;i++) { byteLongtitude[i]=new
			 * Long(longLongitude).byteValue(); longLongitude=longLongitude >>
			 * 8; }
			 */
			//
			// ��GIS��������ʽҪ�󣬽�double�;�γ��ת��Ϊint�ͷ��ͣ�(�о�������ҵ����ϣ�)
			int int_longitude, int_latitude;
			int_longitude = (int) (longitude * (1 << 25) / 360.0); // ����doubleתint
			if (int_longitude < -1e-6) {
				int_longitude = (int) ((longitude + 360.0) * (1 << 25) / 360.0);
			}

			int_latitude = (int) (latitude * (1 << 24) / 180.0); // //γ�ȶ�doubleתint
			if (int_latitude < -1e-6) {
				int_latitude = (int) ((latitude + 180.0) * (1 << 24) / 180.0);
			}
			byte byteLongtitude[] = new byte[4];
			byte byteLatitude[] = new byte[4];
			for (int i = 0; i < 4; i++) // ����intתbyte[]
			{
				int offset = (byteLongtitude.length - 1 - i) * 8;
				byteLongtitude[i] = (byte) ((int_longitude >>> offset) & 0xFF);
			}
			for (int i = 0; i < 4; i++) // γ��intתbyte[]
			{
				int offset = (byteLatitude.length - 1 - i) * 8;
				byteLatitude[i] = (byte) ((int_latitude >>> offset) & 0xFF);
			}

			byte byteName[] = new byte[32];
			byte byteID[] = new byte[32];
			byteName = name.getBytes();
			byteID = ID.getBytes();
			//
			byte packetData[] = new byte[72];
			System.arraycopy(byteID, 0, packetData, 0, byteID.length);
			System.arraycopy(byteName, 0, packetData, 32, byteName.length);
			System.arraycopy(byteLongtitude, 0, packetData, 64,
					byteLongtitude.length);
			System.arraycopy(byteLatitude, 0, packetData, 68,
					byteLatitude.length);

			try {
				DatagramPacket outPacket = new DatagramPacket(packetData, 72,
						InetAddress.getByName(targetHost), targetPort);
				// DatagramSocket socket = new DatagramSocket();
				socket.send(outPacket);

			} catch (UnknownHostException e) {
				e.printStackTrace();
				socket.close();
			} catch (SocketException e) {
				e.printStackTrace();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
				socket.close();
			}

			// Toast toast = new Toast(getApplicationContext());
			// toast.setText("sendGPSInfo");
			// toast.setGravity(MODE_PRIVATE, 0, 1000);
			// toast.show();
		}

		public class myTimerTask extends TimerTask // ��ʱ��������
		{

			@Override
			public void run() {
				double longitude = 116.27929;
				double latitude = 39.83070;
				// double longitude = (Math.random()*2.08268004)+115.416519;
				// double latitude = (Math.random()*1.6159423)+39.4426667;

				// System.out.println("---------------ģ��latitude��"+Double.toString(longitude)+"------------------");
				// double altitude = location.getAltitude();
				// float speed = location.getSpeed();
				// long time = location.getTime();

				String name = "aNameForTest";
				String ID = "anIDForTest";

				String targetHost = mConfigurationService.getString(
						NgnConfigurationEntry.GPS_SENDTO_HOST,
						NgnConfigurationEntry.DEFAULT_GPS_SENDTO_HOST);
				int targetPort = mConfigurationService.getInt(
						NgnConfigurationEntry.GPS_SENDTO_PORT,
						NgnConfigurationEntry.DEFAULT_GPS_SENDTO_PORT);
				//
				// ���������GPS double�����ݣ�
				/*
				 * byte byteLatitude[] = new byte[8]; byte byteLongtitude[] =
				 * new byte[8];
				 * 
				 * long longLatitude=Double.doubleToLongBits(latitude); for(int
				 * i=0;i<8;i++) { byteLatitude[i]=new
				 * Long(longLatitude).byteValue(); longLatitude=longLatitude >>
				 * 8; } long longLongitude=Double.doubleToLongBits(longitude);
				 * for(int i=0;i<8;i++) { byteLongtitude[i]=new
				 * Long(longLongitude).byteValue(); longLongitude=longLongitude
				 * >> 8; }
				 */
				//
				// ��GIS��������ʽҪ�󣬽�double�;�γ��ת��Ϊint�ͷ��ͣ�(�о�������ҵ����ϣ�)
				int int_longitude, int_latitude;
				int_longitude = (int) (longitude * (1 << 25) / 360.0); // ����doubleתint
				if (int_longitude < -1e-6) {
					int_longitude = (int) ((longitude + 360.0) * (1 << 25) / 360.0);
				}

				int_latitude = (int) (latitude * (1 << 24) / 180.0); // //γ�ȶ�doubleתint
				if (int_latitude < -1e-6) {
					int_latitude = (int) ((latitude + 180.0) * (1 << 24) / 180.0);
				}
				byte byteLongtitude[] = new byte[4];
				byte byteLatitude[] = new byte[4];
				for (int i = 0; i < 4; i++) // ����intתbyte[]
				{
					int offset = (byteLongtitude.length - 1 - i) * 8;
					byteLongtitude[i] = (byte) ((int_longitude >>> offset) & 0xFF);
				}
				for (int i = 0; i < 4; i++) // γ��intתbyte[]
				{
					int offset = (byteLatitude.length - 1 - i) * 8;
					byteLatitude[i] = (byte) ((int_latitude >>> offset) & 0xFF);
				}

				byte byteName[] = new byte[32];
				byte byteID[] = new byte[32];
				byteName = name.getBytes();
				byteID = ID.getBytes();
				//
				byte packetData[] = new byte[72];
				System.arraycopy(byteID, 0, packetData, 0, byteID.length);
				System.arraycopy(byteName, 0, packetData, 32, byteName.length);
				System.arraycopy(byteLongtitude, 0, packetData, 64,
						byteLongtitude.length);
				System.arraycopy(byteLatitude, 0, packetData, 68,
						byteLatitude.length);

				try {
					DatagramPacket outPacket = new DatagramPacket(packetData,
							72, InetAddress.getByName(targetHost), targetPort);

					socket.send(outPacket);

				} catch (UnknownHostException e) {
					e.printStackTrace();
					socket.close();
				} catch (SocketException e) {
					e.printStackTrace();
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
					socket.close();
				}
				// System.out.println("----------------������------------------");
			}
		}

		protected void sendSimulationGPSInfo(long period) {

			// myTimer.schedule(new myTimerTask(), 1000, 2000); //
			// 1000ms��ÿ��2000msִ�����񣨷���GPSģ����Ϣ����
			myTimer.schedule(new myTimerTask(), 1000, period); // 1000ms��ÿ��2000msִ�����񣨷���GPSģ����Ϣ����
		}
	}

	public void onCreate() {
		super.onCreate();
		Log.v(TAG, "GPSDataService is created.");
		// Toast.makeText(getBaseContext(), "GPSDataService is created",
		// Toast.LENGTH_LONG).show();
		if (EnableSetGPSData) {
			init();
		}
	}

	public void onDestroy() {
		super.onDestroy();
		// System.out.println("��������������--------GPSSERVICE�ر���------------��������������������");
		myTimer.cancel(); // �رն�ʱ��
		// worker.stop();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
