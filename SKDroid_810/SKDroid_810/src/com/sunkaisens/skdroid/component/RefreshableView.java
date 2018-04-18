package com.sunkaisens.skdroid.component;

import org.doubango.utils.MyLog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sunkaisens.skdroid.R;

public class RefreshableView extends LinearLayout implements OnTouchListener {

	private static String TAG = RefreshableView.class.getCanonicalName();
	/**
	 * ����״̬
	 */
	public static final int STATUS_PULL_TO_REFRESH = 0;
	/**
	 * �ͷ�����ˢ��״̬
	 */
	public static final int STATUS_RELEASE_TO_REFRESH = 1;
	/**
	 * ����ˢ��״̬
	 */
	public static final int STATUS_REFRESHING = 2;
	/**
	 * ˢ����ɻ�δˢ��״̬
	 */
	public static final int STATUS_REFRESH_FINISHED = 3;
	/**
	 * ����ͷ���ع����ٶ�
	 */
	public static final int SCROLL_SPEED = -20;
	/**
	 * һ���ӵĺ���ֵ�������ж��ϴεĸ���ʱ��
	 */
	public static final long ONE_MINUTE = 60 * 1000;
	/**
	 * һСʱ�ĺ���ֵ�������ж��ϴεĸ���ʱ��
	 */
	public static final long ONE_HOUR = 60 * ONE_MINUTE;
	/**
	 * һ��ĺ���ֵ�������ж��ϴεĸ���ʱ��
	 */
	public static final long ONE_DAY = 24 * ONE_HOUR;
	/**
	 * һ�µĺ���ֵ�������ж��ϴεĸ���ʱ��
	 */
	public static final long ONE_MONTH = 30 * ONE_DAY;
	/**
	 * һ��ĺ���ֵ�������ж��ϴεĸ���ʱ��
	 */
	public static final long ONE_YEAR = 12 * ONE_MONTH;
	/**
	 * �ϴθ���ʱ����ַ���������������ΪSharedPreferences�ļ�ֵ
	 */
	private static final String UPDATED_AT = "updated_at";
	/**
	 * ����ˢ�µĻص��ӿ�
	 */
	private PullToRefreshListener mListener;
	/**
	 * ���ڴ洢�ϴθ���ʱ��
	 */
	private SharedPreferences preferences;
	/**
	 * ����ͷ��View
	 */
	private View header;
	/**
	 * ��Ҫȥ����ˢ�µ�ListView
	 */
	private ListView listView;
	/**
	 * ˢ��ʱ��ʾ�Ľ�����
	 */
	private ProgressBar progressBar;
	/**
	 * ָʾ�������ͷŵļ�ͷ
	 */
	private ImageView arrow;
	/**
	 * ָʾ�������ͷŵ���������
	 */
	private TextView description;
	/**
	 * �ϴθ���ʱ�����������
	 */
	private TextView updateAt;
	/**
	 * ����ͷ�Ĳ��ֲ���
	 */
	private MarginLayoutParams headerLayoutParams;
	/**
	 * �ϴθ���ʱ��ĺ���ֵ
	 */
	private long lastUpdateTime;
	/**
	 * Ϊ�˷�ֹ��ͬ���������ˢ�����ϴθ���ʱ���ϻ����г�ͻ��ʹ��id��������
	 */
	private int mId = -1;
	/**
	 * ����ͷ�ĸ߶�
	 */
	private int hideHeaderHeight;
	/**
	 * ��ǰ����״̬
	 */
	private int currentStatus = STATUS_REFRESH_FINISHED;;
	/**
	 * ��¼��һ�ε�״̬��ʲô����������ظ�����
	 */
	private int lastStatus = currentStatus;
	/**
	 * ��ָ����ʱ����Ļ������
	 */
	private float yDown;
	/**
	 * �ڱ��ж�Ϊ����֮ǰ�û���ָ�����ƶ������ֵ
	 */
	private int touchSlop;
	/**
	 * �Ƿ��Ѽ��ع�һ��layout������onLayout�еĳ�ʼ��ֻ�����һ��
	 */
	private boolean loadOnce;
	/**
	 * ��ǰ�Ƿ����������ֻ��ListView������ͷ��ʱ�����������
	 */
	private boolean ableToPull;

	private Context mContext;

