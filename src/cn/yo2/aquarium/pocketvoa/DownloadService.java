package cn.yo2.aquarium.pocketvoa;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DownloadService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		Article article = new Article();
		
		article.title = intent.getStringExtra(Article.K_TITLE);
		article.text = intent.getStringExtra(Article.K_TEXT);
		article.mp3 = intent.getStringExtra(Article.K_MP3);
		article.type = intent.getStringExtra(Article.K_TYPE);
		article.subtype = intent.getStringExtra(Article.K_SUBTYPE);
		article.url = intent.getStringExtra(Article.K_URL);
		article.date = intent.getStringExtra(Article.K_DATE);
		
		int icon = android.R.drawable.stat_sys_download;        						// icon from resources
		CharSequence tickerText = "Downloading \"" + article.title + "\"";              // ticker-text
		long when = System.currentTimeMillis();         								// notification time
		Intent notificationIntent = new Intent(this, Main.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		// the next two lines initialize the Notification, using the configurations above
		Notification notification = new Notification(icon, tickerText, when);
		
		CharSequence contentTitle = tickerText;  		// expanded message title
		CharSequence contentText = "Downloading ";      	// expanded message text
		notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);

	}
	
	

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}
	
}
