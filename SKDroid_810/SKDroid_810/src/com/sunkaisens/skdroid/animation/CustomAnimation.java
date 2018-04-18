package com.sunkaisens.skdroid.animation;

import android.view.animation.Animation;
import android.view.animation.Transformation;

public class CustomAnimation extends Animation {

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		
		t.getMatrix().setTranslate((float) (Math.sin(interpolatedTime*10)*20),0);
		
		super.applyTransformation(interpolatedTime, t);
	}

}
