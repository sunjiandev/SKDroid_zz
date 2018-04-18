package com.sunkaisens.skdroid.Services;

import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnMessagingSession;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.utils.MyLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.MessageTypes;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Screens.ScreenMap;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.util.GlobalVar;

public class ServiceGPSReport {
	
	private final static String TAG = ServiceGPSReport.class.getCanonicalName();
	
	public static final int TERMINAL_OPEN=1;
	public static final int TERMINAL_CLOSE=2;
	
	private LocationManager mLocationManager;
	
	private double longitude = 0;
	private double latitude = 0;
	
	public  double mLastLocationLongitude =  116.27929;//径度  118.46 118.78333; //
	public  double mLastLocationLatitude = 39.83070;//纬度 36.03    32.05000; //
	
	private NgnTimer reportTimer;
	
	private final INgnSipService mSipService;
	
	private String remotePartyUri;
	private String mDisplayName;
	private String mPhoneNumber;
	
	private static Handler mMapHandler;	
	private int mImageId;
	
	/**
	 * 判断GIS上报是否打开
	 */
	private boolean isStart = false;
	
	private final INgnConfigurationService mConfigurationService = (Engine.getInstance()).getConfigurationService(); // 获取配置信息（Preferences）的服务对象;
	public long period;
	public void setPeriod(long period) {
		this.period = period;
	}

	private Boolean EnableSendGPSData = false; // 是否发送GPS数据至服务器
	public GPSReportThread mGPSReportThread;
	private static ServiceGPSReport instance = null;
	
	private Location lastLocation = null;//记录上一个上报的位置信息
	private int speedLimit = 33;//速度限定值  m/s  120km/h
	private String appName = null;//应用名
	private boolean gpsNotifSign1 = false;//标记gps不可用通知是否已经显示过
	
	private boolean oldLoc = false;
	
	private Engine mEngine;
	
	private BroadcastReceiver mGisReceiver = null;
	
	public static boolean bFirst = true;
	
	public int k = 0;//temp
	private Boolean UseSimulateGPSDate = true; // whether use sendSimulationGPSInfo
	
