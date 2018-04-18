package com.sunkaisens.skdroid.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnHistoryAVCallEvent.HistoryEventAVFilter;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.model.NgnHistoryEvent.StatusType;
import org.doubango.ngn.utils.NgnUriUtils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.R.id;
import com.sunkaisens.skdroid.Screens.ScreenOrgInfo;
import com.sunkaisens.skdroid.Screens.ScreenPersonInfo;
import com.sunkaisens.skdroid.Screens.ScreenTabCall;
import com.sunkaisens.skdroid.Services.IServiceScreen;
import com.sunkaisens.skdroid.Services.ServiceAV;
import com.sunkaisens.skdroid.Utils.DateTimeUtils;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.model.ModelCall;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.session.SessionType;
import com.sunkaisens.skdroid.util.GlobalVar;

public class CallAdapter extends BaseAdapter implements Observer {
	protected final IServiceScreen ss = ((Engine) Engine.getInstance())
			.getScreenService();
	private List<ModelCall> list;
	private ScreenTabCall mBaseScreen;
	private List<NgnHistoryEvent> mEvents;
	private final Handler mHandler;

	private String searchNum = "";

	private final static int TYPE_ITEM_AV = 0;
	private final static int TYPE_ITEM_SMS = 1;
	private final static int TYPE_ITEM_FILE_TRANSFER = 2;
	private final static int TYPE_COUNT = 3;

	public CallAdapter(ScreenTabCall c) {
		mBaseScreen = c;
		list = new ArrayList<ModelCall>();
		mHandler = new Handler();
		mEvents = mBaseScreen.mHistorytService.getObservableEvents().filter(
				new HistoryEventAVFilter());
		mBaseScreen.mHistorytService.getObservableEvents().addObserver(this);
		updateEvents2List();
	}

	public void setSearchKey(String key) {
		searchNum = key;
		mEvents = mBaseScreen.mHistorytService.getObservableEvents().filter(
				new HistoryEventAVFilter());
		updateEvents2List();
		notifyDataSetChanged();
	}

