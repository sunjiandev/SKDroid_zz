package com.sunkaisens.skdroid.component;

import com.sunkaisens.skdroid.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;

public class ClearEditText extends EditText implements OnFocusChangeListener,
		TextWatcher {
	/**
	 * ɾ����ť������
	 */
	private Drawable mClearDrawable;
	private Drawable mFindDrawable;
	/**
	 * �ؼ��Ƿ��н���
	 */
	private boolean hasFoucs;

	public ClearEditText(Context context) {
		this(context, null);
	}

	public ClearEditText(Context context, AttributeSet attrs) {
		// ���ﹹ�췽��Ҳ����Ҫ����������ܶ����Բ�����XML���涨��
		this(context, attrs, android.R.attr.editTextStyle);
	}

	public ClearEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		// ��ȡEditText��DrawableRight,����û���������Ǿ�ʹ��Ĭ�ϵ�ͼƬ
		mFindDrawable = getCompoundDrawables()[0];
		mClearDrawable = getCompoundDrawables()[2];
		if (mClearDrawable == null) {
			// throw new
			// NullPointerException("You can add drawableRight attribute in XML");
			mClearDrawable = getResources().getDrawable(
					R.drawable.delete_selector);
		}
		if (mFindDrawable == null) {
			mFindDrawable = getResources().getDrawable(R.drawable.btn_search);
		}

		mFindDrawable.setBounds(0, 0, mFindDrawable.getIntrinsicWidth(),
				mFindDrawable.getIntrinsicHeight());
		mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(),
				mClearDrawable.getIntrinsicHeight());
		// Ĭ����������ͼ��
		setClearIconVisible(false);
		setSearchIconVisible(true);
		// ���ý���ı�ļ���
		setOnFocusChangeListener(this);
		// ����������������ݷ����ı�ļ���
		addTextChangedListener(this);
	}

	/**
	 * ��Ϊ���ǲ���ֱ�Ӹ�EditText���õ���¼������������ü�ס���ǰ��µ�λ����ģ�����¼� �����ǰ��µ�λ�� �� EditText�Ŀ�� -
	 * ͼ�굽�ؼ��ұߵļ�� - ͼ��Ŀ�� �� EditText�Ŀ�� - ͼ�굽�ؼ��ұߵļ��֮�����Ǿ�������ͼ�꣬��ֱ�����û�п���
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (getCompoundDrawables()[2] != null) {

				boolean touchable = event.getX() > (getWidth() - getTotalPaddingRight())
						&& (event.getX() < ((getWidth() - getPaddingRight())));

				if (touchable) {
					this.setText("");
				}
			}
		}

		return super.onTouchEvent(event);
	}

	/**
	 * ��ClearEditText���㷢���仯��ʱ���ж������ַ��������������ͼ�����ʾ������
	 */
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		this.hasFoucs = hasFocus;
		if (hasFocus) {
			setClearIconVisible(getText().length() > 0);
		} else {
			setClearIconVisible(false);
		}
	}

	/**
	 * set the search icon show or hide.
	 * 
	 * @param visible
	 */
	public void setSearchIconVisible(boolean visible) {
		Drawable left = visible ? mFindDrawable : null;
		setCompoundDrawables(left, getCompoundDrawables()[1],
				getCompoundDrawables()[2], getCompoundDrawables()[3]);
	}

	/**
	 * ��������ͼ��
	 * 
	 * @param visible
	 */
	public void setClearIconVisible(boolean visible) {
		Drawable right = visible ? mClearDrawable : null;
		setCompoundDrawables(getCompoundDrawables()[0],
				getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
	}

	/**
	 * ��������������ݷ����仯��ʱ��ص��ķ���
	 */
	@Override
	public void onTextChanged(CharSequence s, int start, int count, int after) {
		if (hasFoucs) {
			setClearIconVisible(s.length() > 0);
		}
		setSearchIconVisible(s.length() <= 0);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	@Override
	public void afterTextChanged(Editable s) {

	}

	/**
	 * ���ûζ�����
	 */
	public void setShakeAnimation() {
		this.setAnimation(shakeAnimation(5));
	}

	/**
	 * �ζ�����
	 * 
	 * @param counts
	 *            1���ӻζ�������
	 * @return
	 */
	public static Animation shakeAnimation(int counts) {
		Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
		translateAnimation.setInterpolator(new CycleInterpolator(counts));
		translateAnimation.setDuration(1000);
		return translateAnimation;
	}

}
