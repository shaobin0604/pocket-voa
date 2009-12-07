package cn.yo2.aquarium.pocketvoa;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
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

	private static final int PROGRESS_DIALOG_SPIN = 1;
	private static final int ALERT_DIALOG = 2;
	private static final int PROGRESS_DIALOG_BAR = 3;

	private static final int MENU_DOWNLOAD_AUDIO = Menu.FIRST;
	private static final int MENU_DOWNLOAD_TEXT = Menu.FIRST + 1;
	private static final int MENU_DOWNLOAD_BOTH = Menu.FIRST + 2;

	protected static final int WHAT_LOAD_PAGE_SUCCESS = 0;
	protected static final int WHAT_LOAD_PAGE_FAIL_IO = 1;
	protected static final int WHAT_LOAD_PAGE_FAIL_PARSE = 2;

	protected static final int WHAT_PLAYER_PROGRESS = 3;

	enum MediaPlayerState {
		Idle, Initialized, Preparing, Prepared, Started, Paused, Stopped, PlaybackCompleted, End, Error,
	}

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

	private MediaPlayerState mMediaPlayerState;

	private DatabaseHelper mDatabaseHelper;

	private Handler mLoadPageHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_LOAD_PAGE_SUCCESS:
				mWebView.loadData(App.article.text, "text/html", "utf-8");
				dismissDialog(PROGRESS_DIALOG_SPIN);
				break;
			case WHAT_LOAD_PAGE_FAIL_IO:
				Toast.makeText(Show.this, "Fail IO", Toast.LENGTH_SHORT).show();
				break;
			case WHAT_LOAD_PAGE_FAIL_PARSE:
				Toast.makeText(Show.this, "Fail PARSE", Toast.LENGTH_SHORT)
						.show();
				break;
			default:
				break;
			}
		}

	};

	private Handler mDownloadHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Downloader.WHAT_DOWNLOAD_PROGRESS:
				mProgressDialogBar.setProgress(msg.arg1);
				break;
			case Downloader.WHAT_DOWNLOAD_SUCCESS:
				dismissDialog(PROGRESS_DIALOG_BAR);
				Toast.makeText(Show.this, "Download Complete",
						Toast.LENGTH_SHORT).show();
				break;
			case Downloader.WHAT_DOWNLOAD_ERROR:
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
				if (mMediaPlayerState == MediaPlayerState.Started) {
					mEllapsedTime = mMediaPlayer.getCurrentPosition();
					Log.d(CLASSTAG, "playing millis -- " + mEllapsedTime
							+ " duration -- " + mTotalTime);
					updateProgressBar();

					mPlayProgress = mEllapsedTime * 100 / mTotalTime;
					Log.d(CLASSTAG, "playing progress -- " + mPlayProgress);
					updateEllapsedTime();

					mPlayerHandler.sendEmptyMessageDelayed(
							WHAT_PLAYER_PROGRESS, 1000);
				}
				break;

			default:
				break;
			}
		}

	};

	private OnClickListener mStartButtonClickListener = new OnClickListener() {

		public void onClick(View v) {
			if (mMediaPlayerState == MediaPlayerState.Idle) {
				Uri uri = null;
				if (App.article.id == null) 
					uri = Uri.parse(App.article.mp3);
				else 
					uri = Uri.fromFile(App.DOWNLOADER.localMp3File(App.article));
				Log.d(CLASSTAG, "mp3 url -- " + uri);
				try {

					mMediaPlayer.setDataSource(Show.this, uri);
					mMediaPlayerState = MediaPlayerState.Initialized;

					mMediaPlayer.prepareAsync();
					mMediaPlayerState = MediaPlayerState.Preparing;

					updatePalyerButton();

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
				mMediaPlayerState = MediaPlayerState.Started;
				updatePalyerButton();
				mPlayerHandler.sendEmptyMessage(WHAT_PLAYER_PROGRESS);
			}

		}
	};

	private OnClickListener mPauseButtonClickListener = new OnClickListener() {

		public void onClick(View v) {
			mMediaPlayer.pause();
			mMediaPlayerState = MediaPlayerState.Paused;

			updatePalyerButton();
		}
	};

	private OnErrorListener mErrorListener = new OnErrorListener() {

		public boolean onError(MediaPlayer mp, int what, int extra) {
			Log.e(CLASSTAG, "what -- " + what + " extra -- " + extra);
			mMediaPlayer.reset();
			mMediaPlayerState = MediaPlayerState.Idle;
			return true;
		}
	};

	private OnPreparedListener mPreparedListener = new OnPreparedListener() {

		public void onPrepared(MediaPlayer mp) {
			mMediaPlayerState = MediaPlayerState.Prepared;
			updatePalyerButton();

			Log.d(CLASSTAG, "media prepared");

			mTotalTime = mp.getDuration();
			updateTotalTime();

			mp.start();
			mMediaPlayerState = MediaPlayerState.Started;
			updatePalyerButton();

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
			mMediaPlayerState = MediaPlayerState.PlaybackCompleted;

			updatePalyerButton();

			mEllapsedTime = 0;
			mPlayProgress = 0;

			updateEllapsedTime();
			updateProgressBar();
		}
	};

	private void updatePalyerButton() {
		switch (mMediaPlayerState) {
		case Preparing:
		case Prepared:
			mBtnStart.setEnabled(false);
			mBtnPause.setEnabled(false);
			break;
		case Started:
			mBtnStart.setEnabled(false);
			mBtnPause.setEnabled(true);
			break;
		case Paused:
		case Stopped:
		case PlaybackCompleted:
			mBtnStart.setEnabled(true);
			mBtnPause.setEnabled(false);
			break;
		default:
			break;
		}
	}

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
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.getItem(0).setEnabled(App.article.id == null);
		menu.getItem(1).setEnabled(App.article.id == null);
		menu.getItem(2).setEnabled(App.article.id == null);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mDatabaseHelper.createArticle(App.article.title, App.article.date,
				App.article.type, App.article.subtype, App.article.url,
				App.article.mp3);
		switch (item.getItemId()) {
		case MENU_DOWNLOAD_TEXT:
			saveText();
			return true;
		case MENU_DOWNLOAD_AUDIO:
			saveMp3();
			return true;
		case MENU_DOWNLOAD_BOTH:
			saveText();
			saveMp3();
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void saveText() {
		try {
			App.DOWNLOADER.downloadText(App.article);
			Toast.makeText(this, "Download Text complete.", Toast.LENGTH_SHORT)
					.show();
		} catch (IOException e) {
			showDialog(ALERT_DIALOG);
		}
	}

	private void saveMp3() {

		showDialog(PROGRESS_DIALOG_BAR);
		new Thread() {

			@Override
			public void run() {

				try {
					App.DOWNLOADER.mDownloadHandler = Show.this.mDownloadHandler;
					App.DOWNLOADER.downloadMp3(App.article);
					mDownloadHandler
							.sendEmptyMessage(Downloader.WHAT_DOWNLOAD_SUCCESS);
				} catch (IOException e) {
					Log.e(CLASSTAG, "Error when save audio.", e);
					mDownloadHandler
							.sendEmptyMessage(Downloader.WHAT_DOWNLOAD_ERROR);
				}

			}

		}.start();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show);

		// set up controls
		mWebView = (WebView) findViewById(R.id.webview);

		mBtnStart = (ImageButton) findViewById(R.id.btn_start);
		mBtnStart.setOnClickListener(mStartButtonClickListener);

		mBtnPause = (ImageButton) findViewById(R.id.btn_pause);
		mBtnPause.setOnClickListener(mPauseButtonClickListener);

		mTvEllapsedTime = (TextView) findViewById(R.id.tv_ellapsed_time);
		mTvTotalTime = (TextView) findViewById(R.id.tv_total_time);

		mProgressBar = (ProgressBar) findViewById(R.id.pb_audio);

		mMediaPlayer = new MediaPlayer();
		mMediaPlayerState = MediaPlayerState.Idle;

		mMediaPlayer.setOnErrorListener(mErrorListener);
		mMediaPlayer.setOnPreparedListener(mPreparedListener);
		mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
		mMediaPlayer.setOnCompletionListener(mCompletionListener);

		// set up database
		mDatabaseHelper = new DatabaseHelper(this);
		mDatabaseHelper.open();

		// load page
		if (App.article.id == null)
			loadRemotePage();
		else
			loadLocalPage();
	}

	private void loadLocalPage() {
		showDialog(PROGRESS_DIALOG_SPIN);
		new Thread() {

			@Override
			public void run() {
				try {
					App.article.text = App.DOWNLOADER.loadText(App.article);
					mLoadPageHandler.sendEmptyMessage(WHAT_LOAD_PAGE_SUCCESS);
				} catch (IOException e) {
					mLoadPageHandler.sendEmptyMessage(WHAT_LOAD_PAGE_FAIL_IO);
				}
			}

		}.start();
	}

	private void loadRemotePage() {
		showDialog(PROGRESS_DIALOG_SPIN);
		new Thread() {

			@Override
			public void run() {
				App.PAGE_GENERATOR.mParser = App.PAGE_PARSERS
						.get(App.article.type + "_" + App.article.subtype);
				try {
					App.PAGE_GENERATOR.getArticle(App.article);
					mLoadPageHandler.sendEmptyMessage(WHAT_LOAD_PAGE_SUCCESS);
				} catch (IOException e) {
					mLoadPageHandler.sendEmptyMessage(WHAT_LOAD_PAGE_FAIL_IO);
				} catch (IllegalContentFormatException e) {
					mLoadPageHandler
							.sendEmptyMessage(WHAT_LOAD_PAGE_FAIL_PARSE);
				}
			}

		}.start();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_DIALOG_SPIN:
			mProgressDialogSpin = new ProgressDialog(this);
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

	@Override
	protected void onPause() {
		switch (mMediaPlayerState) {
		case Started:
		case Paused:
			mMediaPlayer.pause();
			mMediaPlayerState = MediaPlayerState.Paused;
			break;
		default:
			break;
		}

		updatePalyerButton();

		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mMediaPlayer.release();
		mMediaPlayerState = MediaPlayerState.End;
		mDatabaseHelper.close();
		super.onDestroy();
	}

}