	/**
	 * ����ˢ�¿ؼ��Ĺ��캯������������ʱ��̬���һ������ͷ�Ĳ���
	 * 
	 * @param context
	 * @param attrs
	 */
	public RefreshableView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		header = LayoutInflater.from(context).inflate(R.layout.pull_to_refresh,
				null, true);
		progressBar = (ProgressBar) header.findViewById(R.id.progress_bar);
		arrow = (ImageView) header.findViewById(R.id.arrow);
		description = (TextView) header.findViewById(R.id.description);
		updateAt = (TextView) header.findViewById(R.id.updated_at);
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		refreshUpdatedAtValue();
		setOrientation(VERTICAL);
		addView(header, 0);
	}

	/**
	 * ����һЩ�ؼ��Եĳ�ʼ�����������磺������ͷ����ƫ�ƽ������أ���ListViewע��touch�¼�
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
		if (changed && !loadOnce) {
			hideHeaderHeight = -header.getHeight();
			headerLayoutParams = (LinearLayout.MarginLayoutParams) header
					.getLayoutParams();
			// headerLayoutParams = null;
			if (headerLayoutParams != null) {
				headerLayoutParams.topMargin = hideHeaderHeight;
			} else {
				MyLog.d(TAG, "headerLayoutParams == null");
				header.setVisibility(View.GONE);
			}
			listView = (ListView) getChildAt(1);
			listView.setOnTouchListener(this);
			loadOnce = true;
		}
	}

	int a = 0;

	/**
	 * ��ListView������ʱ���ã����д����˸�������ˢ�µľ����߼�
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		boolean isRunturn = false;
		a++;
		Log.i("ywh", "onTouch: " + a);
		long currentTime = System.currentTimeMillis();
		long timePassed = currentTime - lastUpdateTime;
		setIsAbleToPull(event);
		if (ableToPull && timePassed > (90 * 1000)) {// ����ˢ�¼��ʱ��
			if (ableToPull) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					Log.i("ywh", "onTouch --> ACTION_DOWN");
					isRunturn = true;
					updateHeaderView();
					yDown = event.getRawY();
					break;
				case MotionEvent.ACTION_MOVE:
					Log.i("ywh", "onTouch --> ACTION_MOVE");
					float yMove = event.getRawY();
					int distance = (int) (yMove - yDown);
					if (headerLayoutParams != null) {
						// �����ָ���»�״̬����������ͷ����ȫ���صģ������������¼�
						if (distance <= 0
								&& headerLayoutParams.topMargin <= hideHeaderHeight) {
							return false;
						}
						if (distance < touchSlop) {
							return false;
						}
						if (currentStatus != STATUS_REFRESHING) {
							if (headerLayoutParams.topMargin > 0) {
								currentStatus = STATUS_RELEASE_TO_REFRESH;
							} else {
								currentStatus = STATUS_PULL_TO_REFRESH;
							}
							// ͨ��ƫ������ͷ��topMarginֵ����ʵ������Ч��
							headerLayoutParams.topMargin = (distance / 2)
									+ hideHeaderHeight;
							header.setLayoutParams(headerLayoutParams);
						}
					} else {
						if (distance > 50) {
							header.setVisibility(View.VISIBLE);
						}
						if (currentStatus != STATUS_REFRESHING) {
							if (distance > 200) {
								currentStatus = STATUS_RELEASE_TO_REFRESH;
							} else {
								currentStatus = STATUS_PULL_TO_REFRESH;
							}

						}
						MyLog.d(TAG, "headerLayoutParams == null");
					}
					break;
				case MotionEvent.ACTION_UP:
					// if (headerLayoutParams == null) {
					// header.setVisibility(View.GONE);
					// }
					isRunturn = false;
					Log.i("ywh", "onTouch --> ACTION_UP");
					a = 0;
					// break;
					// default:
					if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
						// ����ʱ������ͷ�����ˢ��״̬����ȥ��������ˢ�µ�����
						new RefreshingTask().execute();
					} else if (currentStatus == STATUS_PULL_TO_REFRESH) {
						// ����ʱ���������״̬����ȥ������������ͷ������
						new HideHeaderTask().execute();
					}
					break;
				}
				// ʱ�̼ǵø�������ͷ�е���Ϣ
				if (currentStatus == STATUS_PULL_TO_REFRESH
						|| currentStatus == STATUS_RELEASE_TO_REFRESH) {
					updateHeaderView();
					// 1��ǰ�������������ͷ�״̬��Ҫ��ListViewʧȥ���㣬���򱻵������һ���һֱ����ѡ��״̬
					listView.setPressed(false);
					listView.setFocusable(false);
					listView.setFocusableInTouchMode(false);
					lastStatus = currentStatus;
					// ��ǰ�������������ͷ�״̬��ͨ������true���ε�ListView�Ĺ����¼�
					Log.i("ywh", "onTouch --> return true1;");
					return true;
				}
			}
		} else {
			if (ableToPull) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					Log.i("ywh", "onTouch --> ACTION_DOWN");
					isRunturn = true;
					noRefresh();
					yDown = event.getRawY();
					break;
				case MotionEvent.ACTION_MOVE:
					Log.i("ywh", "onTouch --> ACTION_MOVE");
					float yMove = event.getRawY();
					int distance = (int) (yMove - yDown);
					if (headerLayoutParams != null) {

						// �����ָ���»�״̬����������ͷ����ȫ���صģ������������¼�
						if (distance <= 0
								&& headerLayoutParams.topMargin <= hideHeaderHeight) {
							return false;
						}
						if (distance < touchSlop) {
							return false;
						}
						if (currentStatus != STATUS_REFRESHING) {
							if (headerLayoutParams.topMargin > 0) {
								currentStatus = STATUS_RELEASE_TO_REFRESH;
							} else {
								currentStatus = STATUS_PULL_TO_REFRESH;
							}
							// ͨ��ƫ������ͷ��topMarginֵ����ʵ������Ч��
							headerLayoutParams.topMargin = (distance / 2)
									+ hideHeaderHeight;
							header.setLayoutParams(headerLayoutParams);
						}
					} else {
						if (distance > 50) {
							header.setVisibility(View.VISIBLE);
						}
						if (currentStatus != STATUS_REFRESHING) {
							if (distance > 200) {
								currentStatus = STATUS_RELEASE_TO_REFRESH;
							} else {
								currentStatus = STATUS_PULL_TO_REFRESH;
							}

						}
						MyLog.d(TAG, "headerLayoutParams == null");
					}
					break;
				case MotionEvent.ACTION_UP:
					// if (headerLayoutParams == null) {
					// header.setVisibility(View.GONE);
					// }
					isRunturn = false;
					Log.i("ywh", "onTouch --> ACTION_UP");
					a = 0;
					// break;
					// default:
					if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
						// ����ʱ������ͷ�����ˢ��״̬����ȥ��������ˢ�µ�����
						// new RefreshingTask().execute();
						new HideHeaderTask().execute();
					} else if (currentStatus == STATUS_PULL_TO_REFRESH) {
						// ����ʱ���������״̬����ȥ������������ͷ������
						new HideHeaderTask().execute();
					}
					break;
				}
				// ʱ�̼ǵø�������ͷ�е���Ϣ
				if (currentStatus == STATUS_PULL_TO_REFRESH
						|| currentStatus == STATUS_RELEASE_TO_REFRESH) {
					noRefresh();
					// 2��ǰ�������������ͷ�״̬��Ҫ��ListViewʧȥ���㣬���򱻵������һ���һֱ����ѡ��״̬
					listView.setPressed(false);
					listView.setFocusable(false);
					listView.setFocusableInTouchMode(false);
					lastStatus = currentStatus;
					// ��ǰ�������������ͷ�״̬��ͨ������true���ε�ListView�Ĺ����¼�
					Log.i("ywh", "onTouch --> return true2;");
					return true;
				}
			}
		}
		Log.i("ywh", "onTouch --> return " + isRunturn + ";");
		return isRunturn;
	}

	/**
	 * ������ˢ�¿ؼ�ע��һ��������
	 * 
	 * @param listener
	 *            ��������ʵ��
	 * @param id
	 *            Ϊ�˷�ֹ��ͬ���������ˢ�����ϴθ���ʱ���ϻ����г�ͻ�� �벻ͬ������ע������ˢ�¼�����ʱһ��Ҫ���벻ͬ��id
	 */
	public void setOnRefreshListener(PullToRefreshListener listener, int id) {
		mListener = listener;
		mId = id;
	}

	/**
	 * �����е�ˢ���߼���ɺ󣬼�¼����һ�£��������ListView��һֱ��������ˢ��״̬
	 */
	public void finishRefreshing() {
		currentStatus = STATUS_REFRESH_FINISHED;
		preferences.edit()
				.putLong(UPDATED_AT + mId, System.currentTimeMillis()).commit();
		new HideHeaderTask().execute();
	}

	/**
	 * ���ݵ�ǰListView�Ĺ���״̬���趨 {@link #ableToPull}
	 * ��ֵ��ÿ�ζ���Ҫ��onTouch�е�һ��ִ�У����������жϳ���ǰӦ���ǹ���ListView������Ӧ�ý�������
	 * 
	 * @param event
	 */
	private void setIsAbleToPull(MotionEvent event) {
		View firstChild = listView.getChildAt(0);
		if (firstChild != null) {
			int firstVisiblePos = listView.getFirstVisiblePosition();
			if (firstVisiblePos == 0 && firstChild.getTop() == 0) {
				if (!ableToPull) {
					yDown = event.getRawY();
				}
				// ����׸�Ԫ�ص��ϱ�Ե�����븸����ֵΪ0����˵��ListView���������������ʱӦ����������ˢ��
				ableToPull = true;
			} else {
				if (headerLayoutParams.topMargin != hideHeaderHeight) {
					headerLayoutParams.topMargin = hideHeaderHeight;
					header.setLayoutParams(headerLayoutParams);
				}
				ableToPull = false;
			}
		} else {
			// ���ListView��û��Ԫ�أ�ҲӦ����������ˢ��
			ableToPull = true;
		}
	}

	private void noRefresh() {
		if (lastStatus != currentStatus) {
			if (currentStatus == STATUS_PULL_TO_REFRESH) {
				description.setText(getResources().getString(
						R.string.pull_to_refresh));
				arrow.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				rotateArrow();
			} else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
				description.setText(mContext
						.getString(R.string.refresh_too_mush));
				arrow.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				rotateArrow();
			} else if (currentStatus == STATUS_REFRESHING) {
				description.setText(mContext
						.getString(R.string.refresh_too_mush));
				progressBar.setVisibility(View.VISIBLE);
				arrow.clearAnimation();
				arrow.setVisibility(View.GONE);
			}
			refreshUpdatedAtValue();
		}
	}

	/**
	 * ��������ͷ�е���Ϣ
	 */
	private void updateHeaderView() {
		if (lastStatus != currentStatus) {
			if (currentStatus == STATUS_PULL_TO_REFRESH) {
				description.setText(getResources().getString(
						R.string.pull_to_refresh));
				arrow.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				rotateArrow();
			} else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
				description.setText(getResources().getString(
						R.string.release_to_refresh));
				arrow.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				rotateArrow();
			} else if (currentStatus == STATUS_REFRESHING) {
				description.setText(getResources().getString(
						R.string.refreshing));
				progressBar.setVisibility(View.VISIBLE);
				arrow.clearAnimation();
				arrow.setVisibility(View.GONE);
			}
			refreshUpdatedAtValue();
		}
	}

	/**
	 * ���ݵ�ǰ��״̬����ת��ͷ
	 */
	private void rotateArrow() {
		float pivotX = arrow.getWidth() / 2f;
		float pivotY = arrow.getHeight() / 2f;
		float fromDegrees = 0f;
		float toDegrees = 0f;
		if (currentStatus == STATUS_PULL_TO_REFRESH) {
			fromDegrees = 180f;
			toDegrees = 360f;
		} else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
			fromDegrees = 0f;
			toDegrees = 180f;
		}
		RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees,
				pivotX, pivotY);
		animation.setDuration(100);
		animation.setFillAfter(true);
		arrow.startAnimation(animation);
	}

	/**
	 * ˢ������ͷ���ϴθ���ʱ�����������
	 */
	private void refreshUpdatedAtValue() {
		lastUpdateTime = preferences.getLong(UPDATED_AT + mId, -1);
		long currentTime = System.currentTimeMillis();
		long timePassed = currentTime - lastUpdateTime;
		long timeIntoFormat;
		String updateAtValue;
		if (lastUpdateTime == -1) {
			updateAtValue = getResources().getString(R.string.not_updated_yet);
		} else if (timePassed < 0) {
			updateAtValue = getResources().getString(R.string.time_error);
		} else if (timePassed < ONE_MINUTE) {
			updateAtValue = getResources().getString(R.string.updated_just_now);
		} else if (timePassed < ONE_HOUR) {
			timeIntoFormat = timePassed / ONE_MINUTE;
			String value = timeIntoFormat + mContext.getString(R.string.minute);
			updateAtValue = String.format(
					getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_DAY) {
			timeIntoFormat = timePassed / ONE_HOUR;
			String value = timeIntoFormat + mContext.getString(R.string.hour);
			updateAtValue = String.format(
					getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_MONTH) {
			timeIntoFormat = timePassed / ONE_DAY;
			String value = timeIntoFormat + mContext.getString(R.string.day);
			updateAtValue = String.format(
					getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_YEAR) {
			timeIntoFormat = timePassed / ONE_MONTH;
			String value = timeIntoFormat + mContext.getString(R.string.months);
			updateAtValue = String.format(
					getResources().getString(R.string.updated_at), value);
		} else {
			timeIntoFormat = timePassed / ONE_YEAR;
			String value = timeIntoFormat + mContext.getString(R.string.year);
			updateAtValue = String.format(
					getResources().getString(R.string.updated_at), value);
		}
		updateAt.setText(updateAtValue);
	}

	/**
	 * ����ˢ�µ������ڴ������л�ȥ�ص�ע�����������ˢ�¼�����
	 */
	class RefreshingTask extends AsyncTask<Void, Integer, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			if (headerLayoutParams != null) {
				int topMargin = headerLayoutParams.topMargin;
				while (true) {
					topMargin = topMargin + SCROLL_SPEED;
					if (topMargin <= 0) {
						topMargin = 0;
						break;
					}
					publishProgress(topMargin);
					sleep(10);
					currentStatus = STATUS_REFRESHING;
				}
			}
			// else {
			// currentStatus = STATUS_REFRESHING;
			// updateHeaderView();
			// sleep(3000);
			// }
			publishProgress(0);
			if (mListener != null) {
				mListener.onRefresh();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... topMargin) {
			if (headerLayoutParams != null) {
				updateHeaderView();
				headerLayoutParams.topMargin = topMargin[0];
				header.setLayoutParams(headerLayoutParams);
			} else {
				MyLog.d(TAG, "headerLayoutParams == null");
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			// listView.setPressed(false);
			// listView.setFocusable(false);
			// listView.setFocusableInTouchMode(false);
			if (headerLayoutParams == null) {
				header.setVisibility(View.GONE);
			}
		}

	}

	/**
	 * ��������ͷ�����񣬵�δ��������ˢ�»�����ˢ����ɺ󣬴����񽫻�ʹ����ͷ��������
	 */
	class HideHeaderTask extends AsyncTask<Void, Integer, Integer> {
		@Override
		protected Integer doInBackground(Void... params) {
			int topMargin = 0;
			if (headerLayoutParams != null) {
				topMargin = headerLayoutParams.topMargin;
				while (true) {
					topMargin = topMargin + SCROLL_SPEED;
					if (topMargin <= hideHeaderHeight) {
						topMargin = hideHeaderHeight;
						break;
					}
					publishProgress(topMargin);
					sleep(10);
				}
			}
			return topMargin;
		}

		@Override
		protected void onProgressUpdate(Integer... topMargin) {
			if (headerLayoutParams != null) {
				headerLayoutParams.topMargin = topMargin[0];
				header.setLayoutParams(headerLayoutParams);
			}
		}

		@Override
		protected void onPostExecute(Integer topMargin) {
			// listView.setPressed(false);
			// listView.setFocusable(false);
			// listView.setFocusableInTouchMode(false);
			if (headerLayoutParams != null) {
				headerLayoutParams.topMargin = topMargin;
				header.setLayoutParams(headerLayoutParams);
			} else {
				header.setVisibility(View.GONE);
				MyLog.d(TAG, "headerLayoutParams == null");
			}
			currentStatus = STATUS_REFRESH_FINISHED;
		}
	}

	/**
	 * ʹ��ǰ�߳�˯��ָ���ĺ�����
	 * 
	 * @param time
	 *            ָ����ǰ�߳�˯�߶�ã��Ժ���Ϊ��λ
	 */
	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ����ˢ�µļ�������ʹ������ˢ�µĵط�Ӧ��ע��˼���������ȡˢ�»ص�
	 */
	public interface PullToRefreshListener {
		/**
		 * ˢ��ʱ��ȥ�ص��˷������ڷ����ڱ�д�����ˢ���߼� ע��˷����������߳��е��õģ� ����Բ������߳������к�ʱ����
		 */
		void onRefresh();
	}
}
