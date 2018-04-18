package com.sunkaisens.skdroid.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class ImageLoader {

	private MemoryCache memoryCache = new MemoryCache();
	private AbstractFileCache fileCache;
	private Map<ImageView, String> imageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	// �̳߳�
	private ExecutorService executorService;

	public ImageLoader(Context context) {
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(5);
	}

	// ����Ҫ�ķ���
	public void DisplayImage(String url, ImageView imageView,
			boolean isLoadOnlyFromCache) {
		imageViews.put(imageView, url);
		// �ȴ��ڴ滺���в���

		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null) {

			// �ֶ�����ImageView�ߴ�

			LayoutParams params = imageView.getLayoutParams();
			if (bitmap.getHeight() < params.height) {
				params.width = bitmap.getWidth();
				params.height = bitmap.getHeight();

				imageView.setLayoutParams(params);
			}
			imageView.setImageBitmap(bitmap);

		} else if (!isLoadOnlyFromCache) {

			// ��û�еĻ��������̼߳���ͼƬ
			queuePhoto(url, imageView);
		}
	}

	// ����Ҫ�ķ���2
	public void DisplayImage(String url, ImageView imageView,
			boolean isLoadOnlyFromCache, ImageView imageViewEdge) {
		imageViews.put(imageView, url);
		// �ȴ��ڴ滺���в���

		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null) {

			// �ֶ�����ImageView�ߴ�

			LayoutParams params = imageView.getLayoutParams();
			if (bitmap.getHeight() < params.height) {
				params.width = bitmap.getWidth();
				params.height = bitmap.getHeight();

				imageView.setLayoutParams(params);
			}
			imageViewEdge.setLayoutParams(params);
			imageView.setImageBitmap(bitmap);

		} else if (!isLoadOnlyFromCache) {

			// ��û�еĻ��������̼߳���ͼƬ
			queuePhoto(url, imageView, imageViewEdge);
		}
	}

	private void queuePhoto(String url, ImageView imageView) {
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		executorService.submit(new PhotosLoader(p));
	}

	// ywh
	private void queuePhoto(String url, ImageView imageView,
			ImageView imageViewEdge) {
		PhotoToLoad p = new PhotoToLoad(url, imageView, imageViewEdge);
		executorService.submit(new PhotosLoader(p));
	}

	private Bitmap getBitmap(String url) {
		File f = fileCache.getFile(url);

		// �ȴ��ļ������в����Ƿ���
		Bitmap b = null;
		if (f != null && f.exists()) {
			b = decodeFile(f);
		}
		if (b != null) {
			return b;
		}
		// ����ָ����url������ͼƬ
		BitmapFactory.Options options = new BitmapFactory.Options();
		// options.inSampleSize = 1;

		try {

			// Bitmap bmpBitmap = BitmapFactory.decodeFile(url, options);
			//
			// int width = bmpBitmap.getWidth();
			// int height = bmpBitmap.getHeight();
			//
			// if (width > 100 || height > 100) {
			// float scalewidth = (float) 250 / width;
			//
			// Matrix matrix = new Matrix();
			// matrix.postScale(scalewidth, scalewidth);
			// try {
			//
			// Bitmap newbmpBitmap = Bitmap.createBitmap(bmpBitmap, 0, 0,
			// width, height, matrix, true);
			//
			// // imageViews.put(newbmpBitmap, urlnnames[2]);
			//
			// bmpBitmap.recycle();
			// bmpBitmap = newbmpBitmap;
			// } catch (Exception e) {
			// // TODO: handle exception
			// Log.e("ScreenChatAdapter", "Bitmap.createBitmap error");
			// e.printStackTrace();
			// }
			// }
			//
			// return bmpBitmap;

			// ���Ȳ�����ͼƬ,����ȡͼƬ�ߴ�
			// ��inJustDecodeBounds��Ϊtrueʱ,�������ͼƬ����ȡͼƬ�ߴ���Ϣ
			options.inJustDecodeBounds = true;
			// ��ʱ���ὫͼƬ��Ϣ�ᱣ����options������,decode�������᷵��bitmap����
			BitmapFactory.decodeFile(url, options);

			// ����ѹ������,��inSampleSize=4ʱ,ͼƬ��ѹ����ԭͼ��1/4
			options.inSampleSize = calculateInSampleSize(options, 100, 150);

			// ��inJustDecodeBounds��Ϊfalseʱ,BitmapFactory.decode...�ͻ᷵��ͼƬ������
			options.inJustDecodeBounds = false;
			// ���ü���ı���ֵ��ȡѹ�����ͼƬ����
			return BitmapFactory.decodeFile(url, options);

		} catch (Exception e) {
			// TODO: handle exception
			Log.e("ScreenChatAdapter", "BitmapFactory.decodeFile error");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ����ѹ������ֵ
	 * 
	 * @param options
	 *            ����ͼƬ��������Ϣ
	 * @param reqWidth
	 *            ����ͼƬѹ���ߴ���С���
	 * @param reqHeight
	 *            ����ͼƬѹ���ߴ���С�߶�
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// ����ͼƬԭ���ֵ
		final int height = options.outHeight;
		final int width = options.outWidth;
		// ��ʼ��ѹ������Ϊ1
		int inSampleSize = 1;

		// ��ͼƬ���ֵ�κ�һ����������ѹ��ͼƬ���ֵʱ,����ѭ������ϵͳ
		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// ѹ������ֵÿ��ѭ����������,
			// ֱ��ԭͼ���ֵ��һ�����ѹ��ֵ��~����������ֵΪֹ
			while ((halfHeight / inSampleSize) >= reqHeight
					&& (halfWidth / inSampleSize) >= reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	// decode���ͼƬ���Ұ����������Լ����ڴ����ģ��������ÿ��ͼƬ�Ļ����СҲ�������Ƶ�
	private Bitmap decodeFile(File f) {
		FileInputStream fis = null;
		FileInputStream fis2 = null;
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			fis = new FileInputStream(f);
			BitmapFactory.decodeStream(fis, null, o);

			// Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE = 100;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE
						|| height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			fis.close();
			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			fis2 = new FileInputStream(f);
			return BitmapFactory.decodeStream(fis2, null, o2);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fis2 != null) {
				try {
					fis2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;
		public ImageView imageViewEdge;

		public PhotoToLoad(String u, ImageView i) {
			url = u;
			imageView = i;
		}

		// ywh
		public PhotoToLoad(String u, ImageView i, ImageView image) {
			url = u;
			imageView = i;
			imageViewEdge = image;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			Bitmap bmp = getBitmap(photoToLoad.url);
			memoryCache.put(photoToLoad.url, bmp);
			if (imageViewReused(photoToLoad))
				return;
			BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
			// ���µĲ�������UI�߳���
			Activity a = (Activity) photoToLoad.imageView.getContext();
			a.runOnUiThread(bd);
		}
	}

	/**
	 * ��ֹͼƬ��λ
	 * 
	 * @param photoToLoad
	 * @return
	 */
	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	// ������UI�߳��и��½���
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			if (bitmap != null) {

				// �ֶ�����ImageView�ߴ�
				LayoutParams params = photoToLoad.imageView.getLayoutParams();
				if (bitmap.getHeight() < params.height) {
					params.width = bitmap.getWidth();
					params.height = bitmap.getHeight();

					photoToLoad.imageView.setLayoutParams(params);
				}
				Log.e("", "�ֶ�����ͼƬ�ߴ�");
				if (photoToLoad.imageViewEdge != null) {
					photoToLoad.imageViewEdge.setLayoutParams(params);
				}
				photoToLoad.imageView.setImageBitmap(bitmap);

			}
		}
	}

	public void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
			Log.e("", "CopyStream catch Exception...");
		}
	}
}
