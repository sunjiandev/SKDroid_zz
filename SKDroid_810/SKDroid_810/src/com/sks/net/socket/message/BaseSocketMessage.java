package com.sks.net.socket.message;

public class BaseSocketMessage {

	public static final int MSG_SOCKET_PORT = 31000;
	public static final int MSG_UNDEFINED = 0XFFFF;
	// 3.1.1. ������ָ�0x0000~003f��
	public static final int MSG_C_SET_SOUNDVOLUME = 0X0006; // ����������С
	public static final int MSG_C_SET_VIDEOQUALITY = 0X0005; // ������Ƶ������
	public static final int MSG_C_SET_CAMERA = 0X0007; // ��������ͷ
	public static final int MSG_C_SET_SOUNDMODE = 0X0008; // ���÷���ģʽ
	public static final int MSG_C_SET_CURRENTGROUP = 0X0015; // ���õ�ǰ��Ⱥ��
	// 3.1.2. ����������ָ�0x0080~0x00ff��
	public static final int MSG_S_UPDATE_SOUNDVOLUME = 0X0087; // �ϱ�������С
	public static final int MSG_S_UPDATE_VIDEOQUALITY = 0X008B; // �ϱ���Ƶ������
	public static final int MSG_S_UPDATE_CAMERA = 0X008D; // �ϱ�����ͷ״̬
	public static final int MSG_S_UPDATE_SOUNDMODE = 0X0088; // �ϱ�����ģʽ
	public static final int MSG_S_UPDATE_CURRENTGROUP = 0X0090; // �ϱ���ǰ��Ⱥ��
	// 3.2. ����������Ϣ��0x0300~0x03ff��
	public static final int MSG_C_INIT_OK = 0X0305; // ��ʼ�����
	public static final int MSG_S_USER_NEEDREG = 0X03EF; // �û���δע��
	public static final int MSG_C_USER_REG = 0X03F0; // �û�ע��
	public static final int MSG_S_USER_REGRESULT = 0X03F2; // �û�ע�᷵����Ϣ
	public static final int MSG_C_USER_UNREG = 0X03F3; // �û�ע��
	public static final int MSG_S_USER_UNREGRESULT = 0X03F4; // �û�ע��������Ϣ
	public static final int MSG_S_INIT_STATE = 0X03F1; // �ϱ��豸����״̬��Ϣ
	public static final int MSG_S_INIT_CONTACT = 0X0391; // �ϱ���ϵ���б���Ϣ
	public static final int MSG_S_INIT_GROUP = 0X0392; // �ϱ�������б���Ϣ
	// 3.3.1. ����ҵ��
	public static final int MSG_S_AUDIO_INCOMING = 0X1000; // ��������
	public static final int MSG_S_AUDIO_CANCEL = 0X1001; // �����������
	public static final int MSG_C_AUDIO_CALLOUT = 0X1002; // ��������
	public static final int MSG_S_AUDIO_CALLING = 0X1003; // ���ں���
	public static final int MSG_S_AUDIO_CALLFAILED = 0X1006; // ����ʧ��
	public static final int MSG_S_AUDIO_INCALL = 0X1007; // ��ʼͨ��
	public static final int MSG_C_AUDIO_PICK = 0X1005; // ����ժ��
	public static final int MSG_C_AUDIO_HANGUP = 0X1004; // �����һ�
	public static final int MSG_S_AUDIO_TERMINATED = 0X1008; // ͨ���Ͽ�
	// 3.3.2. ��Ƶҵ��
	public static final int MSG_S_VIDEO_INCOMING = 0X2000; // ��Ƶ����
	public static final int MSG_S_VIDEO_CANCEL = 0X2001; // ��Ƶ�������
	public static final int MSG_C_VIDEO_CALLOUT = 0X2002; // ��Ƶ����
	public static final int MSG_S_VIDEO_CALLING = 0X2003; // ���ں���
	public static final int MSG_S_VIDEO_CALLFAILED = 0X2006; // ����ʧ��
	public static final int MSG_S_VIDEO_INCALL = 0X2007; // ��ʼ��Ƶ
	public static final int MSG_C_VIDEO_PICK = 0X2005; // ����ժ��
	public static final int MSG_C_VIDEO_HANGUP = 0X2004; // �����һ�
	public static final int MSG_S_VIDEO_TERMINATED = 0X2008; // ��Ƶ�Ͽ�
	public static final int MSG_S_VIDEO_STATE = 0X0385; // �ϱ�ҵ��״̬��Ϣ
	// 3.3.3. ��Ⱥҵ��
	public static final int MSG_C_GROUP_CALLOUT = 0X3000; // PTT������Ⱥͨ��
	public static final int MSG_C_GROUP_TERMINATED = 0X3001; // PTT��������Ⱥͨ��
	public static final int MSG_C_GROUP_EXITED = 0X3003; // �˳����
	public static final int MSG_S_GROUP_STATE = 0X3002; // ���״̬
	public static final int MSG_S_GROUP_INCOMING = 0X3004; // �������
	// �������������
	public static final int MSG_C_NORMAL_GROUP_AUDIOCALL = 0x3600;// PTT������Ⱥ���������
	public static final int MSG_C_SUPER_GROUP_AUDIOCALL = 0x3601;// PTT������Ⱥ���������
	public static final int MSG_C_GROUP_AUDIOEXITED = 0X3602; // �˳����
	// 3.3.4. ����Ϣ
	public static final int MSG_C_S_SMS_RESULT = 0X4000; // ACKS(����Ϣ���ͽ��-��/����)
	public static final int MSG_C_S_SMS_FORMAT = 0X4001; // ����Ϣ��ʽ(��/����)
	// 3.3.5. ������
	public static final int MSG_C_ADHOC_LOGIN = 0X5000; // ������ע��
	public static final int MSG_C_ADHOC_UNLOGIN = 0X5001; // ������ע��
	// public static final int MSG_S_ADHOC_UPDATE_CONTACT = 0X5002;
	// //�������ϱ���ϵ���б���Ϣ
	// 3.3.6. ģ����Ƶ���
	public static final int MSG_C_TVOUT_OPEN_WRITE = 0X6000; // ������豸
	public static final int MSG_C_TVOUT_CLOSE = 0X6001; // �ر�����豸

