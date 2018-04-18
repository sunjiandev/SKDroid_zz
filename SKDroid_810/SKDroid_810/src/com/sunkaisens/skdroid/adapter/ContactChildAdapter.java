package com.sunkaisens.skdroid.adapter;

import java.util.List;

import org.doubango.utils.MyLog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Services.ServiceContact;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.util.GlobalVar;

public class ContactChildAdapter extends BaseAdapter {

	private String TAG = ContactChildAdapter.class.getCanonicalName();

	private int num = 0;

	private Context mContext;
	private List<ModelContact> contactList;

	public ContactChildAdapter(Context c, List<ModelContact> list) {

		mContext = c;
		contactList = list;
	}

	public void setList(List<ModelContact> list) {
		contactList = list;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (contactList == null)
			return 0;
		else
			return contactList.size();
	}

	@Override
	public Object getItem(int position) {
		if (contactList == null)
			return null;
		else
			return contactList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		num++;
		if (contactList == null || position > contactList.size())
			return null;

		ImageView imageView;
		TextView userName;
		TextView isOnlineTextView;
		LinearLayout layout;
		ModelContact model = contactList.get(position);
		if (model == null)
			return null;

		if (ServiceContact.mContactAll.keySet().contains(model.mobileNo)) {
			model.isOnline = ServiceContact.mContactAll.get(model.mobileNo).contact.isOnline;
		} else {
			model.isOnline = false;
		}

		if (convertView == null) {
			layout = (LinearLayout) LayoutInflater.from(mContext).inflate(
					R.layout.contact_child_list_item, parent, false);
		} else {
			layout = (LinearLayout) convertView;
		}

		imageView = (ImageView) layout
				.findViewById(R.id.contact_child_item_image);
		SystemVarTools.showicon(imageView, model, mContext);

		userName = (TextView) layout.findViewById(R.id.contact_child_item_name);
		userName.setText(model.name);
		userName.setSelected(true);

		isOnlineTextView = (TextView) layout
				.findViewById(R.id.contact_child_item_isonline);

		if (GlobalVar.bADHocMode) {
			model.isOnline = true;
			// t.setText(model.name+"\n["+SystemVarTools.getIPFromUri(model.uri)+"]");
			userName.setText(model.name + "\n[" + model.index + "]");
		}

		MyLog.d(TAG, "名称" + model.name);
		MyLog.d(TAG, "在线" + model.isOnline);
		MyLog.d(TAG, "为组" + model.isgroup);

		if ((!model.isOnline) && (!model.isgroup))// .name.contains("组")
		{

			imageView.setAlpha(90);
			if (GlobalVar.bADHocMode) {
				userName.setText(model.name + "\n[" + model.index + "]");
			} else {
				userName.setText(model.name);
				isOnlineTextView.setText(mContext
						.getString(R.string.offline_with_bracket));
			}
		}

		if (model.isOnline && (!model.isgroup))// .name.contains("组")
		{
			imageView.setAlpha(255);

			if (GlobalVar.bADHocMode) {
				userName.setText(model.name + "\n[" + model.index + "]");
			} else {
				userName.setText(model.name);
				isOnlineTextView.setText(mContext
						.getString(R.string.online_with_bracket));
			}
		}

		if (model.isgroup) {
			isOnlineTextView.setVisibility(View.GONE);
		} else {
			isOnlineTextView.setVisibility(View.VISIBLE);
		}

		return layout;
	}

}