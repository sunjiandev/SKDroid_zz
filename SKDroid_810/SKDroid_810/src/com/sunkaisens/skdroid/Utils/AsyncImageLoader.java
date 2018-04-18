package com.sunkaisens.skdroid.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

public class AsyncImageLoader {

	private ExecutorService executorService = Executors.newFixedThreadPool(5);

	private final Handler handler = new Handler();

	LruImageCache imageCache_rul = new LruImageCache();

	public Bitmap loadDrawable(int id, final String imageUrl,
			final ImageView v, final ImageCallback callback) {
		Bitmap bitmap = imageCache_rul.getBitmap(imageUrl);
		if (bitmap != null) {
			v.setImageBitmap(bitmap);
			return bitmap;
		}
		v.setImageResource(id);
		executorService.submit(new Runnable() {
			public void run() {
				try {
					final Bitmap bitmap = getUrlimg(imageUrl);
					if (bitmap != null) {
						imageCache_rul.putBitmap(imageUrl, bitmap);
					}
					handler.post(new Runnable() {
						public void run() {
							// bitmap=
							callback.imageLoaded(-1, bitmap, v);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return null;
	}

	private Bitmap getUrlimg(String url) {
		Bitmap bitmap = null;
		URL imageUrl = null;

		if ("".equals(url)) {
			return null;
		}
		HttpURLConnection conn = null;
		InputStream is = null;
		try {
			imageUrl = new URL(url);
			conn = (HttpURLConnection) imageUrl.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5 * 1000);
			conn.connect();
			is = conn.getInputStream();
			byte[] bt = getBytes(is);
			bitmap = BitmapFactory.decodeByteArray(bt, 0, bt.length);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (conn != null)
				conn.disconnect();
		}

		// BitmapDrawable bd=new BitmapDrawable(bitmap);
		return bitmap;
	}

	private byte[] getBytes(InputStream is) throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] b = new byte[1024];
		int len = 0;
		while ((len = is.read(b, 0, 1024)) != -1) {
			baos.write(b, 0, len);
			baos.flush();
		}
		byte[] bytes = baos.toByteArray();
		return bytes;
	}

	public interface ImageCallback {
		public void imageLoaded(Integer t, Bitmap bitmap, ImageView v);
	}
}