	private void updateEvents2List() {
		list.clear();
		if (mEvents.size() <= 0) {
			return;
		}
		for (int i = 0; i < mEvents.size(); i++) {
			/*
			 * String localNum = mEvents.get(i).getLocalParty(); String
			 * myLocalNum = SystemVarTools.getLocalParty();
			 * if(!localNum.equals(myLocalNum)) continue;
			 */

			if (searchNum != null && searchNum.length() > 0) {
				if (NgnUriUtils.getUserName(mEvents.get(i).getRemoteParty())
						.indexOf(searchNum) >= 0) {
					ModelCall mc = new ModelCall();
					mc.mediatype = mEvents.get(i).getMediaType();
					mc.mobileNo = NgnUriUtils.getUserName(mEvents.get(i)
							.getRemoteParty());
					mc.name = mEvents.get(i).getDisplayName();
					mc.callstarttime = mEvents.get(i).getmCallStartTime();
					mc.starttime = mEvents.get(i).getStartTime();
					mc.endtime = mEvents.get(i).getEndTime();
					mc.status = mEvents.get(i).getStatus();
					mc.sessionType = mEvents.get(i).getSessionType();
					mc.mEvent = mEvents.get(i);
					list.add(mc);
				}
			} else {
				ModelCall mc = new ModelCall();
				mc.mediatype = mEvents.get(i).getMediaType();
				mc.mobileNo = NgnUriUtils.getUserName(mEvents.get(i)
						.getRemoteParty());
				mc.name = mEvents.get(i).getDisplayName();
				mc.callstarttime = mEvents.get(i).getmCallStartTime();
				mc.starttime = mEvents.get(i).getStartTime();
				mc.endtime = mEvents.get(i).getEndTime();
				mc.status = mEvents.get(i).getStatus();
				mc.sessionType = mEvents.get(i).getSessionType();
				mc.mEvent = mEvents.get(i);
				list.add(mc);
			}

		}

		// SystemVarTools.setCallList(list);
		// SystemVarTools.updateContactRecent();
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

	private String getMediaTypeShow(NgnMediaType type, StatusType status,
			int sessionType, ModelContact mc) {
		String showStr;
		switch (type) {
		case Audio:
			if (sessionType == SessionType.GroupAudioCall) {
				showStr = ""
						+ mBaseScreen.getText(R.string.call_desc_group_audio);
			} else {
				showStr = "" + mBaseScreen.getText(R.string.call_desc_audio);
			}
			break;
		case AudioVideo:
			if (sessionType == SessionType.GroupVideoCall) {
				showStr = ""
						+ mBaseScreen.getText(R.string.call_desc_group_video);
			} else if (sessionType == SessionType.VideoMonitor) {
				showStr = ""
						+ mBaseScreen.getText(R.string.call_desc_video_monitor);
			} else if (sessionType == SessionType.VideoTransmit) {
				showStr = ""
						+ mBaseScreen
								.getText(R.string.call_desc_video_Transmint);
			} else if (sessionType == SessionType.VideoUaMonitor) {
				showStr = ""
						+ mBaseScreen.getText(R.string.call_desc_video_back);
			} else {
				showStr = "" + mBaseScreen.getText(R.string.call_desc_video);
			}
			break;
		default:
			showStr = "" + mBaseScreen.getText(R.string.call_desc_unknown);
		}

		switch (status) {
		case Outgoing:
			showStr += " " + mBaseScreen.getText(R.string.call_desc_outgoing);
			// ivType.setImageResource(R.drawable.call_outgoing_45);
			break;
		case Incoming:
			showStr += " " + mBaseScreen.getText(R.string.call_desc_incoming);
			// ivType.setImageResource(R.drawable.call_incoming_45);
			break;
		case Failed:
		case Missed:
			showStr += " "
					+ mBaseScreen.getText(R.string.call_desc_missed_call);
			// ivType.setImageResource(R.drawable.call_missed_45);
			break;
		}
		return showStr;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (list == null || position > list.size())
			return null;

		RelativeLayout layout;
		ModelCall model = list.get(position);
		if (model == null)
			return null;

		if (convertView == null) {
			layout = (RelativeLayout) LayoutInflater.from(mBaseScreen).inflate(
					R.layout.call_list_item, parent, false);
		} else {
			layout = (RelativeLayout) convertView;
		}
		//
		TextView name = (TextView) layout.findViewById(id.itemname);
		TextView text = (TextView) layout.findViewById(id.itemtext);
		TextView itemtime = (TextView) layout.findViewById(id.itemtime);
		ImageView image = (ImageView) layout.findViewById(id.itemimage);
		ImageButton button = (ImageButton) layout.findViewById(id.itembutton);
		TextView iteminterval = (TextView) layout.findViewById(id.iteminterval);
		ModelContact contact = SystemVarTools
				.getContactFromPhoneNumber(model.mobileNo);
		if (contact != null) {
			name.setText(contact.name);
		} else
			name.setText(model.name);

		name.setSelected(true);

		final String date = DateTimeUtils.getFriendlyDateString(new Date(
				model.callstarttime));
		itemtime.setText(date);
		iteminterval.setText(""
				+ mBaseScreen.getText(R.string.call_desc_time)
				+ SystemVarTools
						.mCallPeriodFormat((model.endtime - model.starttime)));
		ModelContact mc = SystemVarTools
				.createContactFromPhoneNumber(model.mobileNo);

		text.setText(getMediaTypeShow(model.mediatype, model.status,
				model.sessionType, mc));
		if (mc != null) {
			// image.setImageResource(SystemVarTools.getThumbID(mc.imageid));
			SystemVarTools.showicon(image, mc,
					mBaseScreen.getApplicationContext());
		} else
			image.setImageResource(SystemVarTools.getThumbID(0));

		if (model.mediatype == NgnMediaType.AudioVideo) {
			switch (model.status) {
			case Outgoing:
				button.setImageResource(R.drawable.call_video_outgoing);
				break;
			case Incoming:
				button.setImageResource(R.drawable.call_video_incoming);
				break;
			case Failed:
				button.setImageResource(R.drawable.call_video_failed);
				break;
			case Missed:
				button.setImageResource(R.drawable.call_video_missed);
				break;
			}
			button.setPadding(0, 0, 0, 0);
		} else {
			switch (model.status) {
			case Outgoing:
				button.setImageResource(R.drawable.call_audio_outgoing);
				break;
			case Incoming:
				button.setImageResource(R.drawable.call_audio_incoming);
				break;
			case Failed:
				button.setImageResource(R.drawable.call_audio_failed);
				break;
			case Missed:
				button.setImageResource(R.drawable.call_audio_missed);
				break;
			}
			button.setPadding(7, 7, 7, 7);
		}

		layout.setTag(model);
		button.setTag(position);
		//
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int pos = (Integer) v.getTag();
				ModelCall model = list.get(pos);
				if (model != null) {
					// Toast.makeText(mBaseScreen,
					// "start button action with "+"phone:" + model.name,
					// Toast.LENGTH_LONG).show();
					ServiceAV.makeCall(model.mobileNo, model.mediatype,
							model.sessionType);
				}
			}
		});

		layout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ModelCall model = (ModelCall) v.getTag();
				if (model != null) {
					// Toast.makeText(mBaseScreen,
					// "start button action with "+"list:" + model.name,
					// Toast.LENGTH_LONG).show();
					ModelContact mc = SystemVarTools
							.getContactFromPhoneNumber(model.mobileNo);
					if (mc != null && mc.isgroup) {
						if (ss.show(ScreenOrgInfo.class, model.mobileNo)) {
							/*
							 * final IBaseScreen screen =
							 * ss.getScreen(ScreenOrgInfo.TAG); if (screen
							 * instanceof ScreenOrgInfo) { ((ScreenOrgInfo)
							 * screen).updateInfo(model.mobileNo); }
							 */
						}
					} else {
						if (GlobalVar.bADHocMode) {
							ModelContact info = SystemVarTools
									.createContactFromRemoteParty(model.mobileNo);
							if (info.uri == null) {
								Log.d("CallApapter",
										"Sorry, you can't call anyone who is out of your contactList!");
								return;
							}
						}
						if (ss.show(ScreenPersonInfo.class, model.mobileNo)) {
							/*
							 * final IBaseScreen screen =
							 * ss.getScreen(ScreenPersonInfo.TAG); if (screen
							 * instanceof ScreenPersonInfo) {
							 * ((ScreenPersonInfo)
							 * screen).updateInfo(model.mobileNo); }
							 */
						}
					}
				}
			}
		});
		mBaseScreen.registerForContextMenu(layout);

		return layout;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		mBaseScreen.mHistorytService.getObservableEvents().deleteObserver(this);
	}

	@Override
	public int getViewTypeCount() {
		return TYPE_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		final ModelCall event = (ModelCall) getItem(position);
		if (event != null) {
			switch (event.mediatype) {
			case Audio:
			case AudioVideo:
			default:
				return TYPE_ITEM_AV;
			case FileTransfer:
				return TYPE_ITEM_FILE_TRANSFER;
			case SMS:
				return TYPE_ITEM_SMS;
			}
		}
		return TYPE_ITEM_AV;
	}

	@Override
	public void update(Observable observable, Object data) {
		mEvents = mBaseScreen.mHistorytService.getObservableEvents().filter(
				new HistoryEventAVFilter());

		updateEvents2List();
		if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
			notifyDataSetChanged();
		} else {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					notifyDataSetChanged();
				}
			});
		}
	}

}