	public ServiceGPSReport()
	{
		mEngine = (Engine) Engine.getInstance();
		mSipService = mEngine.getSipService();
		mLocationManager =(LocationManager)NgnApplication.getContext().getSystemService(Context.LOCATION_SERVICE);
	
		mPhoneNumber = mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME);
		mDisplayName = SystemVarTools.getDisplayNameFromPhoneNumber(mPhoneNumber);	
		
//		reportTimer = new NgnTimer();
	}
	
	public static ServiceGPSReport getInstance()
	{
		if(instance == null)
		{
			instance = new ServiceGPSReport();
		}
		//
		return instance;
	} 
	public boolean getGPSServiceReprotStart()
	{
		return EnableSendGPSData;
	}
	public void SetRemoteRartyUri(String remotePartyUri)
	{
		this.remotePartyUri = remotePartyUri;
	}
	
	public static void setMapHandler(Handler mapHandler) {
		mMapHandler = mapHandler;
	}

	public  void GPSReport(String contentBody,String appName)
	{
		Log.d(TAG, "GIS消息:"+contentBody);
		this.appName = appName;
		int nnStart = contentBody.indexOf("<command from") + "<command from".length();
		int nsEnd;
		contentBody = contentBody.substring(nnStart);
		if(contentBody.contains("period"))
		{
			nnStart= contentBody.indexOf("period=\"") + "period=\"".length();
			contentBody = contentBody.substring(nnStart);
			nsEnd = contentBody.indexOf("\"");
			String periodStr = contentBody.substring(0, nsEnd).trim();
			period = Long.parseLong(periodStr);
			Log.d(TAG, "GIS消息  上报间隔:"+period);
			contentBody = contentBody.substring(nsEnd + 1);
		}
		nnStart = contentBody.indexOf(">") + ">".length();
		contentBody = contentBody.substring(nnStart);
		nsEnd = contentBody.indexOf("<");
		String comType = contentBody.substring(0, nsEnd).trim();
		Log.d(TAG, "GIS消息  指令:"+comType);
		switch(Integer.parseInt(comType))
		{
			case TERMINAL_OPEN:
				//防止收到多条打开上报指令时显示多条提示问题
				if(!isStart){
//					SystemVarTools.showToast(appName + "提示：\n开启位置上报");
					mEngine.showGISReportNotif(R.drawable.icon, "正在上报位置信息");
				}
				isStart = true;
				StartGPSReport();
				
				break;
			case TERMINAL_CLOSE:
				
				//防止收到多条关闭上报指令时显示多条提示问题
				if(isStart){
//					SystemVarTools.showToast(appName + "提示：\n关闭位置上报");
					mEngine.cancelGISReportNotif();
				}
				isStart = false;
				StopGPSReport();
				
				break;
			default:
				break;
		}
		
	
	}
	
	public  void StartGPSReport()
	{
		//瀚讯大终端gps信息采用特殊方式获取		
		if(SKDroid.isBhSocket()){
			MyLog.d(TAG, "Is HX socket.Start receiver.");
			dealWithHXsocket();
			
		}else {
			MyLog.d(TAG, "Is not HX socket. Start GPS Thread.");
			mGPSReportThread = new GPSReportThread();
			EnableSendGPSData = true;
			mGPSReportThread.start();
		}
		
		if(reportTimer != null){
			try {
				reportTimer.cancel();
				reportTimer.purge();
			} catch (Exception e) {
				e.printStackTrace();
			}
			reportTimer = null;
		}
		reportTimer = new NgnTimer();
		reportTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				if(instance != null){
					instance.sendGPSInfo(longitude,latitude);
				}
			}
		}, period*1000,period*1000);
	}
	public void StopGPSReport()
	{
		Intent intent = new Intent(MessageTypes.MSG_GIS_EVENT);
		intent.putExtra(MessageTypes.MSG_GIS_TYPE, MessageTypes.MSG_GIS_REQUEST_STOP);
		SKDroid.getContext().sendBroadcast(intent);
		MyLog.d(TAG, "Message = "+MessageTypes.MSG_GIS_REQUEST_STOP+"  sended.");
		if(mGisReceiver != null){
			SKDroid.getContext().unregisterReceiver(mGisReceiver);
			mGisReceiver = null;
			MyLog.d(TAG, "mGisReceiver unregisted.");
		}
		reportTimer.cancel();
		reportTimer.purge();
		reportTimer = null;
		
		if(mLocationManager != null && llListener != null){
			mLocationManager.removeUpdates(llListener);
		}
		
		EnableSendGPSData = false;
		instance = null;
		
		Log.d("ServiceGPSReport","LBS ServiceGPSReport: StopGPSReport...");
		//mGPSReportThread.stop();
	}
	
	private void dealWithHXsocket(){
		MyLog.d(TAG, "dealWithHXsocket()");
		Intent intent = new Intent(MessageTypes.MSG_GIS_EVENT);
		intent.putExtra(MessageTypes.MSG_GIS_TYPE, MessageTypes.MSG_GIS_REQUEST_START);
		if(GlobalVar.orderedbroadcastSign){
			SKDroid.getContext().sendOrderedBroadcast(intent,null);
		}else {
			SKDroid.getContext().sendBroadcast(intent);
		}
		mGisReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				MyLog.d(TAG, "Receive a msg,action="+action);
				if(action.equals(MessageTypes.MSG_GIS_RESPONSE)){
					String lat = intent.getStringExtra("lat");
					String lon = intent.getStringExtra("lon");
					if(lat == null
							|| lon == null){
						MyLog.d(TAG, "Receive GIS info ERROR. Latitude or longitude is null.");
					}else {
						//lat 31,13,38,N   lon 121,20,899,E
						MyLog.d(TAG, "Receive GIS info.lat="+lat+",lon="+lon);
						String[] latStrs = lat.split(",");
						String[] lonStrs = lon.split(",");
						if(latStrs.length != 4 || lonStrs.length != 4){
							MyLog.d(TAG, "lat or lon is wrong.");
						}else {
							try{
								int a = Integer.parseInt(latStrs[0]); //度
								int b = Integer.parseInt(latStrs[1]); //分
								int c = Integer.parseInt(latStrs[2]); //秒
								latitude = a+(double)b/60+(double)c/3600;
							}catch(NumberFormatException numberFormatException){
								MyLog.d(TAG, "Latitude numberFormatException");
								numberFormatException.printStackTrace();
								latitude = 0;
							}
							try{
								int a = Integer.parseInt(lonStrs[0]); //度
								int b = Integer.parseInt(lonStrs[1]); //分
								int c = Integer.parseInt(lonStrs[2]); //秒
								longitude = a+(double)b/60+(double)c/3600;
							}catch(NumberFormatException numberFormatException){
								MyLog.d(TAG, "Longitude numberFormatException");
								numberFormatException.printStackTrace();
								longitude = 0;
							}
							MyLog.d(TAG, "GIS INFO lat="+latitude+"  lon="+longitude);
						}
						
					}
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(MessageTypes.MSG_GIS_RESPONSE);
		SKDroid.getContext().registerReceiver(mGisReceiver, filter);
	}
	
	//************************************GPS************************************************
	private class GPSReportThread extends Thread
	{
		
		public void run() {
			Looper.prepare();
			Log.d(TAG,"LBS ServiceGPSReport: StartGPSReport...");
			while(EnableSendGPSData == true)
			{
				Location location = null;
				Location gpsLoc = null;
				Location netLoc = null;
				
				Log.i(TAG, "LBS GPS:"+mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)+"  "
						+ "NETWORK:"+mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
				
				//判断GPS和network是否可用
				if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
						mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {	//判断gps或是网络定位是否可用		
					
					MyLog.d(TAG,"LBS 定位服务已开.");//发送模拟数据
					
					if(gpsNotifSign1){
						mEngine.showGISReportNotif(R.drawable.icon, "正在上报位置");
					}
					gpsNotifSign1 = false;
					
					List<String> providers = mLocationManager.getProviders(false);
					
					//如果GPS可用，则添加GPS   
					if(providers!=null && providers.contains(LocationManager.GPS_PROVIDER)){
						gpsLoc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); // 从GPS获取最近的位置信息。
							mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, llListener);
					}else {
						Log.d(TAG, "LBS no gps provider");
					}
					
					//如果network可用，则添加network
					if(providers!=null && providers.contains(LocationManager.NETWORK_PROVIDER)){
						netLoc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); // 从network获取最近的位置信息。
						mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, llListener);
					}else {
						Log.d(TAG, "LBS no network provider");
					}
					
					if(gpsLoc != null && netLoc != null ){
						Log.d(TAG, "LBS isBetterLocation(netLoc, gpsLoc)"+isBetterLocation(netLoc, gpsLoc));
						if(isBetterLocation(netLoc, gpsLoc)){
							location = netLoc;
						}else {
							location = gpsLoc;
						}
					}else if (gpsLoc != null) {
						location = gpsLoc;
					}else {
						location = netLoc;
					}
					
					if(location != null){
						//两次定位的平均速度小于设定的速度极限值
						if (lastLocation != null) {						
							if(isBetterLocation(location, lastLocation)){
								longitude = location.getLongitude();
								latitude = location.getLatitude();
								lastLocation = location;
							}else {
								longitude = lastLocation.getLongitude();
								latitude = lastLocation.getLatitude();
							}
						} else {
							longitude = location.getLongitude();
							latitude = location.getLatitude();

							lastLocation = location;
							mLastLocationLongitude = location.getLongitude();
							mLastLocationLatitude = location.getLatitude();
						}
						break;
					}else {
						MyLog.d(TAG,"LBS 定位信息获取失败.");//发送模拟数据
					}
				}
				else {					
					if(!gpsNotifSign1){
						if(UseSimulateGPSDate){
							SystemVarTools.showToast(appName + "提示：\n 定位服务未开启，将启用虚拟数据.");
							Log.d(TAG,"First GPSReprot: "+UseSimulateGPSDate);
						}
						mEngine.showGISReportNotif(R.drawable.icon, "位置服务未开, 模拟上报！");
						gpsNotifSign1 = true;
					}
					
					
					if(UseSimulateGPSDate){
						//SystemVarTools.showToast(appName + "提示：\n 定位服务未开启，将启用虚拟数据.");
						
						Log.d(TAG,"LBS GPSReprot: "+UseSimulateGPSDate);
					
					
						if(lastLocation != null){//gps不可用时，如果有上一个位置信息，则循环上报上一个位置信息
							longitude = lastLocation.getLongitude();
							latitude = lastLocation.getLatitude();
						}
						mLastLocationLongitude = mLastLocationLongitude + 0.0001;
						mLastLocationLatitude = mLastLocationLatitude+0.0001;
	
						longitude = mLastLocationLongitude;
						latitude = mLastLocationLatitude;
						
						++k;		
					}
					
					try {
						Thread.sleep(period*1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}				
			}
			Looper.loop(); 
		}
		
	}
		
		private LocationListener llListener = new LocationListener() { 
			// 备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新

			@Override
			public void onLocationChanged(Location location) {
				
				Log.d(TAG, "LBS Location change 经度="+location.getLatitude()+"  纬度="+location.getLongitude());
//				SystemVarTools.showToast("Location change 经度="+location.getLatitude()+"  纬度="+location.getLongitude());
				if(location != null){
					longitude = location.getLongitude();
					latitude = location.getLatitude();
				}
				
			}

			@Override
			public void onStatusChanged(String provider,
					int status, Bundle extras) {

			}
			@Override
			public void onProviderEnabled(String provider) {
				Log.d(TAG, "LBS Provider enable  provider="+provider);
				Location location = mLocationManager.getLastKnownLocation(provider);
				if(location != null){
					longitude = location.getLongitude();
					latitude = location.getLatitude();
				}
				
			}

			@Override
			public void onProviderDisabled(String provider) {
				Log.d(TAG, "LBS Provider disable  provider="+provider);
			}

		};

		public String ctreateExpandedField(double longitude,double latitude) {
			String strBody ="<?xml version=\"1.0\" encoding=\"UTF-8\"?><gpsinfo><type>real</type><user id=\""
					+mPhoneNumber+"\" displayName=\""+mDisplayName+"\"><longitude>"+
					String.format("%.5f", longitude)+"</longitude><latitude>"+String.format("%.5f", latitude)+"</latitude></user></gpsinfo>"
					;
			return strBody;
		}
		public void sendGPSInfo(double longitude,double latitude) // 发送GPS数据
		{
			boolean ret = false;
			final NgnMessagingSession imSession = NgnMessagingSession.createOutgoingSession(mSipService.getSipStack(),remotePartyUri);
			//String localMsgID = "UE" + java.util.UUID.randomUUID().toString();
			String mes = ctreateExpandedField(longitude,latitude);
			if (!(ret = imSession.sendGPSMessage_data(mes))) {
				Log.d("SEND ERROR","remotePartyUri = "+remotePartyUri);
			}
			Log.d(TAG,"LBS ServiceGPSReport: sending...   longitude="+longitude+"  latitude="+latitude);
			NgnMessagingSession.releaseSession(imSession);
	
		}
		
		private static final int TWO_MINUTES = 1000 * 60;

		/** Determines whether one Location reading is better than the current Location fix
		  * @param location  The new Location that you want to evaluate
		  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
		  */
		protected boolean isBetterLocation(Location location, Location currentBestLocation) 
		{
		    if (currentBestLocation == null) 
		    {
		        // A new location is always better than no location
		        return true;
		    }

		    // Check whether the new location fix is newer or older
		    long timeDelta = location.getTime() - currentBestLocation.getTime();
		    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		    boolean isNewer = timeDelta > 0;

		    // If it's been more than two minutes since the current location, use the new location
		    // because the user has likely moved
		    if (isSignificantlyNewer) 
		    {
		        return true;
		    // If the new location is more than two minutes older, it must be worse
		    } 
		    else if (isSignificantlyOlder) 
		    {
		        return false;
		    }

		    // Check whether the new location fix is more or less accurate
		    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		    boolean isLessAccurate = accuracyDelta > 0;
		    boolean isMoreAccurate = accuracyDelta < 0;
		    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		    // Check if the old and new location are from the same provider
		    boolean isFromSameProvider = isSameProvider(location.getProvider(),
		            currentBestLocation.getProvider());

		    // Determine location quality using a combination of timeliness and accuracy
		    if (isMoreAccurate) 
		    {
		        return true;
		    } 
		    else if (isNewer && !isLessAccurate) 
		    {
		        return true;
		    } 
		    else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) 
		    {
		        return true;
		    }
		    return false;
		}
		
		/** Checks whether two providers are the same */
		private boolean isSameProvider(String provider1, String provider2) 
		{
		    if (provider1 == null) 
		    {
		      return provider2 == null;
		    }
		    return provider1.equals(provider2);
		}
		
		/**
		 * 打开gps位置上报
		 */
		public static void openGpsReport(String mRemoteParty) {
			Log.d(TAG, "openGpsReport()");
			final String remotePartyUri = NgnUriUtils.makeValidSipUri(mRemoteParty);
			final NgnMessagingSession imSession = NgnMessagingSession.createOutgoingSession(Engine.getInstance().getSipService().getSipStack(), remotePartyUri);
//			String mes = ctreateGpsReportXml(mRemoteParty, "0", "3", "0");
			String mes = ctreateGpsReportXml(mRemoteParty, "0", NgnConfigurationEntry.GPS_REPORT_PERIOD, "0");
			if (!imSession.sendGPSMessage(mes)) {
				Log.d("open error", "remotePartyUri = " + remotePartyUri);
			}
			NgnMessagingSession.releaseSession(imSession);
		}

		/**
		 * 关闭gps位置上报
		 */
		public static void closeGpsReport(String mRemoteParty, HashMap<String, String> contactMapGps, List<HashMap<String, String>> contactListGps) {
			Log.d(TAG, "closeGpsReport()");
			final String remotePartyUri = NgnUriUtils.makeValidSipUri(mRemoteParty);
			final NgnMessagingSession imSession = NgnMessagingSession.createOutgoingSession(Engine.getInstance().getSipService().getSipStack(), remotePartyUri);
//			String mes = ctreateGpsReportXml(mRemoteParty, "0", "3", "2");
			String mes = ctreateGpsReportXml(mRemoteParty, "0", NgnConfigurationEntry.GPS_REPORT_PERIOD, "2");
			if (!imSession.sendGPSMessage(mes)) {
				Log.d("close error", "remotePartyUri = " + remotePartyUri);
			}
			NgnMessagingSession.releaseSession(imSession);

			if (mMapHandler != null) { //发送在地图上面删除终端标识的消息
				Log.d(TAG, "closeGpsReport() - mMapHandler != null");
				Message msg = Message.obtain(mMapHandler, MessageTypes.MSG_MAP_GPS_REMOVE);
				Bundle b = new Bundle();
				b.putString("id", mRemoteParty); //id
				msg.setData(b);
				mMapHandler.sendMessage(msg);
			}

			int count = contactListGps.size();
			if (count > 0) {
				for (int i = 0; i < count; i++) {
					HashMap<String, String> hm = contactListGps.get(i);
					String str = null;
					if (hm != null && (str = hm.get("id")) != null 
							&& str.equals(mRemoteParty)) {
						contactListGps.remove(i);
						break;
					}
				}
			}

			contactMapGps.remove(mRemoteParty);
		}

		/**
		 * 创建gps位置上报xml命令字符串
		 */
		private static String ctreateGpsReportXml(String mRemoteParty, String isGroup, String period, String command) {
			String localMobileNo = Engine.getInstance().getConfigurationService().getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME);
			String strBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
			strBody += "<gpscommands>\n";
			strBody += "\t<command from=\"" + localMobileNo + "\" to=\"" + mRemoteParty + "\" isGroup=\"" + isGroup + "\"";
			strBody += " period=\"" + period + "\" start=\"\" end=\"\">" + command + "</command>\n";
			strBody += "</gpscommands>\n";
			return strBody;
		}

		/**
		 * 接收、解析GPS上报数据
		 * 在地图上面标绘终端图像
		 * @param contentBody
		 * @param appName
		 */
		public void receiveGPSInfo(String contentBody) {
			int startIndex = contentBody.indexOf("<user id=\"");
			int endIndex = contentBody.indexOf("\" displayName=\"");
			String id = contentBody.substring(startIndex + 10, endIndex);
			
			startIndex = contentBody.indexOf("\" displayName=\"");
			endIndex = contentBody.indexOf("\"><longitude>");
			String name = contentBody.substring(startIndex + 15, endIndex);
			
			startIndex = contentBody.indexOf("<longitude>");
			endIndex = contentBody.indexOf("</longitude>");
			String longitude = contentBody.substring(startIndex + 11, endIndex);
			
			startIndex = contentBody.indexOf("<latitude>");
			endIndex = contentBody.indexOf("</latitude>");
			String latitude = contentBody.substring(startIndex + 10, endIndex);

			if (!ScreenMap.contactMapGps.containsKey(id)) {
				HashMap<String, String> userinfo = new HashMap<String, String>();
				userinfo.put("id", id);
				userinfo.put("name", name);
				mImageId = SystemVarTools.getImageIDFromNumber(id);
				userinfo.put("imageId", mImageId + "");
				userinfo.put("lon", longitude);
				userinfo.put("lat", latitude);
				ScreenMap.contactListGps.add(userinfo);

				ScreenMap.contactMapGps.put(id, null);

				bFirst = true;
			}

			if (mMapHandler != null) { //发送在地图上面创建/移动终端标识的消息
				Log.d(TAG, "GPSReportThread - mMapHandler != null");
				Log.d(TAG, "id = " + id);
				Log.d(TAG, "name = " + name);
				Log.d(TAG, "longitude = " + longitude);
				Log.d(TAG, "latitude = " + latitude);
//				Message msg = Message.obtain(mMapHandler, MessageTypes.MSG_MAP_GPS_CREATE);
				Message msg = null;
				if (bFirst) {
					msg = Message.obtain(mMapHandler, MessageTypes.MSG_MAP_GPS_CREATE);
					bFirst = false;
				}
				else {
					msg = Message.obtain(mMapHandler, MessageTypes.MSG_MAP_GPS_MOVE);
				}
				Bundle b = new Bundle();
				b.putString("id", id); //id
				b.putString("name", name); //name
				b.putString("lon", longitude); //lon
				b.putString("lat", latitude); //lat
				msg.setData(b);
				mMapHandler.sendMessage(msg);
			}
		}
}
