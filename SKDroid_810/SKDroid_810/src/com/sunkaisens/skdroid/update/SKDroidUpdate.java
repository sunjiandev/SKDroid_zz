package com.sunkaisens.skdroid.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.utils.MyLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.Utils.Tools_data;
import com.sunkaisens.skdroid.app.service.UpdateService;

/**
 * @author zh
 * 
 */
public class SKDroidUpdate {
	private final static String TAG = SKDroidUpdate.class.getCanonicalName();
	public static int newVerCode = 0;
	public Handler handler;
	public ProgressDialog bar;
	public int vercode;

	private static SKDroidUpdate mInstance;

	private static boolean updatingFlag = false;
	public static boolean isStartUpdateChecked = false;
	public static boolean isUpdateDialogUp = false;

	public static int progress = 0;

	public SKDroidUpdate() {
	}

	public static SKDroidUpdate getSkDroidUpdate() {
		if (mInstance == null) {
			mInstance = new SKDroidUpdate();
		}
		return mInstance;
	}

	// Update application method
	private boolean getServerVerCode() {
		try {
			String verjson = NetworkTool.getContent(ChkVer.getUpdateUrl());
			JSONArray array = new JSONArray(verjson);
			if (array.length() > 0) {
				JSONObject obj = array.getJSONObject(0);
				try {
					newVerCode = Integer.parseInt(obj.getString("verCode"));
				} catch (Exception e) {
					newVerCode = -1;
					return false;
				}
			}
		} catch (Exception e) {
			Log.e("Connect Server", e.toString());
			return false;
		}
		return true;
	}

