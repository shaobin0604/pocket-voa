package cn.yo2.aquarium.pocketvoa;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DownloadService extends Service {
	private static final String CLASSTAG = DownloadService.class.getSimpleName();
	
	private App mApp;
	
	
	private DatabaseHelper mDatabaseHelper;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mApp = (App) getApplication();
		
		mDatabaseHelper = new DatabaseHelper(this);
		mDatabaseHelper.open();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mDatabaseHelper.close();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		Article article = Utils.getArticleFromIntent(intent);
		DownloadTask task = new DownloadTask(mApp.mHttpClient, mDatabaseHelper, article);
		task.addProgressListener(new NotificationProgressListener(this, article));
		new Thread(task).start();
	}
	
	

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}
	
}
