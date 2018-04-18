package com.sunkaisens.skdroid.Screens;

import org.doubango.ngn.utils.NgnConfigurationEntry;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Utils.FileHttpUpLoadClient;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.model.FeedbackBody;

public class ScreenFeedback extends Activity {

	private EditText feedback_body;
	private Button feedback_commit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_feedback);

		ImageView back = (ImageView) findViewById(R.id.screen_feedback_back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// IScreenService mScreenService = ((Engine)
				// Engine.getInstance()).getScreenService();
				// mScreenService.back();
				finish();
			}
		});

		feedback_body = (EditText) findViewById(R.id.feedback_body);
		feedback_commit = (Button) findViewById(R.id.feedback_commit);

		feedback_commit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String content = feedback_body.getText().toString();

				if (content.equals("")) {
					SystemVarTools.showToast(ScreenFeedback.this
							.getString(R.string.feedback_cannot_be_null));
					return;
				}
				FeedbackBody fb = new FeedbackBody(content);

				String groupIp = Engine
						.getInstance()
						.getConfigurationService()
						.getString(
								NgnConfigurationEntry.NETWORK_GROUP_REALM,
								NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM);

				String uri = "http://" + groupIp + ":8080/SKDBUG/bugadd.action";

				// Log.d("", "BUG uri="+uri);

				Handler handler = new Handler() {

					@Override
					public void handleMessage(Message msg) {
						// TODO Auto-generated method stub
						if (msg.what == 9000) {
							Log.d("", "BUG 问题发送成功");
							SystemVarTools.showToast(ScreenFeedback.this
									.getString(R.string.feedback_thank_for_help));
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							finish();
						} else if (msg.what == 9001) {
							Log.d("", "BUG 问题发送失败");
							SystemVarTools.showToast(ScreenFeedback.this
									.getString(R.string.feedback_send_failed));
						} else if (msg.what == 9002) {
							Log.d("", "BUG 服务器连接失败");
							SystemVarTools.showToast(ScreenFeedback.this
									.getString(R.string.feedback_srever_connect_failed));
						}
					}

				};

				FileHttpUpLoadClient.httpSendFeedbackBodyNotJason(uri,
						fb.toJasonString(), handler);
			}
		});

	}

}
