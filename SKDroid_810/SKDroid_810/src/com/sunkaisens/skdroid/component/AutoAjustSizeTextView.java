package com.sunkaisens.skdroid.component;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class AutoAjustSizeTextView extends TextView {

	private static float DEFAULT_MIN_SIZE = 2;
	private static float DEFAULT_MAX_SIZE = 30;

	private Paint testPaint;
	private float minTextSize, maxTextSize;

	public AutoAjustSizeTextView(Context context) {
		this(context, null);
		init();
	}

	public AutoAjustSizeTextView(Context context, AttributeSet attrs) {
		// 这里构造方法也很重要，不加这个很多属性不能再XML里面定义
		this(context, attrs, android.R.attr.textViewStyle);
		init();
	}

	public AutoAjustSizeTextView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		testPaint = new Paint();
		testPaint.set(this.getPaint());

		maxTextSize = this.getTextSize();

		if (maxTextSize <= DEFAULT_MIN_SIZE) {
			maxTextSize = DEFAULT_MIN_SIZE;
		}
		minTextSize = DEFAULT_MIN_SIZE;
	}

	private void refitText(String text, int textWidth) {
		if (textWidth > 0) {
			int availableWidth = textWidth - this.getPaddingTop()
					- this.getPaddingBottom();
			float trySize = maxTextSize;
			testPaint.setTextSize(trySize);

			while ((trySize > minTextSize)
					&& (testPaint.measureText(text) > availableWidth)) {
				trySize -= 1;
				if (trySize <= minTextSize) {
					trySize = minTextSize;
					break;

				}
				testPaint.setTextSize(trySize);
			}
			this.setTextSize(trySize);

		}
	}

	@Override
	protected void onTextChanged(CharSequence text, int start,
			int lengthBefore, int lengthAfter) {
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		refitText(text.toString(), this.getWidth());
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (h != oldh) {
			refitText(this.getText().toString(), w);

		}
	}

}
