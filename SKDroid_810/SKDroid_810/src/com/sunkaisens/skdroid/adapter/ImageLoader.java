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
	// 线程池
	private ExecutorService executorService;

	public ImageLoader(Context context) {
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(5);
	}

	// 最主要的方法
	public void DisplayImage(String url, ImageView imageView,
			boolean isLoadOnlyFromCache) {
		imageViews.put(imageView, url);
		// 先从内存缓存中查找

		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null) {

			// 手动设置ImageView尺寸

			LayoutParams params = imageView.getLayoutParams();
			if (bitmap.getHeight() < params.height) {
				params.width = bitmap.getWidth();
				params.height = bitmap.getHeight();

				imageView.setLayoutParams(params);
			}
			imageView.setImageBitmap(bitmap);

		} else if (!isLoadOnlyFromCache) {

			// 若没有的话则开启新线程加载图片
			queuePhoto(url, imageView);
		}
	}

	// 最主要的方法2
	public void DisplayImage(String url, ImageView imageView,
			boolean isLoadOnlyFromCache, ImageView imageViewEdge) {
		imageViews.put(imageView, url);
		// 先从内存缓存中查找

		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null) {

			// 手动设置ImageView尺寸

			LayoutParams params = imageView.getLayoutParams();
			if (bitmap.getHeight() < params.height) {
				params.width = bitmap.getWidth();
				params.height = bitmap.getHeight();

				imageView.setLayoutParams(params);
			}
			imageViewEdge.setLayoutParams(params);
			imageView.setImageBitmap(bitmap);

		} else if (!isLoadOnlyFromCache) {

			// 若没有的话则开启新线程加载图片
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

		// 先从文件缓存中查找是否有
		Bitmap b = null;
		if (f != null && f.exists()) {
			b = decodeFile(f);
		}
		if (b != null) {
			return b;
		}
		// 最后从指定的url中下载图片
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

			// 首先不加载图片,仅获取图片尺寸
			// 当inJustDecodeBounds设为true时,不会加载图片仅获取图片尺寸信息
			options.inJustDecodeBounds = true;
			// 此时仅会将图片信息会保存至options对象内,decode方法不会返回bitmap对象
			BitmapFactory.decodeFile(url, options);

			// 计算压缩比例,如inSampleSize=4时,图片会压缩成原图的1/4
			options.inSampleSize = calculateInSampleSize(options, 100, 150);

			// 当inJustDecodeBounds设为false时,BitmapFactory.decode...就会返回图片对象了
			options.inJustDecodeBounds = false;
			// 利用计算的比例值获取压缩后的图片对象
			return BitmapFactory.decodeFile(url, options);

		} catch (Exception e) {
			// TODO: handle exception
			Log.e("ScreenChatAdapter", "BitmapFactory.decodeFile error");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 计算压缩比例值
	 * 
	 * @param options
	 *            解析图片的配置信息
	 * @param reqWidth
	 *            所需图片压缩尺寸最小宽度
	 * @param reqHeight
	 *            所需图片压缩尺寸最小高度
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// 保存图片原宽高值
		final int height = options.outHeight;
		final int width = options.outWidth;
		// 初始化压缩比例为1
		int inSampleSize = 1;

		// 当图片宽高值任何一个大于所需压缩图片宽高值时,进入循环计算系统
		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// 压缩比例值每次循环两倍增加,
			// 直到原图宽高值的一半除以压缩值后都~大于所需宽高值为止
			while ((halfHeight / inSampleSize) >= reqHeight
					&& (halfWidth / inSampleSize) >= reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	// decode这个图片并且按比例缩放以减少内存消耗，虚拟机对每张图片的缓存大小也是有限制的
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
			// 更新的操作放在UI线程中
			Activity a = (Activity) photoToLoad.imageView.getContext();
			a.runOnUiThread(bd);
		}
	}

	/**
	 * 防止图片错位
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

	// 用于在UI线程中更新界面
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

				// 手动设置ImageView尺寸
				LayoutParams params = photoToLoad.imageView.getLayoutParams();
				if (bitmap.getHeight() < params.height) {
					params.width = bitmap.getWidth();
					params.height = bitmap.getHeight();

					photoToLoad.imageView.setLayoutParams(params);
				}
				Log.e("", "手动设置图片尺寸");
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
