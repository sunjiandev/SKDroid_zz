package com.sunkaisens.skdroid.Services;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.view.ViewGroup;

import com.sunkaisens.skdroid.fragments.AVGroupAudioFragment;
import com.sunkaisens.skdroid.fragments.AVGroupVideoFragment;
import com.sunkaisens.skdroid.fragments.AVGroupVideoTryingFragment;
import com.sunkaisens.skdroid.fragments.AVSingleAudioFragment;
import com.sunkaisens.skdroid.fragments.AVSingleAudioTryingFragment;
import com.sunkaisens.skdroid.fragments.AVSingleINcomingFragment;
import com.sunkaisens.skdroid.fragments.AVSingleVideoFragment;
import com.sunkaisens.skdroid.fragments.AVSingleVideoTryingFragment;
import com.sunkaisens.skdroid.fragments.AVVideoMonitorFragment;
import com.sunkaisens.skdroid.fragments.SksFunctionList;

public class ServiceFragment {
	
	/**
	 * ������Ƶ�������
	 * @param activity
	 * @param serviceAV
	 * @param layout
	 * @return
	 */
	public static AVGroupVideoFragment makeAvGroupVideoFragment(Activity activity,
			ServiceAV serviceAV,ViewGroup layout){
		
		FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
		AVGroupVideoFragment groupVideoFragment = new AVGroupVideoFragment();
		groupVideoFragment.init(serviceAV);
		ft.replace(layout.getId(), groupVideoFragment);
		ft.commit();
		return groupVideoFragment;
		
	}
	
	/**
	 * ���������������
	 * @param activity
	 * @param serviceAV
	 * @param layout
	 * @return
	 */
	public static AVGroupAudioFragment makeAvGroupAudioFragment(Activity activity,
			ServiceAV serviceAV,ViewGroup layout){
		
		FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
		AVGroupAudioFragment groupAudioFragment = new AVGroupAudioFragment();
		groupAudioFragment.init(serviceAV);
		ft.replace(layout.getId(), groupAudioFragment);
		ft.commit();
		return groupAudioFragment;
		
	}
	
	/**
	 * ������Ƶ��������
	 * @param activity
	 * @param serviceAV
	 * @param layout
	 * @return
	 */
	public static AVSingleVideoFragment makeAvSingleVideoFragment(Activity activity,
			ServiceAV serviceAV,ViewGroup layout){
		
		FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
		AVSingleVideoFragment videoFragment = new AVSingleVideoFragment();
		videoFragment.init(serviceAV);
		ft.replace(layout.getId(), videoFragment);
//		ft.add(layout.getId(), videoFragment);
		ft.commit();
		return videoFragment;
		
	}
	
	/**
	 * ������Ƶ��ؽ���
	 * @param activity
	 * @param serviceAV
	 * @param layout
	 * @return
	 */
	public static AVVideoMonitorFragment makeAvVideoMonitorFragment(Activity activity,
			ServiceAV serviceAV,ViewGroup layout){
		
		FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
		AVVideoMonitorFragment videoMonitorFragment = new AVVideoMonitorFragment();
		videoMonitorFragment.init(serviceAV);
		ft.replace(layout.getId(), videoMonitorFragment);
		ft.commit();
		return videoMonitorFragment;
		
	}
	
	/**
	 * ����������������
	 * @param activity
	 * @param serviceAV
	 * @param layout
	 * @return
	 */
	public static AVSingleAudioFragment makeAvSingleAudioFragment(Activity activity,
			ServiceAV serviceAV,ViewGroup layout){
		
		FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
		AVSingleAudioFragment audioFragment = new AVSingleAudioFragment();
		audioFragment.init(serviceAV);
		ft.replace(layout.getId(), audioFragment);
		ft.commit();
		return audioFragment;
		
	}
	
	/**
	 * �����������н���
	 * @param activity
	 * @param serviceAV
	 * @param layout
	 * @return
	 */
	public static AVSingleAudioTryingFragment makeAvSingleAudioTryingFragment(Activity activity,
			ServiceAV serviceAV,ViewGroup layout){
		
		layout.removeAllViews();
		
		FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
		AVSingleAudioTryingFragment audioFragment = new AVSingleAudioTryingFragment();
		audioFragment.init(serviceAV);
		ft.replace(layout.getId(), audioFragment);
		ft.commit();
		return audioFragment;
		
	}
	
	/**
	 * ������Ƶ�������н���
	 * @param activity
	 * @param serviceAV
	 * @param layout
	 * @return
	 */
	public static AVSingleVideoTryingFragment makeAvSingleVideoTryingFragment(Activity activity,
			ServiceAV serviceAV,ViewGroup layout){
		
		layout.removeAllViews();
		
		FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
		AVSingleVideoTryingFragment videoFragment = new AVSingleVideoTryingFragment();
		videoFragment.init(serviceAV);
		ft.replace(layout.getId(), videoFragment);
		ft.commit();
		return videoFragment;
		
	}
	
	/**
	 * �����������
	 * @param activity
	 * @param serviceAV
	 * @param layout
	 * @return
	 */
	public static AVSingleINcomingFragment makeAvSingleIncomingFragment(Activity activity,
			ServiceAV serviceAV,ViewGroup layout){
		
		layout.removeAllViews();
		
		FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
		AVSingleINcomingFragment incomingFragment = new AVSingleINcomingFragment();
		incomingFragment.init(serviceAV);
		ft.replace(layout.getId(), incomingFragment);
		ft.commit();
		return incomingFragment;
		
	}
	
	/**
	 * ������Ƶ������н���
	 * @param activity
	 * @param serviceAV
	 * @param layout
	 * @return
	 */
	public static AVGroupVideoTryingFragment makeAvGroupVideoTryingFragment(Activity activity,
			ServiceAV serviceAV,ViewGroup layout){
		
		layout.removeAllViews();
		
		FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
		AVGroupVideoTryingFragment incomingFragment = new AVGroupVideoTryingFragment();
		incomingFragment.init(serviceAV);
		ft.replace(layout.getId(), incomingFragment);
		ft.commit();
		return incomingFragment;
		
	}

}
