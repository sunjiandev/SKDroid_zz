package com.sunkaisens.skdroid.model;

import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.model.NgnHistoryEvent.StatusType;

public class ModelCall {
	public String name; //����
	public String mobileNo; //�ƶ�����
	public NgnMediaType	mediatype; //��������,
	public long callstarttime; //����/���п�ʼʱ��
	public long starttime; //ͨ����ͨ��ʼʱ��
	public long endtime; //ͨ������ʱ��
	public StatusType status; //
	public int sessionType; //ͨ������
	public NgnHistoryEvent mEvent;
}
