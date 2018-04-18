package com.sunkaisens.skdroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sunkaisens.skdroid.R;

public class AbcListAdapter extends BaseAdapter {

	private Context mContext;

	private String[] abcs;

	private int Viewheight = 20;

	public AbcListAdapter(Context context, String[] list, int height) {
		mContext = context;
		abcs = list;

		Viewheight = height;

	}

	@Override
	public int getCount() {
		return abcs.length;
	}

	@Override
	public Object getItem(int pos) {
		return abcs[pos];
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int pos, View view, ViewGroup arg2) {
		if (view == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			view = inflater.inflate(R.layout.abclist_item, null);
		}

		// view.setLayoutParams(new ViewGroup.LayoutParams(30,Viewheight));

		TextView tv = (TextView) view.findViewById(R.id.abc);
		tv.setHeight(Viewheight);

		// tv.setTextSize(Viewheight);
		tv.setText(abcs[pos]);
		return view;
	}

}