	// gzc ҵ������������������
	public static final int MSG_C_ALIVE_REQ = 0X7000; // ҵ����������������
	public static final int MSG_S_ALIVE_RES = 0X7001; // ҵ��������������Ӧ
	public static final int MSG_C_TVOUT_SET = 0X7002; // ������Ƶ���Դ
	public static final int MSG_S_TVOUT_REPORT = 0X7003; // �ϱ���Ƶ���Դ

	// ������֪ͨ��Ϣ
	public static final int MSG_S_RING_NOTIF = 0X8001;

	public static final int MSG_C_GIS_REQUEST = 0X0100; // GIS��Ϣ������Ϣ
	public static final int MSG_S_GIS_RESPONSE = 0X0101; // GIS��Ϣ��Ӧ��Ϣ

	public static final int MSG_C_IP_CHANGE = 0X0200; // WAN��ip�����Ϣ

	public static final int MSG_S_IP_CHANGE = 0X0201; // �ɵ�¼�󷵻���Ϣ

	// Ĭ�ϵ�ǰ��Ⱥ��洢����
	public final static String SHARED_PREF_GROUP = "CURRENT_GROUP";
	public final static String DEFAULT_CURRENT_GROUP = "DEFAULT_CURRENT_GROUP";

	// Ĭ�ϵ�ǰ��Ⱥ��
	public static final String DEFAULT_CURRENT_GROUPNO = "110";

	// ��Ϣ����
	private int msgType = MSG_UNDEFINED;
	// ������
	private byte[] data = null;

	//

	public BaseSocketMessage() {

	}

	public BaseSocketMessage(int type) {
		msgType = type;
	}

	public BaseSocketMessage(int type, byte[] indata) {
		msgType = type;
		data = indata;
	}

	public int getType() {
		return msgType;
	}

	public byte[] getData() {
		return data;
	}

	//
	public void setType(int type) {
		msgType = type;
	}

	public void setData(byte[] indata) {
		data = indata;
	}

	public String description() {
		String description = "type=0x"
				+ MessageTools.bytes2HexString(MessageTools.intToBytes(msgType,
						2));
		description += ",";
		if (data != null)
			description += "length=" + data.length;
		else
			description += "length=" + 0;
		description += ",";
		if (data != null) {
			description += "data=0x" + MessageTools.bytes2HexString(data);
		}
		return description;
	}

}
