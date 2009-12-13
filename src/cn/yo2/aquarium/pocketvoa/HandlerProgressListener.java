package cn.yo2.aquarium.pocketvoa;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class HandlerProgressListener implements IProgressListener {
	private static final String CLASSTAG = HandlerProgressListener.class.getSimpleName();
	
	public static final int WHAT_DOWNLOAD_PROGRESS = 2;
	public static final int WHAT_DOWNLOAD_ERROR = 1;
	public static final int WHAT_DOWNLOAD_SUCCESS = 0;
	
	public static final String KEY_ERROR_MSG = "error";
	
	public HandlerProgressListener(Handler handler) {
		super();
		this.mHandler = handler;
	}

	private Handler mHandler;
	
	public void updateProgress(int which, long pos, long total) {
		Message message = obtainMessage(WHAT_DOWNLOAD_PROGRESS);
		message.arg1 = (int) (pos * 100 / total);
		message.arg2 = which;
		mHandler.sendMessage(message);
	}

	public void setSuccess(int which) {
		Message message = obtainMessage(WHAT_DOWNLOAD_SUCCESS);
		message.arg2 = which;
		mHandler.sendMessage(message);
	}

	public void setError(int which, String detail) {
		Message message = obtainMessage(WHAT_DOWNLOAD_ERROR);
		message.arg2 = which;
		Bundle bundle = new Bundle();
		bundle.putString(KEY_ERROR_MSG, detail);
		message.setData(bundle);
		mHandler.sendMessage(message);
	}

	private Message obtainMessage(int what) {
		Message message = Message.obtain(mHandler);
		message.what = what;
		return message;
	}
}
