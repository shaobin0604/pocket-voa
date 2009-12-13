package cn.yo2.aquarium.pocketvoa;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationProgressListener implements IProgressListener {
	
	private static final String CLASSTAG = NotificationProgressListener.class.getSimpleName();
	
	private Context mContext;
	
	private Article mArticle;
	
	private Notification mNotificationProgress;
	private Notification mNotificationSuccess;
	private Notification mNotificationError;

	private PendingIntent mContentIntent;
	
	private NotificationManager mNotificationManager;

	public NotificationProgressListener(Context context, Article article) {
		super();
		this.mContext = context;
		this.mArticle = article;
		
		mContentIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext, Main.class), 0);
		
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		
	}

	public void setError(int which, String message) {
		int icon = android.R.drawable.stat_sys_warning;
		CharSequence tickerText = null;
		CharSequence contentTitle = mArticle.title;  		
		CharSequence contentText = null;
		
		switch (which) {
		case DownloadTask.WHICH_DOWNLOAD_MP3:
			tickerText = contentText = mContext.getString(R.string.notification_download_audio_error_ticker);
			break;
		case DownloadTask.WHICH_DOWNLOAD_TEXT:
			tickerText = contentText = mContext.getString(R.string.notification_download_text_error_ticker);
			break;
		}
		
		long when = System.currentTimeMillis();
		if (mNotificationError == null) {
			mNotificationError = new Notification(icon, tickerText, when);
		} else {
			mNotificationError.icon = icon;
			mNotificationError.tickerText = tickerText;
			mNotificationError.when = when;
		}
		
		mNotificationError.flags = Notification.FLAG_AUTO_CANCEL;
		     	
		mNotificationError.setLatestEventInfo(mContext, contentTitle, contentText, mContentIntent);
		
		long threadId = Thread.currentThread().getId();

		mNotificationManager.notify((int)threadId, mNotificationError);
	}

	public void setSuccess(int which) {
		int icon = android.R.drawable.stat_sys_download_done;
		
		CharSequence tickerText = null;
		CharSequence contentTitle = mArticle.title;  		
		CharSequence contentText = null;
		
		switch (which) {
		case DownloadTask.WHICH_DOWNLOAD_MP3:
			tickerText = contentText = mContext.getString(R.string.notification_download_audio_success_ticker);
			break;
		case DownloadTask.WHICH_DOWNLOAD_TEXT:
			tickerText = contentText = mContext.getString(R.string.notification_download_text_success_ticker);
			break;
		}
		
		long when = System.currentTimeMillis();
		if (mNotificationSuccess == null) {
			mNotificationSuccess = new Notification(icon, tickerText, when);
		} else {
			mNotificationSuccess.icon = icon;
			mNotificationSuccess.tickerText = tickerText;
			mNotificationSuccess.when = when;
		}
		 	
		mNotificationSuccess.flags = Notification.FLAG_AUTO_CANCEL;
		
		mNotificationSuccess.setLatestEventInfo(mContext, contentTitle, contentText, mContentIntent);
		
		long threadId = Thread.currentThread().getId();

		mNotificationManager.notify((int)threadId, mNotificationSuccess);
	}

	public void updateProgress(int which, long pos, long total) {

		int icon = android.R.drawable.stat_sys_download;
		
		CharSequence tickerText = null;
		CharSequence contentTitle = mArticle.title;  		
		CharSequence contentText = null;
		
		switch (which) {
		case DownloadTask.WHICH_DOWNLOAD_MP3:
			tickerText = mContext.getString(R.string.notification_download_audio_progress_ticker);
			contentText = mContext.getString(R.string.notification_download_audio_progress_content, 100 * pos / total, pos / 1000, total / 1000);
			break;
		case DownloadTask.WHICH_DOWNLOAD_TEXT:
			tickerText = mContext.getString(R.string.notification_download_text_progress_ticker);
			contentText = mContext.getString(R.string.notification_download_text_progress_content, 100 * pos / total, pos / 1000, total / 1000);
			break;
		}
		
		long when = System.currentTimeMillis();
		if (mNotificationProgress == null) {
			mNotificationProgress = new Notification(icon, tickerText, when);
		} else {
			mNotificationProgress.icon = icon;
			mNotificationProgress.tickerText = tickerText;
		}
		
		mNotificationProgress.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
		    	
		mNotificationProgress.setLatestEventInfo(mContext, contentTitle, contentText, mContentIntent);
		
		long threadId = Thread.currentThread().getId();
		
		mNotificationManager.notify((int)threadId, mNotificationProgress);
	}
}
