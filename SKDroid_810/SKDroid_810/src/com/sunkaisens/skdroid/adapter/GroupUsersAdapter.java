package com.sunkaisens.skdroid.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.model.ModelContact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GroupUsersAdapter extends BaseAdapter implements Observer {

	private List<ModelContact> users;
	public List<ModelContact> getUsers() {
		return users;
	}


	public void setUsers(List<ModelContact> users) {
		if(users == null){
			users = new ArrayList<ModelContact>();
		}
		this.users = users;
		notifyDataSetChanged();
	}

	private Context mContext;
	
	
	
	public GroupUsersAdapter(Context mContext) {
		super();
		this.mContext = mContext;
	}
	

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(users == null){
			return 0;
		}
		return users.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		if(users == null){
			return null;
		}
		return users.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		ModelContact model = users.get(position);
		if (model == null){
			return null;
		}
		LayoutInflater lf = LayoutInflater.from(mContext);
		if (convertView == null) {
			convertView = lf.inflate(R.layout.gav_onlineitem, null);
		} 
		
		ImageView item_icon = (ImageView) convertView.findViewById(R.id.item_icon);
		TextView item_name = (TextView) convertView.findViewById(R.id.item_name);
//		item_icon.setImageResource(SystemVarTools.getThumbID(model.imageid));
		
		SystemVarTools.showicon(item_icon, model, mContext);
		
		
		item_name.setText(model.name);
		convertView.setTag(model.mobileNo);
		
		return convertView;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

}
