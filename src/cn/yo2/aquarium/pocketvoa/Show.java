package cn.yo2.aquarium.pocketvoa;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Show extends Activity {
	private static final String TAG = Show.class.getSimpleName();

	private static final String HTML_DEC = "<!DOCTYPE html PUBliC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\" >"
			+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" >"
			+ "<head><title>";

	private static final int PROGRESS_DIALOG = 1;

	protected static final int WHAT_SUCCESS = 0;
	protected static final int WHAT_FAIL_IO = 1;
	protected static final int WHAT_PROGRESS = 2;

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

	private String mTitle;
	private String mHtml;
	private String mAudioUrl;
	private String mUrl;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_SUCCESS:
				updateWebView();
				dismissDialog(PROGRESS_DIALOG);
				break;
			case WHAT_FAIL_IO:
				Toast.makeText(Show.this, "FAIL IO", Toast.LENGTH_LONG);
				break;

			case WHAT_PROGRESS:
				if (mIsPlaying) {
					mEllapsedTime = mMediaPlayer.getCurrentPosition();
					Log.d(TAG, "playing millis -- " + mEllapsedTime
							+ " duration -- " + mTotalTime);
					updateProgressBar();
					
					mPlayProgress = mEllapsedTime * 100 / mTotalTime;
					Log.d(TAG, "playing progress -- " + mPlayProgress);
					updateEllapsedTime();
					
					mHandler.sendEmptyMessageDelayed(WHAT_PROGRESS, 1000);
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
				Uri uri = Uri.parse("http://www.51voa.com" + mAudioUrl);
				Log.d(TAG, "mp3 url -- " + uri);
				try {
					mMediaPlayer.setDataSource(Show.this, uri);
					mIsIdle = false;
					mMediaPlayer.prepareAsync();
					mIsPlaying = true;
					mBtnStart.setEnabled(!mIsPlaying);
					mBtnPause.setEnabled(mIsPlaying);
				} catch (IllegalArgumentException e) {
					Log.e(TAG, "mp3 url -- " + uri, e);
				} catch (SecurityException e) {
					Log.e(TAG, "mp3 url -- " + uri, e);
				} catch (IllegalStateException e) {
					Log.e(TAG, "mp3 url -- " + uri, e);
				} catch (IOException e) {
					Log.e(TAG, "mp3 url -- " + uri, e);
				}

			} else {
				mMediaPlayer.start();
				mIsPlaying = true;
				mBtnStart.setEnabled(!mIsPlaying);
				mBtnPause.setEnabled(mIsPlaying);
				mHandler.sendEmptyMessage(WHAT_PROGRESS);
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
			Log.e(TAG, "what -- " + what + " extra -- " + extra);
			return false;
		}
	};

	private OnPreparedListener mPreparedListener = new OnPreparedListener() {

		public void onPrepared(MediaPlayer mp) {
			Log.d(TAG, "media prepared");
			mTotalTime = mp.getDuration();
			updateTotalTime();
			mp.start();
			mHandler.sendEmptyMessage(WHAT_PROGRESS);
		}
	};

	private OnBufferingUpdateListener mBufferingUpdateListener = new OnBufferingUpdateListener() {

		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			mProgressBar.setSecondaryProgress(percent);
		}
	};

	private OnCompletionListener mCompletionListener = new OnCompletionListener() {

		public void onCompletion(MediaPlayer mp) {
			Log.d(TAG, "complete");
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

	private void updateWebView() {
		mWebView.loadData(mHtml, "text/html", "utf-8");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show);

		mWebView = (WebView) findViewById(R.id.webview);

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

		Bundle bundle = getIntent().getExtras();

		mTitle = bundle.getString(Article.KEY_TITLE);
		mUrl = bundle.getString(Article.KEY_URL);

		refreshWebView();
	}

	private void refreshWebView() {
		showDialog(PROGRESS_DIALOG);
		new Thread() {
			@Override
			public void run() {
				try {
					loadPage("http://www.51voa.com" + mUrl);
					mHandler.sendEmptyMessage(WHAT_SUCCESS);
				} catch (IOException e) {
					mHandler.sendEmptyMessage(WHAT_FAIL_IO);
				}
			}

		}.start();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_DIALOG:
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setCancelable(false);
			progressDialog.setTitle("Loading");
			progressDialog.setMessage("Loading");
			return progressDialog;
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
			Log.e(TAG, "Error when execute http get.", e);
			throw e;
		} catch (IOException e) {
			Log.e(TAG, "Error when execute http get.", e);
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

		Pattern audioPattern = Pattern.compile(
				"Player\\(\"(/path.asp\\?url=[-_/\\d\\w]+\\.mp3)\"\\);",
				Pattern.CASE_INSENSITIVE);

		Matcher audioMatcher = audioPattern.matcher(content);
		if (audioMatcher.find()) {
			mAudioUrl = audioMatcher.group(1);
		}

		int textStart = 0;

		Pattern bylinePattern = Pattern.compile(
				"<span\\s+class=\"?byline\"?\\s*>", Pattern.CASE_INSENSITIVE);

		Matcher bylineMatcher = bylinePattern.matcher(content);
		if (bylineMatcher.find()) {
			textStart = bylineMatcher.start();
		}

		String text = content.substring(textStart).replaceAll(
				"<img\\s+src=\"?([-_/\\d\\w]+\\.jpg)\"?\\s*>",
				"<img src=\"" + "http://www.51voa.com" + "$1\">");

		mHtml = buildHtml(mTitle, text);
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
	protected void onDestroy() {
		mMediaPlayer.release();
		super.onDestroy();
	}
}