	/**
	 * 软件更新
	 * 
	 * @param installType
	 *            FORCE: 强制更新 其它: 可选更新
	 */
	private void doNewVersionUpdate(String installType) {

		try {
			if (!NgnStringUtils.isNullOrEmpty(ChkVer.UPDATE_SAVENAME)) {
				// 如果更新文件已下载完成，则直接显示更新界面
				File file = new File(Environment.getExternalStorageDirectory(),
						ChkVer.UPDATE_SAVENAME);
				if (file.exists()) {
					update();
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		StringBuffer sb = new StringBuffer();
		sb.append(NgnApplication.getContext().getString(
				R.string.find_new_version));
		sb.append(NgnApplication.getContext()
				.getString(R.string.is_need_update));
		AlertDialog.Builder builder = new AlertDialog.Builder(Engine
				.getInstance().getMainActivity());
		builder.setTitle(
				NgnApplication.getContext().getString(R.string.soft_update))
				.setMessage(sb.toString())
				.setCancelable(false)
				// 按对话框以外的地方不起作用。按返回键也不起作用
				.setPositiveButton(
						NgnApplication.getContext().getString(R.string.update),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// bar = new
								// ProgressDialog(Engine.getInstance().getMainActivity());
								// bar.setTitle("正在下载");
								// bar.setMessage("请稍候...");
								// bar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
								// downFile(ChkVer.getDownloadUrl());
								Intent updateService = new Intent(SKDroid
										.getContext(), UpdateService.class);
								SKDroid.getContext()
										.startService(updateService);
							}
						});
		if (installType == null || !installType.equals("FORCE")) {
			builder.setNegativeButton(
					NgnApplication.getContext().getString(R.string.not_update),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// EzApp.showIntent(new Intent(EzTent.SHOW_MAIN));
							// // 返回到调用者界面
							if (dialog != null) {
								dialog.dismiss();
							}
						}
					});
		}
		Dialog dialog = builder.create();
		dialog.show();
	}

	public void downFile(Handler progressHandler) {
		// bar.show();
		handler = progressHandler;
		final String url = ChkVer.getDownloadUrl();
		new Thread() {
			public void run() {
				Looper.prepare();
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(url);
				HttpResponse response;
				File file = null;
				FileOutputStream fileOutputStream = null;
				try {
					response = client.execute(get);
					HttpEntity entity = response.getEntity();
					long length = entity.getContentLength();
					InputStream is = entity.getContent();
					if (is != null) {
						file = new File(
								Environment.getExternalStorageDirectory(),
								ChkVer.UPDATE_SAVENAME);
						fileOutputStream = new FileOutputStream(file);
						byte[] buf = new byte[1024];
						int len = 0;
						int persent = 0;
						int downloadLength = 0;
						while ((len = is.read(buf)) != -1) {
							fileOutputStream.write(buf, 0, len);

							downloadLength += len;
							float persent1 = (float) ((float) downloadLength / (float) length);
							persent = (int) (persent1 * 100);
							if ((persent - progress) >= 1) {
								progress = persent;
								refreshProgress(persent);
								Log.d(TAG, "uploadPersent : " + progress);
							}
						}
						fileOutputStream.flush();
					}

					// down();
					// bar.cancel();
					handler = null;

				} catch (ClientProtocolException e) {
					e.printStackTrace();
					return;
				} catch (IOException e) {
					e.printStackTrace();
					return;
				} finally {
					if (file != null) {
						file.delete();
					}
					((Engine) Engine.getInstance()).cancelUpdateNotify();
					if (fileOutputStream != null) {
						try {
							fileOutputStream.close();
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
				}
				update();
				Looper.loop();
			}
		}.start();
	}

	private void refreshProgress(int progress) {
		try {
			if (handler != null) {
				Message msg = Message.obtain(handler,
						Main.SKDROIDUPDATEPROGRESS);
				msg.getData().putInt("progress", progress);
				MyLog.d(TAG, "Handler send Progress=" + progress);
				handler.sendMessage(msg);
			}
		} catch (Exception e) {
			Log.d(TAG, "SKDroidUpdateException : " + handler);
		}
	}

	// 开始安装下载完的程序
	public void update() {
		// Intent intent = new Intent(Intent.ACTION_VIEW);
		// intent.setDataAndType(Uri.fromFile(new File(Environment
		// .getExternalStorageDirectory(), ChkVer.UPDATE_SAVENAME)),
		// "application/vnd.android.package-archive");
		// Engine.getInstance().getMainActivity().startActivity(intent);
		if (!isUpdateDialogUp) {
			isUpdateDialogUp = true;
		} else {
			return;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(NgnApplication.getContext().getString(
				R.string.new_version_download_success));
		AlertDialog.Builder builder = new AlertDialog.Builder(Engine
				.getInstance().getMainActivity());
		builder.setTitle(
				NgnApplication.getContext().getString(R.string.soft_install))
				.setMessage(sb.toString())
				.setCancelable(false)
				// 按对话框以外的地方不起作用。按返回键也不起作用
				.setPositiveButton(
						NgnApplication.getContext().getString(R.string.install),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								isUpdateDialogUp = false;

								cancelUpdateNotify();

								try {
									Tools_data
											.writeData(Main.mMessageReportHashMap);
								} catch (IOException e) {
									e.printStackTrace();
								}

								Intent intent = new Intent(Intent.ACTION_VIEW);
								intent.setDataAndType(Uri.fromFile(new File(
										Environment
												.getExternalStorageDirectory(),
										ChkVer.UPDATE_SAVENAME)),
										"application/vnd.android.package-archive");
								SKDroid.getContext().startActivity(intent);
							}
						})
				.setNegativeButton(
						NgnApplication.getContext().getString(R.string.cancel),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								isUpdateDialogUp = false;
								dialog.dismiss();
								cancelUpdateNotify();
							}
						});
		Dialog dialog = builder.create();
		dialog.show();
	}

	/**
	 * 软件不更新
	 */
	public void notNewVersionShow() {
		String verName = NgnApplication.getContext()
				.getString(R.string.unknown);
		Activity main = Engine.getInstance().getMainActivity();
		if (main != null) {
			verName = ChkVer.getVerName(main);
		}
		StringBuffer sb = new StringBuffer();
		sb.append(NgnApplication.getContext().getString(
				R.string.current_version));
		sb.append(verName);
		sb.append(NgnApplication.getContext().getString(
				R.string.not_need_update));
		Dialog dialog = new AlertDialog.Builder(Engine.getInstance()
				.getMainActivity())
				.setTitle(
						NgnApplication.getContext().getString(
								R.string.soft_update))
				.setMessage(sb.toString())
				.setCancelable(false)
				// 按对话框以外的地方不起作用。按返回键也不起作用
				.setPositiveButton(
						NgnApplication.getContext().getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// EzApp.showIntent(new
								// Intent(EzTent.SHOW_MAIN)); //
								// 返回到调用者界面
							}
						}).create();
		dialog.show();
	}

	private void cancelUpdateNotify() {
		NotificationManager mNotifManager = (NotificationManager) SKDroid
				.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		mNotifManager.cancel(R.layout.update_notify);
	}

	public JSONObject getPost(String uri) {
		HttpPost request = new HttpPost(uri);
		// 先封装一个JSON对象
		JSONObject param = new JSONObject();
		try {
			// param.put("name", "rarnu");
			// param.put("password", "123456");
			param.put("key", "SKDroid");
			// 绑定到请求Entry
			StringEntity se = new StringEntity(param.toString());
			request.setEntity(se);
			// 发送请求
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(request);
			// 得到应答的字符串，这也是一个JSON格式保存的数据
			String retSrc = EntityUtils.toString(httpResponse.getEntity());
			// 生成JSON对象
			JSONObject result = new JSONObject(retSrc);
			// String token = (String) result.get("token");
			return result;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) { // org.apache.http.conn.HttpHostConnectException:
									// Connection to http://192.168.1.222:8090
									// refused
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @throws NumberFormatException
	 */
	public void doUpdate() throws NumberFormatException {
		if (updatingFlag) {
			return;
		}

		final SKDroidUpdate sKDroidUpdate = new SKDroidUpdate();
		// {"installType":"FORCE","downloadUrl":"http:\/\/192.168.1.222:8090\/files\/SKDroid.apk","key":"SKDroid","version":"1.0.173"}
		final Handler handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Bundle bundle = msg.getData();
				// 检查软件下载服务器
				if (bundle == null) {
					SystemVarTools
							.showToast(NgnApplication.getContext().getString(
									R.string.feedback_srever_connect_failed));
					return;
				}

				try {
					String key = bundle.getString("key");
					String version = bundle.getString("version");
					String downloadUrl = bundle.getString("downloadUrl");
					String installType = bundle.getString("installType");

					// 检查软件下载协议
					if (key == null || version == null || downloadUrl == null) {
						SystemVarTools.showToast(NgnApplication.getContext()
								.getString(R.string.soft_download_error));
						return;
					}

					// 检查软件标识
					if (!key.equals("SKDroid")) {
						SystemVarTools.showToast(NgnApplication.getContext()
								.getString(R.string.soft_log_error) + key);
						return;
					}

					ChkVer.UPDATE_SAVENAME = "SKDroid_" + version + ".apk";

					String verName = ChkVer.getVerName(SKDroid.getContext());
					String[] versions = version.split("\\.");
					String[] verNames = verName.split("\\.");

					// 检查软件版本名称格式
					if (versions.length < 3) {
						SystemVarTools.showToast(NgnApplication.getContext()
								.getString(R.string.soft_name_error));
						return;
					}

					// 软件下载地址
					ChkVer.setUPDATE_URL(downloadUrl);

					// 检查升级 是否需要升级
					if (Integer.parseInt(versions[0]) > Integer
							.parseInt(verNames[0])) {
						sKDroidUpdate.doNewVersionUpdate(installType);
					} else if (Integer.parseInt(versions[0]) == Integer
							.parseInt(verNames[0])
							&& Integer.parseInt(versions[1]) > Integer
									.parseInt(verNames[1])) {
						sKDroidUpdate.doNewVersionUpdate(installType);
					} else if (Integer.parseInt(versions[0]) == Integer
							.parseInt(verNames[0])
							&& Integer.parseInt(versions[1]) == Integer
									.parseInt(verNames[1])
							&& Integer.parseInt(versions[2]) > Integer
									.parseInt(verNames[2])) {
						sKDroidUpdate.doNewVersionUpdate(installType);
					} else {
						sKDroidUpdate.notNewVersionShow();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					updatingFlag = true;
					JSONObject resultTmp = sKDroidUpdate.getPost(ChkVer
							.getUpdateUrl());
					Message msg = handler.obtainMessage();
					if (resultTmp != null) {
						Bundle bundle = new Bundle();
						bundle.putString("key", resultTmp.getString("key"));
						bundle.putString("version",
								resultTmp.getString("version"));
						bundle.putString("downloadUrl",
								resultTmp.getString("downloadUrl"));
						bundle.putString("installType",
								resultTmp.getString("installType"));
						msg.setData(bundle);
					}
					msg.sendToTarget();
					updatingFlag = false;
				} catch (JSONException e) {
					e.printStackTrace();
					updatingFlag = false;
				} catch (Exception e) {
					e.printStackTrace();
					updatingFlag = false;
				}
			}
		}).start();
	}

	/**
	 * @throws NumberFormatException
	 */
	public void doUpdate2() throws NumberFormatException {
		if (updatingFlag) {
			return;
		}

		final SKDroidUpdate sKDroidUpdate = new SKDroidUpdate();
		// {"installType":"FORCE","downloadUrl":"http:\/\/192.168.1.222:8090\/files\/SKDroid.apk","key":"SKDroid","version":"1.0.173"}
		final Handler handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Bundle bundle = msg.getData();
				// 检查软件下载服务器
				if (bundle == null) {
					// SystemVarTools.showToast("服务器连接失败！");
					return;
				}

				try {
					String key = bundle.getString("key");
					String version = bundle.getString("version");
					String downloadUrl = bundle.getString("downloadUrl");
					String installType = bundle.getString("installType");

					// 检查软件下载协议
					if (key == null || version == null || downloadUrl == null) {
						// SystemVarTools.showToast("软件下载协议不正确！");
						return;
					}

					// 检查软件标识
					if (!key.equals("SKDroid")) {
						// SystemVarTools.showToast("软件标识不正确：" + key);
						return;
					}

					ChkVer.UPDATE_SAVENAME = "SKDroid_" + version + ".apk";

					String verName = ChkVer.getVerName(SKDroid.getContext());
					String[] versions = version.split("\\.");
					String[] verNames = verName.split("\\.");

					// 检查软件版本名称格式
					if (versions.length < 3) {
						SystemVarTools.showToast(NgnApplication.getContext()
								.getString(R.string.soft_name_error));
						return;
					}

					// 软件下载地址
					ChkVer.setUPDATE_URL(downloadUrl);

					// 检查升级 是否需要升级
					if (Integer.parseInt(versions[0]) > Integer
							.parseInt(verNames[0])) {
						sKDroidUpdate.doNewVersionUpdate(installType);
					} else if (Integer.parseInt(versions[0]) == Integer
							.parseInt(verNames[0])
							&& Integer.parseInt(versions[1]) > Integer
									.parseInt(verNames[1])) {
						sKDroidUpdate.doNewVersionUpdate(installType);
					} else if (Integer.parseInt(versions[0]) == Integer
							.parseInt(verNames[0])
							&& Integer.parseInt(versions[1]) == Integer
									.parseInt(verNames[1])
							&& Integer.parseInt(versions[2]) > Integer
									.parseInt(verNames[2])) {
						sKDroidUpdate.doNewVersionUpdate(installType);
					} else {
						// sKDroidUpdate.notNewVersionShow();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					updatingFlag = true;
					JSONObject resultTmp = sKDroidUpdate.getPost(ChkVer
							.getUpdateUrl());
					Message msg = handler.obtainMessage();
					if (resultTmp != null) {
						Bundle bundle = new Bundle();
						bundle.putString("key", resultTmp.getString("key"));
						bundle.putString("version",
								resultTmp.getString("version"));
						bundle.putString("downloadUrl",
								resultTmp.getString("downloadUrl"));
						bundle.putString("installType",
								resultTmp.getString("installType"));
						msg.setData(bundle);
					}
					msg.sendToTarget();
					updatingFlag = false;
				} catch (JSONException e) {
					e.printStackTrace();
					updatingFlag = false;
				} catch (Exception e) {
					e.printStackTrace();
					updatingFlag = false;
				}
			}
		}).start();
	}

	public boolean isUpdatingFlag() {
		return updatingFlag;
	}

	private void createNotification() {
		// notification
	}

}
