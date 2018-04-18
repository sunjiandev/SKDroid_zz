package com.sks.adhoc.service;

import java.io.Serializable;

import com.sunkaisens.skdroid.Services.ServiceAV;

import android.util.Log;

public class SKSPerson {
	private static final String TAG = SKSPerson.class.getCanonicalName();
	private String personNickeName = null;
	private String mobileNo = null;
	private String ipAddress = null;
	private long loginTime = 0;
	private long heartbeatTime = 0;
	
	public int cmdType=0;
	
	private int GroupNumStrLen = 2;
	private int UserNumStrLen = 3;
	private String IsGroupNum = "001";
	
	/**集群业务 **/
	//private int callerGroupNumber = -1; // 主叫
	private int calleeGroupNumber = -1; // 被叫
	private boolean isGroupLeader = false;
	private int audioPort = -1;
	private int videoPort = -1;
	private boolean isSuperGroupCall = false;
	private int mLastRecCmd = -1;
	
	

	public SKSPerson(String ipAddress,String personNickeName,String mobile,long loginTime){
		this.ipAddress = ipAddress;
		this.personNickeName = personNickeName;
		this.mobileNo = mobile;
		this.loginTime = loginTime;
		
		setOtherPropertity(this.mobileNo);
	}

	public String toString() {
		String person = new StringBuffer(ipAddress == null ? "" : ipAddress+":")
				.append(personNickeName == null ? "" : personNickeName+":")
					.append(mobileNo == null ? "" : mobileNo+":")
						.append(loginTime+":").append(cmdType+":")
						.append(audioPort+":").append(videoPort+":")
						.append(calleeGroupNumber+":").toString();
		//8个
		//192.168.1.125:11:19811207001:1446501315093:10:3682:2816:7: 
		String personStr = String.format("%s:%s:%s:%s:%s:%s:%s:%s:", this.ipAddress,this.personNickeName,
										this.mobileNo,this.loginTime,
										Integer.toString(this.cmdType),Integer.toString(this.audioPort),
										Integer.toString(this.videoPort),Integer.toString(this.calleeGroupNumber));
		Log.d("SKSPerson:", personStr);
		return person;
	}
	
	public SKSPerson(String input)
	{
		Log.d("SKSPerson", "content:"+input);
		String[] s=input.split(":");
		this.ipAddress = s[0];
		Log.d("SKSPerson", "ipAddress:"+ipAddress);
		this.personNickeName =s[1];
		Log.d("SKSPerson", "personNickeName:"+personNickeName);
		this.mobileNo = s[2];
		Log.d("SKSPerson", "mobileNo:"+mobileNo);
		setOtherPropertity(this.mobileNo); //default callee
		
		this.loginTime = Long.parseLong(s[3]);
		Log.d("SKSPerson", "loginTime:"+loginTime);
		this.cmdType=Integer.parseInt(s[4]);
		Log.d("SKSPerson", "cmdType:"+cmdType);
		this.audioPort = Integer.parseInt(s[5]);
		this.videoPort = Integer.parseInt(s[6]);
		Log.d(TAG,String.format("audioPort = %d; videoPort = %d", this.audioPort,this.videoPort));
		
		//Log.d(TAG,String.format("callerGroupNumber = %d", this.callerGroupNumber));
		
		
	}
	
	/*
	 * the last five num, eg:198-110a-bcde
	 * isGroupLeader: if(cde=="001")
	 *  groupNumber = ab
	 * */
	protected boolean setOtherPropertity( String phoneNumber)
	{
		if(phoneNumber.length() <= 0)
			return false;
		setSuperGroupCall(false);
		/*if(phoneNumber.trim().length() < (GroupNumStrLen+UserNumStrLen)){
			Log.d(TAG,"phoneNumber = " + phoneNumber + ",its length is too short!");
			int _mLen = GroupNumStrLen+UserNumStrLen - phoneNumber.trim().length();
			switch(_mLen ){
			case 1:
				phoneNumber = "0" + phoneNumber;
				break;
			case 2:
				phoneNumber = "00" + phoneNumber;
				break;
			case 3:
				phoneNumber = "000" + phoneNumber;
				break;
			case 4:
				phoneNumber = "0000" + phoneNumber;
				break;
			}
			this.mobileNo = phoneNumber;
			Log.d(TAG,"phoneNumber = " + this.mobileNo );
		}
		int length = phoneNumber.trim().length();
		String groupStr = phoneNumber.trim().substring(length-1-GroupNumStrLen-UserNumStrLen+1, length-1-UserNumStrLen+1);
		String userStr = phoneNumber.trim().substring(length-1-UserNumStrLen+1, length);
		
		this.calleeGroupNumber = Integer.parseInt(groupStr);
		this.isGroupLeader = userStr.trim().equalsIgnoreCase(IsGroupNum);
		//Log.d(TAG,String.format("groupStr = %s;  userStr = %s", phoneNumber.substring(5,7), phoneNumber.substring(7,10)));
		Log.d(TAG,String.format("groupStr = %s;  userStr = %s; groupNumber = %d, isGroupLeader = %s",  groupStr, userStr,calleeGroupNumber,isGroupLeader==true?"true":"false"));
		*/
		return true;
	}
	public SKSPerson(){}

	public boolean isGroupLeader()
	{
		return this.isGroupLeader;
	}
	public String getMobileNo()
	{
		return this.mobileNo;
	}
	public void setMobileNo(String _mobileNo)
	{
		this.mobileNo = _mobileNo;
	}
	public String getPersonNickeName() {
		return personNickeName;
	}

	public void setPersonNickeName(String personNickeName) {
		this.personNickeName = personNickeName;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

	public long getHeartbeatTime()
	{
		return this.heartbeatTime;
	}
	public void setHeartbeatTime(long _heartbeatTime)
	{
		this.heartbeatTime = _heartbeatTime;
	}
	public int getCmdType() {
		return cmdType;
	}

	public void setCmdType(int cmdType) {
		this.cmdType = cmdType;
	}
	public void setAudioPort(int _audioPort)
	{
		this.audioPort = _audioPort;
	}
	public int getAudioPort()
	{
		return this.audioPort;
	}
	public void setVideoPort(int _videoPort)
	{
		this.videoPort = _videoPort;
	}
	public int getVideoPort()
	{
		return this.videoPort;
	}
//	public int getCallerGroupNumber()
//	{
//		return this.callerGroupNumber;
//	}
//	
//	public boolean setCallerGroupNumber(int _groupNumber)
//	{
//		this.callerGroupNumber = _groupNumber;
//		return true;
//	}
	public int getCalleeGroupNumber()
	{
		return this.calleeGroupNumber;
	}
	
	public boolean setCalleeGroupNumber(int _groupNumber)
	{
		this.calleeGroupNumber = _groupNumber;
		return true;
	}
	public boolean getIsGroupLeader()
	{
		return this.isGroupLeader == true;
	}
	public boolean setIsGroupLeaner(boolean _isGroupLeader)
	{
		this.isGroupLeader = _isGroupLeader;
		return true;
	}
	public boolean getIsSuperGroupCall()
	{
		return this.isSuperGroupCall;
	}
	public void setSuperGroupCall(boolean isSuperCall)
	{
		this.isSuperGroupCall = isSuperCall;
	}
	public void setLastRecCmd(int cmd)
	{
		this.mLastRecCmd = cmd;
	}
	public int getLastRecCmd()
	{
		return this.mLastRecCmd;
	}
	public void clearRTPPort(){
		this.audioPort = -1;
		this.videoPort = -1;
		this.isSuperGroupCall = false;
		//this.mLastRecCmd = -1;
	}
	
}

