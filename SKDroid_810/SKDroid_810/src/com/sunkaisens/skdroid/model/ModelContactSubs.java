package com.sunkaisens.skdroid.model;

public class ModelContactSubs {

	/**
	 * ��ǰ�û������õĴ��� ����Ϊ0ʱ��ɾ���û�
	 */
	public int refCount = 0;

	/**
	 * ��ǰ�û�
	 */
	public ModelContact contact;

	/**
	 * ��ǰ�û����Ĺ�ϵsessionid
	 */
	public long subSessionId;

	@Override
	public boolean equals(Object o) {
		ModelContactSubs mcs = (ModelContactSubs) o;
		return this.contact.equals(mcs.contact);
	}

}
