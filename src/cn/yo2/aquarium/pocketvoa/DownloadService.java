package cn.yo2.aquarium.pocketvoa;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DownloadService extends Service {
	private static final String CLASSTAG = DownloadService.class.getSimpleName();
	
	private DatabaseHelper mDatabaseHelper;
	private ExecutorService mExecutorService;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

	@Override
	public void onCreate() {
		super.onCreate();
		
		mDatabaseHelper = new DatabaseHelper(this);
		mDatabaseHelper.open();
		
		mExecutorService = Executors.newSingleThreadExecutor();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mDatabaseHelper.close();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		long now = System.currentTimeMillis();
		
		Article article = Utils.getArticleFromIntent(intent);
		DownloadTask task = new DownloadTask(mDatabaseHelper, article);
		NotificationProgressListener listener = new NotificationProgressListener(this, article, (int) now);
		listener.setWait();
		task.addProgressListener(listener);
		// add to execution queue
		mExecutorService.execute(task);
	}
	
	
	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}
	
}
