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

public class SksFunctionList extends Fragment {

	private ListView funclist = null;

	private FuncListAdaptor funcListAdaptor = null;

	private String[] functions = null;

	private Context mContext = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = SKDroid.getContext();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		functions = mContext.getResources().getStringArray(R.array.functions);

		funclist = (ListView) inflater.inflate(R.layout.functionlist, null);
		funcListAdaptor = new FuncListAdaptor(functions);
		funclist.setAdapter(funcListAdaptor);

		return funclist;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	class FuncListAdaptor extends BaseAdapter {

		private String[] funcs = null;

		public FuncListAdaptor(String[] funcs) {
			if (funcs == null) {
				this.funcs = new String[0];
			} else {
				this.funcs = funcs;
			}
		}

		@Override
		public int getCount() {
			return funcs.length;
		}

		@Override
		public Object getItem(int position) {
			return funcs[position];
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
				item_num.setText("" + (position + 1));
				item_content.setText(funcs[position]);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return view;
		}

	}

}
