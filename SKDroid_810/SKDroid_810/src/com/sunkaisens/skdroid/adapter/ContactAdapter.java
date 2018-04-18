package com.sunkaisens.skdroid.adapter;

import java.util.List;

import org.doubango.utils.MyLog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Services.ServiceContact;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.util.GlobalVar;

public class ContactAdapter extends BaseAdapter {

	private String TAG = ContactAdapter.class.getCanonicalName();

	private int num = 0;

	private Context mContext;
	private List<ModelContact> contactList;

	// public static String accountString = null;

	public ContactAdapter(Context c, List<ModelContact> list) {
		MyLog.d(TAG, "ContactAdapter()");
		mContext = c;
		// try {
		// Thread.sleep(100);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		contactList = list;
		if (contactList == null || contactList.size() == 0) {
			return;
		}
	}

	public void setList(List<ModelContact> list) {
		// try {
		// Thread.sleep(100);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		num++;
		if (contactList == null || position > contactList.size())
			return null;
		ModelContact model = contactList.get(position);
		// MyLog.d(TAG, "ModelAAA¡¾"+model.mobileNo+","+model.name+"¡¿");

		if (ServiceContact.mContactAll.keySet().contains(model.mobileNo)) {
			model.isOnline = ServiceContact.mContactAll.get(model.mobileNo).contact.isOnline;
		} else {
			model.isOnline = false;
		}
		RelativeLayout layout;
		ImageView imageView;
		TextView textView;
		TextView isonlineTextView;

		if (convertView == null) {
			layout = (RelativeLayout) LayoutInflater.from(mContext).inflate(
					R.layout.contact_list_item, parent, false);
		} else {
			layout = (RelativeLayout) convertView;
		}

		imageView = (ImageView) layout.findViewById(R.id.contact_item_image);

		SystemVarTools.showicon(imageView, model, mContext);

		textView = (TextView) layout.findViewById(R.id.contact_item_name);

		isonlineTextView = (TextView) layout
				.findViewById(R.id.contact_item_isonline);

		View lineView = (View) layout
				.findViewById(R.id.contact_list_item_bottom_line);

		if (contactList.size() == 1) {
			lineView.setVisibility(View.GONE);
		} else {
			lineView.setVisibility(View.VISIBLE);
		}

		textView.setText(model.name);
		textView.setSelected(true);
		if (GlobalVar.bADHocMode) {
			model.isOnline = true;
			textView.setText(model.name + "\n["
					+ SystemVarTools.getIPFromUri(model.uri) + "]");
		}
		if (!model.isgroup) {
			isonlineTextView.setVisibility(View.VISIBLE);
			if (!model.isOnline) {
				imageView.setAlpha(90);
				isonlineTextView.setText(mContext
						.getString(R.string.offline_with_bracket));
			} else {
				imageView.setAlpha(255);
				isonlineTextView.setText(mContext
						.getString(R.string.online_with_bracket));
			}

		} else {
			isonlineTextView.setVisibility(View.GONE);
			imageView.setAlpha(255);
		}
		return layout;
	}

}