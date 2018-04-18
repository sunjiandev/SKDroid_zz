package com.sunkaisens.skdroid.model;

public class ModelContactSubs {

	/**
	 * 当前用户被引用的次数 次数为0时，删除用户
	 */
	public int refCount = 0;

	/**
	 * 当前用户
	 */
	public ModelContact contact;

	/**
	 * 当前用户订阅关系sessionid
	 */
	public long subSessionId;

	@Override
	public boolean equals(Object o) {
		ModelContactSubs mcs = (ModelContactSubs) o;
		return this.contact.equals(mcs.contact);
	}

}
