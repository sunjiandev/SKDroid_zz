package com.sunkaisens.skdroid.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.R.id;
import com.sunkaisens.skdroid.model.ModelMessagerList;

public class MessageAdapter extends BaseAdapter {
	private Context mContext;
	private List<ModelMessagerList> list;

	public MessageAdapter(Context c, List<ModelMessagerList> inlist) {
		mContext = c;
		list = inlist;
	}

	public void setList(List<ModelMessagerList> inlist) {
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
		ModelMessagerList model = list.get(position);
		if (model == null)
			return null;
		int px = dip2px(parent.getContext(), 80);
		if (convertView == null) {
			layout = (RelativeLayout) LayoutInflater.from(mContext).inflate(
					R.layout.messager_list_item, parent, false);
		} else {
			layout = (RelativeLayout) convertView;
		}
		//
		TextView name = (TextView) layout.findViewById(id.itemname);
		TextView text = (TextView) layout.findViewById(id.itemtext);
		ImageView image = (ImageView) layout.findViewById(id.itemimage);

		name.setTextColor(mContext.getResources().getColor(
				R.color.color_titleblack));
		name.setText(model.name);
		text.setText(model.brief);
		image.setImageResource(mThumbIds[model.imageid]);
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

	private Integer[] mThumbIds = { R.drawable.n_image_icon1,
			R.drawable.n_image_icon2, R.drawable.n_image_icon3,
			R.drawable.n_image_icon4, R.drawable.n_image_icon5,
			R.drawable.n_image_icon6, R.drawable.n_image_icon7,
			R.drawable.n_image_icon8, R.drawable.n_image_icon9,
			R.drawable.n_image_icon10, R.drawable.n_image_icon11,
			R.drawable.n_image_icon12, R.drawable.n_image_icon13,
			R.drawable.n_image_icon14, };

}