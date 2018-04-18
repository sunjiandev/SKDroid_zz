package com.sunkaisens.skdroid.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.util.GlobalVar;

public class FeedbackBody {

	private String content;
	private String phone_num;
	private String user;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public FeedbackBody() {
	}

	public FeedbackBody(String content) {
		super();
		this.content = content;
		this.phone_num = GlobalVar.mLocalNum;
		this.user = SystemVarTools
				.createContactFromPhoneNumber(GlobalVar.mLocalNum).name;
		if (this.user == null) {
			this.user = "δ֪";
		}
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getPhone_num() {
		return phone_num;
	}

	public void setPhone_num(String phone_num) {
		this.phone_num = phone_num;
	}

	public String toJasonString() {

		JSONObject content = new JSONObject();
		try {
			content.put("feedbacker", this.user);
			content.put("phone", this.phone_num);
			content.put("info", this.content);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Log.d("Feedback", "content = " + content.toString());

		return content.toString();
	}

}
