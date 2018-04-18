package com.sunkaisens.skdroid.Screens;

import org.doubango.utils.MyLog;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.fragments.SksBugList;
import com.sunkaisens.skdroid.fragments.SksFunctionList;
import com.sunkaisens.skdroid.fragments.SksUpdateTipsList;

public class ScreenAboutExpanded extends BaseScreen {

	public ScreenAboutExpanded() {
		super(BaseScreen.SCREEN_TYPE.ABOUT_EXPANDED_T, TAG);
	}

	private static final String TAG = ScreenAboutExpanded.class
			.getCanonicalName();

	private FragmentManager fm = null;

	private FragmentTransaction ft = null;

	private TextView screen_desc = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screen_function);

		String screen_id = getIntent().getStringExtra("id");
		MyLog.d(TAG, "ScreenId = " + screen_id);
		screen_desc = (TextView) findViewById(R.id.screen_desc);

		if (fm == null) {
			fm = getFragmentManager();
		}
		ft = fm.beginTransaction();
		if (screen_id.equals("funclist")) {

			ft.replace(R.id.about_fragment_parent, new SksFunctionList());
			ft.commit();
			screen_desc.setText(getResources().getString(R.string.func_desc));
		} else if (screen_id.equals("buglist")) {

			ft.replace(R.id.about_fragment_parent, new SksBugList());
			ft.commit();
			screen_desc.setText(getResources().getString(R.string.bug_list));
		} else if (screen_id.equals("updatelist")) {
			ft.replace(R.id.about_fragment_parent, new SksUpdateTipsList());
			ft.commit();
			screen_desc.setText(getResources().getString(R.string.update_list));
		}

		ImageView back = (ImageView) findViewById(R.id.back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.back();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.screen_function, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
