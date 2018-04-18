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
			.getInstance()).getConfigurationService(); // 获取配置信息（Preferences）的服务对象;

	private LocationManager mLocationManager;

	private Boolean EnableSetGPSData = false; // 是否发送GPS数据至服务器

	private ServiceWorker worker = new ServiceWorker(); // 服务线程

	private DatagramSocket socket;

	Timer myTimer = new Timer(); // 定时器（线程）用于模拟

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
					.getLastKnownLocation(LocationManager.GPS_PROVIDER); // 从GPS获取最近的位置信息。
			if (mLocationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER)
					&& location != null) {
				Log.d("GPSDataService-ServiceWorker-run()",
						"location：径度 "
								+ String.format("%.5f", location.getLongitude())
								+ " 纬度   = "
								+ String.format("%.5f", location.getLatitude()));

				// sendGPSInfo(location);
				// System.out.println("第一次发送gps");

				mLocationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 500, 0,
						new LocationListener() {
							// 备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新

							@Override
							public void onLocationChanged(Location location) {
								// sendGPSInfo(location);

								// Log.d("zhangjie:GPSDataService-ServiceWorker-run()",
								// "onLocationChanged()位置更新sendGPSInfo");
								// System.out.println("位置更新时发送gps");
								// Toast toast = new
								// Toast(getApplicationContext());
								// toast.setText("位置更新时发送gps");
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
								// "onProviderEnabled()位置更新sendGPSInfo");
								// System.out.println("Privider可用时发送gps");

							}

							@Override
							public void onProviderDisabled(String provider) {

							}

						});
			}
			/*
			 * // 判断GPS是否正常启动
			 * 
			 * if (mLocationManager
			 * .isProviderEnabled(LocationManager.GPS_PROVIDER) &&
			 * (mLocationManager
			 * .getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)) {
			 * //
			 * System.out.println("!!!!!!!!!!!!成功从GPSProver获得GPS数据！！！！！！！！！！！！！！！"
			 * ); Location location = mLocationManager
			 * .getLastKnownLocation(LocationManager.GPS_PROVIDER); //
			 * 从GPS获取最近的位置信息。 //sendGPSInfo(location);
			 * 
			 * // 监听 GPS卫星 的状态变化： // lm.addGpsStatusListener(listener);
			 * 
			 * // 监听 位置信息状态、GPS信号获取设备状态 的变化： // 绑定监听，有4个参数 //
			 * 参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种 // 参数2，位置信息更新周期，单位毫秒 //
			 * 参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息 // 参数4，监听 //
			 * 备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新
			 * 
			 * // 2秒更新一次，或最小位移变化超过1米更新一次； //
			 * 注意：此处更新准确度非常低，推荐在service里面启动一个Thread
			 * ，在run中sleep(10000);然后执行handler.sendMessage(),更新位置
			 * LocationListener locationListener = new LocationListener() { //
			 * 位置信息变化时触发
			 * 
			 * @Override public void onLocationChanged(Location location) {
			 * sendGPSInfo(location); Log.i(TAG, "时间：" + location.getTime());
			 * Log.i(TAG, "经度：" + location.getLongitude()); Log.i(TAG, "纬度：" +
			 * location.getLatitude()); Log.i(TAG, "海拔：" +
			 * location.getAltitude());
			 * 
			 * }
			 * 
			 * //GPS状态变化时触发
			 * 
			 * @Override public void onStatusChanged(String provider, int
			 * status, Bundle extras) { switch (status) { // GPS状态为可见时 case
			 * LocationProvider.AVAILABLE: Log.i(TAG, "当前GPS状态为可见状态"); break; //
			 * GPS状态为服务区外时 case LocationProvider.OUT_OF_SERVICE: Log.i(TAG,
			 * "当前GPS状态为服务区外状态"); break; // GPS状态为暂停服务时 case
			 * LocationProvider.TEMPORARILY_UNAVAILABLE: Log.i(TAG,
			 * "当前GPS状态为暂停服务状态"); break; }
			 * 
			 * }
			 * 
			 * //GPS开启时触发
			 * 
			 * @Override public void onProviderEnabled(String provider) {
			 * Location location = mLocationManager
			 * .getLastKnownLocation(provider); sendGPSInfo(location); }
			 * 
			 * //GPS禁用时触发
			 * 
			 * @Override public void onProviderDisabled(String provider) {
			 * Log.i(TAG, "GPS被禁用！");
			 * 
			 * }
			 * 
			 * }; mLocationManager
			 * .requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 8,
			 * locationListener); }
			 */
			else {
				MyLog.d(TAG, "进入模拟GPS");
				// System.out.println("!!!!!!!!!!!!进入模拟GPS！！！！！！！！！！！！！！！");
				// Toast.makeText( null, "请开启GPS导航...",
				// Toast.LENGTH_SHORT).show();
				// Log.e(TAG,"无法获取GPS，GPS未正常启动！！");
				// 模拟。。。。。。
				//sendSimulationGPSInfo(2000);
			}

			Looper.loop(); // sks
		}

		protected void sendGPSInfo(Location location) // 用UDP数据报发送GPS数据
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
			// 发送正规的GPS double型数据：
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
			// 按GIS服务器格式要求，将double型经纬度转换为int型发送：(感觉不合理：业务耦合？)
			int int_longitude, int_latitude;
			int_longitude = (int) (longitude * (1 << 25) / 360.0); // 经度double转int
			if (int_longitude < -1e-6) {
				int_longitude = (int) ((longitude + 360.0) * (1 << 25) / 360.0);
			}

			int_latitude = (int) (latitude * (1 << 24) / 180.0); // //纬度度double转int
			if (int_latitude < -1e-6) {
				int_latitude = (int) ((latitude + 180.0) * (1 << 24) / 180.0);
			}
			byte byteLongtitude[] = new byte[4];
			byte byteLatitude[] = new byte[4];
			for (int i = 0; i < 4; i++) // 经度int转byte[]
			{
				int offset = (byteLongtitude.length - 1 - i) * 8;
				byteLongtitude[i] = (byte) ((int_longitude >>> offset) & 0xFF);
			}
			for (int i = 0; i < 4; i++) // 纬度int转byte[]
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

		public class myTimerTask extends TimerTask // 定时器任务类
		{

			@Override
			public void run() {
				double longitude = 116.27929;
				double latitude = 39.83070;
				// double longitude = (Math.random()*2.08268004)+115.416519;
				// double latitude = (Math.random()*1.6159423)+39.4426667;

				// System.out.println("---------------模拟latitude："+Double.toString(longitude)+"------------------");
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
				// 发送正规的GPS double型数据：
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
				// 按GIS服务器格式要求，将double型经纬度转换为int型发送：(感觉不合理：业务耦合？)
				int int_longitude, int_latitude;
				int_longitude = (int) (longitude * (1 << 25) / 360.0); // 经度double转int
				if (int_longitude < -1e-6) {
					int_longitude = (int) ((longitude + 360.0) * (1 << 25) / 360.0);
				}

				int_latitude = (int) (latitude * (1 << 24) / 180.0); // //纬度度double转int
				if (int_latitude < -1e-6) {
					int_latitude = (int) ((latitude + 180.0) * (1 << 24) / 180.0);
				}
				byte byteLongtitude[] = new byte[4];
				byte byteLatitude[] = new byte[4];
				for (int i = 0; i < 4; i++) // 经度int转byte[]
				{
					int offset = (byteLongtitude.length - 1 - i) * 8;
					byteLongtitude[i] = (byte) ((int_longitude >>> offset) & 0xFF);
				}
				for (int i = 0; i < 4; i++) // 纬度int转byte[]
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
				// System.out.println("----------------到底了------------------");
			}
		}

		protected void sendSimulationGPSInfo(long period) {

			// myTimer.schedule(new myTimerTask(), 1000, 2000); //
			// 1000ms后每个2000ms执行任务（发送GPS模拟信息）。
			myTimer.schedule(new myTimerTask(), 1000, period); // 1000ms后每个2000ms执行任务（发送GPS模拟信息）。
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
		// System.out.println("！！！！！！！--------GPSSERVICE关闭了------------！！！！！！！！！！");
		myTimer.cancel(); // 关闭定时器
		// worker.stop();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
