package com.sunkaisens.skdroid.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;

public class SksUpdateTipsList extends Fragment {

	private ListView buglist = null;

	private UpdateTipsAdaptor updateTipsAdaptor = null;

	private String[] bugs = null;

	private Context mContext = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = SKDroid.getContext();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		bugs = mContext.getResources().getStringArray(R.array.update_tips);

		buglist = (ListView) inflater.inflate(R.layout.update_view, null);
		updateTipsAdaptor = new UpdateTipsAdaptor(bugs);
		buglist.setAdapter(updateTipsAdaptor);

		return buglist;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	class UpdateTipsAdaptor extends BaseAdapter {

		private String[] UpdateTips = null;

		public UpdateTipsAdaptor(String[] funcs) {
			if (funcs == null) {
				this.UpdateTips = new String[0];
			} else {
				this.UpdateTips = funcs;
			}
		}

		@Override
		public int getCount() {
			return UpdateTips.length;
		}

		@Override
		public Object getItem(int position) {
			return UpdateTips[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View view = null;
			if (convertView == null) {
				view = LayoutInflater.from(mContext).inflate(
						R.layout.func_list_item, null);
			} else {
				view = convertView;
			}

			try {
				TextView item_num = (TextView) view
						.findViewById(R.id.func_item_num);
				TextView item_content = (TextView) view
						.findViewById(R.id.func_item_content);
				item_num.setText("" + (position + 1) + ".");
				item_content.setText(UpdateTips[position]);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return view;
		}

	}

}
