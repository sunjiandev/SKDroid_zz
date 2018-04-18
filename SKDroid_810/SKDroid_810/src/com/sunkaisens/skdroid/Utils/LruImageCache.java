package com.sunkaisens.skdroid.Utils;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.util.Log;

public class LruImageCache {
	private final int hardCachedSize = 8 * 1024 * 1024;
	// hard cache
	private final LruCache<String, Bitmap> sHardBitmapCache = new LruCache<String, Bitmap>(
			hardCachedSize) {
		@Override
		public int sizeOf(String key, Bitmap value) {
			return value.getRowBytes() * value.getHeight();
		}

		@Override
		protected void entryRemoved(boolean evicted, String key,
				Bitmap oldValue, Bitmap newValue) {
			// Ӳ���û�����������һ�������ʹ�õ�oldvalue���뵽�����û�����
			sSoftBitmapCache.put(key, new SoftReference<Bitmap>(oldValue));
			// �˴�Ҳ����д��sd����
		}
	};

	// ������
	private static final int SOFT_CACHE_CAPACITY = 40;
	private final static LinkedHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache = new LinkedHashMap<String, SoftReference<Bitmap>>(
			SOFT_CACHE_CAPACITY, 0.75f, true) {

		@Override
		public SoftReference<Bitmap> put(String key, SoftReference<Bitmap> value) {
			return super.put(key, value);
		}

		@Override
		protected boolean removeEldestEntry(
				Entry<String, SoftReference<Bitmap>> eldest) {

			if (size() > SOFT_CACHE_CAPACITY) {
				Log.v("tag", "Soft Reference limit , purge one");
				return true;
			}
			return false;
		}
		// return super.removeEldestEntry(eldest);
	};

	/**
	 * ����bitmap
	 * 
	 * @param key
	 * @param bitmap
	 * @return
	 */
	public boolean putBitmap(String key, Bitmap bitmap) {
		if (bitmap != null) {
			synchronized (sHardBitmapCache) {
				sHardBitmapCache.put(key, bitmap);
			}
			return true;
		}
		return false;
	}

	/**
	 * �ӻ����л�ȡbitmap
	 * 
	 * @param key
	 * @return
	 */
	public Bitmap getBitmap(String key) {
		synchronized (sHardBitmapCache) {
			final Bitmap bitmap = sHardBitmapCache.get(key);
			if (bitmap != null)
				return bitmap;
		}
		// Ӳ���û��������ж�ȡʧ�ܣ��������û��������ȡ
		synchronized (sSoftBitmapCache) {
			SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(key);
			if (bitmapReference != null) {
				final Bitmap bitmap2 = bitmapReference.get();
				if (bitmap2 != null)
					return bitmap2;
				else {
					Log.v("tag", "soft reference �Ѿ�������");
					sSoftBitmapCache.remove(key);
				}
			}
		}
		return null;
	}

}
