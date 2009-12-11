package cn.yo2.aquarium.pocketvoa;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
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

	private static final int DLG_PROGRESS_SPIN = 1;
	private static final int DLG_ERROR_ALERT = 2;
	private static final int DLG_PROGRESS_BAR = 3;
	private static final int DLG_CONFIRM_DOWNLOAD = 4;

	private static final int MENU_DOWNLOAD = Menu.FIRST;

	protected static final int WHAT_LOAD_PAGE_SUCCESS = 0;
	protected static final int WHAT_LOAD_PAGE_FAIL_IO = 1;
	protected static final int WHAT_LOAD_PAGE_FAIL_PARSE = 2;

	protected static final int WHAT_PLAYER_PROGRESS = 3;

	private enum MediaPlayerState {
		Idle, Initialized, Preparing, Prepared, Started, Paused, Stopped, PlaybackCompleted, End, Error,
	}

	private enum Error {
		LoadPageError, DownloadAudioError, PlayAudioError, DownloadTextError,
	}

	private Error mLastError;

	private App mApp;

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
				mWebView.loadDataWithBaseURL("", mApp.article.text,
						"text/html", "utf-8", "");
				dismissDialog(DLG_PROGRESS_SPIN);
				break;
			case WHAT_LOAD_PAGE_FAIL_IO:
			case WHAT_LOAD_PAGE_FAIL_PARSE:
				dismissDialog(DLG_PROGRESS_SPIN);
				mLastError = Error.LoadPageError;
				showDialog(DLG_ERROR_ALERT);
				break;
			default:
				break;
			}
		}

	};

	private Handler mDownloadMp3Handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Downloader.WHAT_DOWNLOAD_PROGRESS:
				// msg.arg1 store progress
				mProgressDialogBar.setProgress(msg.arg1);
				break;
			case Downloader.WHAT_DOWNLOAD_SUCCESS:
				dismissDialog(DLG_PROGRESS_BAR);
				Toast.makeText(Show.this,
						R.string.toast_download_audio_complete,
						Toast.LENGTH_SHORT).show();
				break;
			case Downloader.WHAT_DOWNLOAD_ERROR:
				dismissDialog(DLG_PROGRESS_BAR);
				mLastError = Error.DownloadAudioError;
				showDialog(DLG_ERROR_ALERT);
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
				if (mApp.article.id == null)
					uri = Uri.parse(mApp.article.mp3);
				else
					uri = Uri.fromFile(mApp.mDownloader
							.localMp3File(mApp.article));
				Log.d(CLASSTAG, "mp3 url -- " + uri);
				try {

					mMediaPlayer.setDataSource(Show.this, uri);
					mMediaPlayerState = MediaPlayerState.Initialized;

					mMediaPlayer.prepareAsync();
					mMediaPlayerState = MediaPlayerState.Preparing;

					updatePalyerButton();

				} catch (IllegalArgumentException e) {
					Log.e(CLASSTAG, "mp3 url -- " + uri, e);
					mLastError = Error.PlayAudioError;
					showDialog(DLG_ERROR_ALERT);
				} catch (SecurityException e) {
					Log.e(CLASSTAG, "mp3 url -- " + uri, e);
					mLastError = Error.PlayAudioError;
					showDialog(DLG_ERROR_ALERT);
				} catch (IllegalStateException e) {
					Log.e(CLASSTAG, "mp3 url -- " + uri, e);
					mLastError = Error.PlayAudioError;
					showDialog(DLG_ERROR_ALERT);
				} catch (IOException e) {
					Log.e(CLASSTAG, "mp3 url -- " + uri, e);
					mLastError = Error.PlayAudioError;
					showDialog(DLG_ERROR_ALERT);
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

			mLastError = Error.PlayAudioError;

			showDialog(DLG_ERROR_ALERT);

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
			Log.d(CLASSTAG, "playback completed");
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
		menu.add(Menu.NONE, MENU_DOWNLOAD, Menu.NONE, R.string.menu_download)
				.setIcon(R.drawable.file_download);
		return result;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.getItem(0).setEnabled(mApp.article.id == null);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_DOWNLOAD:
			// TODO check if the article has been downloaded
			if (mDatabaseHelper.isArticleExist(mApp.article)) {
				showDialog(DLG_CONFIRM_DOWNLOAD);
			} else {
				mDatabaseHelper.createArticle(mApp.article.title,
						mApp.article.date, mApp.article.type,
						mApp.article.subtype, mApp.article.url,
						mApp.article.mp3);
				saveText();
				saveMp3();
				
			}
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void saveText() {
		try {
			mApp.mDownloader.downloadText(mApp.article);
			Toast.makeText(this, R.string.toast_download_text_complete,
					Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			mLastError = Error.DownloadTextError;
			showDialog(DLG_ERROR_ALERT);
		}
	}

	private void saveMp3() {

		showDialog(DLG_PROGRESS_BAR);
		new Thread() {

			@Override
			public void run() {

				try {
					mApp.mDownloader.mDownloadHandler = mDownloadMp3Handler;
					mApp.mDownloader.downloadMp3(mApp.article);
					mDownloadMp3Handler
							.sendEmptyMessage(Downloader.WHAT_DOWNLOAD_SUCCESS);
				} catch (IOException e) {
					Log.e(CLASSTAG, "Error when save audio.", e);
					mDownloadMp3Handler
							.sendEmptyMessage(Downloader.WHAT_DOWNLOAD_ERROR);
				}

			}

		}.start();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show);

		mApp = (App) getApplication();
		
		setTitle(mApp.article.title);

		// set up controls
		setupWidgets();

		// set up media player
		setupMediaPlayer();

		// set up database
		mDatabaseHelper = new DatabaseHelper(this);
		mDatabaseHelper.open();

		// load page
		if (mApp.article.id == null)
			loadRemotePage();
		else
			loadLocalPage();
	}

	private void setupWidgets() {
		mWebView = (WebView) findViewById(R.id.webview);

		mBtnStart = (ImageButton) findViewById(R.id.btn_start);
		mBtnStart.setOnClickListener(mStartButtonClickListener);

		mBtnPause = (ImageButton) findViewById(R.id.btn_pause);
		mBtnPause.setOnClickListener(mPauseButtonClickListener);

		mTvEllapsedTime = (TextView) findViewById(R.id.tv_ellapsed_time);
		mTvTotalTime = (TextView) findViewById(R.id.tv_total_time);

		mProgressBar = (ProgressBar) findViewById(R.id.pb_audio);
	}

	private void setupMediaPlayer() {
		mMediaPlayer = new MediaPlayer();
		mMediaPlayerState = MediaPlayerState.Idle;

		mMediaPlayer.setOnErrorListener(mErrorListener);
		mMediaPlayer.setOnPreparedListener(mPreparedListener);
		mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
		mMediaPlayer.setOnCompletionListener(mCompletionListener);
	}

	private void loadLocalPage() {
		showDialog(DLG_PROGRESS_SPIN);
		new Thread() {

			@Override
			public void run() {
				try {
					mApp.article.text = mApp.mDownloader.loadText(mApp.article);
					mLoadPageHandler.sendEmptyMessage(WHAT_LOAD_PAGE_SUCCESS);
				} catch (IOException e) {
					mLoadPageHandler.sendEmptyMessage(WHAT_LOAD_PAGE_FAIL_IO);
				}
			}

		}.start();
	}

	private void loadRemotePage() {
		showDialog(DLG_PROGRESS_SPIN);
		new Thread() {

			@Override
			public void run() {
				mApp.mPageGenerator.mParser = mApp.mPageParsers
						.get(mApp.article.type + "_" + mApp.article.subtype);
				try {
					mApp.mPageGenerator.getArticle(mApp.article);
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
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DLG_PROGRESS_SPIN:
			mProgressDialogSpin = new ProgressDialog(this);
			mProgressDialogSpin
					.setMessage(getString(R.string.progressspin_loadpage_msg));
			return mProgressDialogSpin;
		case DLG_PROGRESS_BAR:
			mProgressDialogBar = new ProgressDialog(this);
			mProgressDialogBar.setTitle(R.string.progressbar_download_title);
			mProgressDialogBar
					.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			return mProgressDialogBar;
		case DLG_ERROR_ALERT:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setTitle(R.string.alert_title_error);
			// without this statement, you would not be able to change AlertDialog's message in onPreparedDialog
			builder.setMessage("");
			builder.setNeutralButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			});
			return builder.create();
		case DLG_CONFIRM_DOWNLOAD:
			AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
			builder2.setIcon(android.R.drawable.ic_dialog_alert);
			builder2.setTitle(R.string.alert_title_confirm_download);
			// without this statement, you would not be able to change AlertDialog's message in onPreparedDialog
			builder2.setMessage("");
			builder2.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					
					saveText();
					saveMp3();
				}
			});
			
			builder2.setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					
				}
			});
			
			return builder2.create();
		default:
			break;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case DLG_ERROR_ALERT:
			AlertDialog alertDialog = (AlertDialog) dialog;
			switch (mLastError) {
			case LoadPageError:
				alertDialog
						.setMessage(getString(R.string.alert_msg_loadpage_error));
				break;
			case PlayAudioError:
				alertDialog
						.setMessage(getString(R.string.alert_msg_play_audio_error));
				break;
			case DownloadAudioError:
				alertDialog
						.setMessage(getString(R.string.alert_msg_download_audio_error));
				break;
			case DownloadTextError:
				alertDialog
						.setMessage(getString(R.string.alert_msg_download_text_error));
				break;
			default:
				break;
			}
			break;
		case DLG_CONFIRM_DOWNLOAD:
			AlertDialog alertDialog2 = (AlertDialog) dialog;
			alertDialog2.setMessage(getString(R.string.alert_msg_confirm_download, mApp.article.title));
			break;
		default:
			break;
		}
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
