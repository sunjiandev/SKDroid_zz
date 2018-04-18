package com.sunkaisens.skdroid.model;

import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.model.NgnHistoryEvent.StatusType;

public class ModelCall {
	public String name; //姓名
	public String mobileNo; //移动号码
	public NgnMediaType	mediatype; //呼叫类型,
	public long callstarttime; //来电/呼叫开始时间
	public long starttime; //通话接通开始时间
	public long endtime; //通话结束时间
	public StatusType status; //
	public int sessionType; //通话类型
	public NgnHistoryEvent mEvent;
}
