package cn.yo2.aquarium.pocketvoa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Show extends Activity {
	private static final String CLASSTAG = Show.class.getSimpleName();

	private static final String HTML_DEC = "<!DOCTYPE html PUBliC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\" >"
			+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" >"
			+ "<head><title>";

	private static final int PROGRESS_DIALOG_SPIN = 1;
	private static final int ALERT_DIALOG = 2;
	private static final int PROGRESS_DIALOG_BAR = 3;

	private static final int MENU_DOWNLOAD_AUDIO = Menu.FIRST;
	private static final int MENU_DOWNLOAD_TEXT = Menu.FIRST + 1;
	private static final int MENU_DOWNLOAD_BOTH = Menu.FIRST + 2;

	protected static final int WHAT_SUCCESS = 0;
	protected static final int WHAT_FAIL_IO = 1;
	protected static final int WHAT_PLAYER_PROGRESS = 2;

	protected static final int WHAT_DOWNLOAD_PROGRESS = 3;
	protected static final int WHAT_DOWNLOAD_ERROR = 4;
	protected static final int WHAT_DOWNLOAD_SUCCESS = 5;

	private ProgressDialog mProgressDialogSpin;
	private ProgressDialog mProgressDialogBar;

	private WebView mWebView;
	private ImageButton mBtnStart;
	private ImageButton mBtnPause;
	private TextView mTvEllapsedTime;
	private TextView mTvTotalTime;
	private ProgressBar mProgressBar;

	private int mPlayProgress; // 1..100
	private int mTotalTime; // in millis
	private int mEllapsedTime; // in millis

	private StringBuilder mRecycle = new StringBuilder(10);

	private MediaPlayer mMediaPlayer;
	// private AsyncPlayer mAsyncPlayer;

	private boolean mIsIdle = true;
	private boolean mIsPlaying = false;

	private Long mId;
	private String mTitle;
	private String mText;
	private String mMp3;
	private String mUrl;
	private String mType;
	private String mSubtype;
	private String mDate;

	private DatabaseHelper mDatabaseHelper;

	private Handler mDownloadHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_DOWNLOAD_PROGRESS:
				mProgressDialogBar.setProgress(msg.arg1);
				break;
			case WHAT_DOWNLOAD_SUCCESS:
				dismissDialog(PROGRESS_DIALOG_BAR);
				Toast.makeText(Show.this, "Download Complete",
						Toast.LENGTH_SHORT);
				break;
			case WHAT_DOWNLOAD_ERROR:
				dismissDialog(PROGRESS_DIALOG_BAR);
				showDialog(ALERT_DIALOG);
				break;
			default:
				break;
			}
		}
	};

	private Handler mPlayerHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_PLAYER_PROGRESS:
				if (mIsPlaying) {
					mEllapsedTime = mMediaPlayer.getCurrentPosition();
					Log.d(CLASSTAG, "playing millis -- " + mEllapsedTime
							+ " duration -- " + mTotalTime);
					updateProgressBar();

					mPlayProgress = mEllapsedTime * 100 / mTotalTime;
					Log.d(CLASSTAG, "playing progress -- " + mPlayProgress);
					updateEllapsedTime();

					mPlayerHandler
							.sendEmptyMessageDelayed(WHAT_PLAYER_PROGRESS, 1000);
				}
				break;

			default:
				break;
			}
		}

	};

	private OnClickListener mStartButtonClickListener = new OnClickListener() {

		public void onClick(View v) {
			if (mIsIdle) {
				Uri uri = Uri.parse("http://www.51voa.com" + mMp3);
				Log.d(CLASSTAG, "mp3 url -- " + uri);
				try {
					mMediaPlayer.setDataSource(Show.this, uri);
					mIsIdle = false;
					mMediaPlayer.prepareAsync();
					mIsPlaying = true;
					mBtnStart.setEnabled(!mIsPlaying);
					mBtnPause.setEnabled(mIsPlaying);
				} catch (IllegalArgumentException e) {
					Log.e(CLASSTAG, "mp3 url -- " + uri, e);
				} catch (SecurityException e) {
					Log.e(CLASSTAG, "mp3 url -- " + uri, e);
				} catch (IllegalStateException e) {
					Log.e(CLASSTAG, "mp3 url -- " + uri, e);
				} catch (IOException e) {
					Log.e(CLASSTAG, "mp3 url -- " + uri, e);
				}

			} else {
				mMediaPlayer.start();
				mIsPlaying = true;
				mBtnStart.setEnabled(!mIsPlaying);
				mBtnPause.setEnabled(mIsPlaying);
				mPlayerHandler.sendEmptyMessage(WHAT_PLAYER_PROGRESS);
			}

		}
	};

	private OnClickListener mPauseButtonClickListener = new OnClickListener() {

		public void onClick(View v) {
			mMediaPlayer.pause();
			mIsPlaying = false;
			mBtnStart.setEnabled(!mIsPlaying);
			mBtnPause.setEnabled(mIsPlaying);
		}
	};

	private OnErrorListener mErrorListener = new OnErrorListener() {

		public boolean onError(MediaPlayer mp, int what, int extra) {
			Log.e(CLASSTAG, "what -- " + what + " extra -- " + extra);
			return false;
		}
	};

	private OnPreparedListener mPreparedListener = new OnPreparedListener() {

		public void onPrepared(MediaPlayer mp) {
			Log.d(CLASSTAG, "media prepared");
			mTotalTime = mp.getDuration();
			updateTotalTime();
			mp.start();
			mPlayerHandler.sendEmptyMessage(WHAT_PLAYER_PROGRESS);
		}
	};

	private OnBufferingUpdateListener mBufferingUpdateListener = new OnBufferingUpdateListener() {

		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			mProgressBar.setSecondaryProgress(percent);
		}
	};

	private OnCompletionListener mCompletionListener = new OnCompletionListener() {

		public void onCompletion(MediaPlayer mp) {
			Log.d(CLASSTAG, "complete");
			mIsPlaying = false;
			mBtnStart.setEnabled(!mIsPlaying);
			mBtnPause.setEnabled(mIsPlaying);
			mEllapsedTime = 0;
			mPlayProgress = 0;

			updateEllapsedTime();
			updateProgressBar();
		}
	};

	private void updateProgressBar() {
		mProgressBar.setProgress(mPlayProgress);
	}

	private void updateEllapsedTime() {
		mTvEllapsedTime.setText(DateUtils.formatElapsedTime(mRecycle,
				mEllapsedTime / 1000));
	}

	private void updateTotalTime() {
		mTvTotalTime.setText(DateUtils.formatElapsedTime(mRecycle,
				mTotalTime / 1000));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, MENU_DOWNLOAD_TEXT, Menu.NONE, "Download Text");
		menu.add(Menu.NONE, MENU_DOWNLOAD_AUDIO, Menu.NONE, "Download Audio");
		menu.add(Menu.NONE, MENU_DOWNLOAD_BOTH, Menu.NONE, "Download Both");
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mDatabaseHelper.createArticle(mTitle, "20091203", "Standard English",
				"Standard English", mUrl, mMp3);
		switch (item.getItemId()) {
		case MENU_DOWNLOAD_TEXT:
			saveText();
			return true;
		case MENU_DOWNLOAD_AUDIO:
			asynSaveAudio();
			return true;
		case MENU_DOWNLOAD_BOTH:
			saveText();
			asynSaveAudio();
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void asynSaveAudio() {

		showDialog(PROGRESS_DIALOG_BAR);
		new Thread() {

			@Override
			public void run() {

				try {
					saveAudio();
				} catch (IOException e) {
					Log.e(CLASSTAG, "Error when save audio.", e);
					mDownloadHandler.sendEmptyMessage(WHAT_DOWNLOAD_ERROR);
				}

			}

		}.start();
	}

	private void saveAudio() throws IOException {
		File appDir = getAppDir();
		int timeoutConnection = 3000;
		int timeoutSocket = 1000 * 10;

		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, timeoutConnection);
		HttpConnectionParams.setSoTimeout(params, timeoutSocket);

		DefaultHttpClient client = null;
		FileOutputStream fos = null;
		InputStream is = null;
		try {
			if (appDir == null)
				throw new IOException("Cannot get app dir");
			File savedAudio = new File(appDir, extractFilename(mMp3));
			if (!savedAudio.exists())
				savedAudio.createNewFile();
			fos = new FileOutputStream(savedAudio);
			client = new DefaultHttpClient(params);
			HttpGet get = new HttpGet("http://www.51voa.com" + mMp3);
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			long length = entity.getContentLength();
			Log.d(CLASSTAG, "content-length: " + length);
			is = entity.getContent();
			byte[] buffer = new byte[1024];
			int len = 0;
			int read = 0;
			while ((len = is.read(buffer)) != -1) {
				read += len;
				fos.write(buffer, 0, len);
				Message message = Message.obtain(mDownloadHandler);
				message.what = WHAT_DOWNLOAD_PROGRESS;
				message.arg1 = (int) (read * 100 / length);
				mDownloadHandler.sendMessage(message);
			}
			mDownloadHandler.sendEmptyMessage(WHAT_DOWNLOAD_SUCCESS);
		} catch (IOException e) {
			throw e;
		} finally {
			if (fos != null)
				fos.close();
			if (is != null)
				is.close();
			client.getConnectionManager().shutdown();
		}

	}

	private String extractFilename(String url) {
		return url.substring(url.lastIndexOf('/') + 1);
	}

	private File getAppDir() {
		if (IsExternalStorageReady()) {
			File appDir = new File(Environment.getExternalStorageDirectory(),
					"pocket-voa");
			if (appDir.exists()) {
				return appDir;

			} else {
				if (appDir.mkdir())
					return appDir;
				else
					return null;
			}
		} else
			return null;
	}

	private boolean IsExternalStorageReady() {
		return (Environment.getExternalStorageState()
				.equals(Environment.MEDIA_MOUNTED));
	}

	private void saveText() {
		if (TextUtils.isEmpty(mText)) {
			Log.e(CLASSTAG, "mHtml is empty");
			showDialog(ALERT_DIALOG);
		} else {
			FileWriter fos = null;
			File appDir = getAppDir();
			try {
				if (appDir == null)
					throw new IOException("Cannot get App dir");
				File downloadFile = new File(appDir, extractFilename(mUrl));

				if (!downloadFile.exists())
					if (!downloadFile.createNewFile())
						throw new IOException("Cannot create file");

				fos = new FileWriter(downloadFile);
				fos.write(mText);
			} catch (IOException e) {
				Log.e(CLASSTAG, "Error when save text.", e);
				showDialog(ALERT_DIALOG);
			} finally {
				if (fos != null)
					try {
						fos.close();
					} catch (IOException e) {
						Log.e(CLASSTAG, "Error when close fos", e);
					}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show);
		
		getIntentExtras();
		
		// set up controls
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.loadData(mText, "text/html", "utf-8");

		mBtnStart = (ImageButton) findViewById(R.id.btn_start);
		mBtnStart.setOnClickListener(mStartButtonClickListener);

		mBtnPause = (ImageButton) findViewById(R.id.btn_pause);
		mBtnPause.setOnClickListener(mPauseButtonClickListener);

		mTvEllapsedTime = (TextView) findViewById(R.id.tv_ellapsed_time);
		mTvTotalTime = (TextView) findViewById(R.id.tv_total_time);

		mProgressBar = (ProgressBar) findViewById(R.id.pb_audio);

		mMediaPlayer = new MediaPlayer();

		mMediaPlayer.setOnErrorListener(mErrorListener);
		mMediaPlayer.setOnPreparedListener(mPreparedListener);
		mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
		mMediaPlayer.setOnCompletionListener(mCompletionListener);
		
		// set up database
		mDatabaseHelper = new DatabaseHelper(this);
		mDatabaseHelper.open();
	}
	
	private void getIntentExtras() {
		Intent intent = getIntent();
		mId = intent.getLongExtra(Article.K_ID, -1);
		mTitle = intent.getStringExtra(Article.K_TITLE);
		mText = intent.getStringExtra(Article.K_TEXT);
		mUrl = intent.getStringExtra(Article.K_URL);
		mMp3 = intent.getStringExtra(Article.K_MP3);
		mDate = intent.getStringExtra(Article.K_DATE);
		mType = intent.getStringExtra(Article.K_TYPE);
		mSubtype = intent.getStringExtra(Article.K_SUBTYPE);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_DIALOG_SPIN:
			mProgressDialogSpin = new ProgressDialog(this);
			mProgressDialogSpin.setCancelable(false);
			mProgressDialogSpin.setTitle("Loading");
			mProgressDialogSpin.setMessage("Loading");
			return mProgressDialogSpin;
		case PROGRESS_DIALOG_BAR:
			mProgressDialogBar = new ProgressDialog(this);
			mProgressDialogBar.setTitle("Downloading audio file");
			mProgressDialogBar
					.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			return mProgressDialogBar;
		case ALERT_DIALOG:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Alert");
			return builder.create();
		default:
			break;
		}
		return super.onCreateDialog(id);
	}

	private void loadPage(String url) throws IOException {
		int timeoutConnection = 3000;
		int timeoutSocket = 1000 * 300;

		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, timeoutConnection);
		HttpConnectionParams.setSoTimeout(params, timeoutSocket);

		DefaultHttpClient client = new DefaultHttpClient(params);
		// ///////////////////

		HttpGet get = new HttpGet(url);
		ResponseHandler<String> handler = new BasicResponseHandler();

		try {
			parse(client.execute(get, handler));
		} catch (ClientProtocolException e) {
			Log.e(CLASSTAG, "Error when execute http get.", e);
			throw e;
		} catch (IOException e) {
			Log.e(CLASSTAG, "Error when execute http get.", e);
			throw e;
		} finally {
			get.abort();
			client.getConnectionManager().shutdown();
		}
	}

	private void parse(String body) {

		int contentStart = body.indexOf("<div id=\"content\"");
		int listadsStart = body.indexOf("<div id=\"listads\"");

		String content = body.substring(contentStart, listadsStart);

		Pattern audioPattern = Pattern.compile("Player\\(\"([^\\s]+)\"\\)",
				Pattern.CASE_INSENSITIVE);

		Matcher audioMatcher = audioPattern.matcher(content);
		if (audioMatcher.find()) {
			mMp3 = audioMatcher.group(1);
		}

		int textStart = 0;

		Pattern bylinePattern = Pattern.compile(
				"<span\\s+class=\"?byline\"?\\s*>", Pattern.CASE_INSENSITIVE);

		Matcher bylineMatcher = bylinePattern.matcher(content);
		if (bylineMatcher.find()) {
			textStart = bylineMatcher.start();
		}

		String text = content.substring(textStart).replaceAll(
				"src=([\"\']?)(/[^\\s\'\">]+(?:\\.jpg|\\.png|\\.bmp|\\.gif))\\1?",
				"src=\"" + "http://www.51voa.com" + "$2\"");

		mText = buildHtml(mTitle, text);
	}

	private String buildHtml(String title, String text) {
		StringBuilder s = new StringBuilder(HTML_DEC);
		s.append(title);
		s.append("</title></head><body><h1>");
		s.append(title);
		s.append("</h1><div id=\"content\">");
		s.append(text);
		s.append("</body></html>");

		return s.toString();
	}

	@Override
	protected void onPause() {
		mMediaPlayer.pause();
		mIsPlaying = false;
		mBtnStart.setEnabled(!mIsPlaying);
		mBtnPause.setEnabled(mIsPlaying);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mMediaPlayer.release();
		mDatabaseHelper.close();
		super.onDestroy();
	}
}
