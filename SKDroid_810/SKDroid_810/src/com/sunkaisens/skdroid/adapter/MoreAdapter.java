package com.sunkaisens.skdroid.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.R.id;

public class MoreAdapter extends BaseAdapter {
	private Context mContext;
	private List<String> list;

	public MoreAdapter(Context c, List<String> inlist) {
		mContext = c;
		list = inlist;
	}

	public void setList(List<String> inlist) {
		list = inlist;
	}

	@Override
	public int getCount() {
		if (list == null)
			return 0;
		else
			return list.size();
	}

	@Override
	public Object getItem(int position) {
		if (list == null)
			return null;
		else
			return list.get(position);
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
		if (list == null || position > list.size())
			return null;

		RelativeLayout layout;
		String name = list.get(position);
		if (name == null)
			return null;
		if (convertView == null) {
			layout = (RelativeLayout) LayoutInflater.from(mContext).inflate(
					R.layout.more_list_item, parent, false);
		} else {
			layout = (RelativeLayout) convertView;
		}
		//
		TextView itemname = (TextView) layout.findViewById(id.itemname);
		itemname.setText(name);
		//
		/*
		 * layout.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { Toast.makeText(mContext,
		 * "start chat action with "+"test", Toast.LENGTH_LONG).show();
		 * 
		 * } });;
		 */

		return layout;
	}

}