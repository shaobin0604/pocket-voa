package cn.yo2.aquarium.pocketvoa;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;
import cn.yo2.aquarium.pocketvoa.lyric.LyricView;

import com.admob.android.ads.AdView;

public class Show extends Activity {
	private static final String TAG = Show.class.getSimpleName();

	private static final String[] KEYWORDS = { "android game farm",
			"food sport", "life auto outdoor", "iphone", };

	// Current View in ViewFlipper
	private static final int VIEW_INVALID = -1;
	private static final int VIEW_ORIGINAL = 0;
	private static final int VIEW_TRANSLATION = 1;
	private static final int VIEW_LYRIC = 2;

	// Activity managed Dialogs
	private static final int DLG_PROGRESS_SPIN = 1;
	private static final int DLG_PROGRESS_BAR = 2;
	private static final int DLG_ERROR = 3;
	private static final int DLG_CONFIRM_DOWNLOAD = 4;

	// Option Menu Groups
	private static final int MENU_REMOTE_GROUP = 1;
	private static final int MENU_LOCAL_GROUP = 2;

	// Option Menus
	private static final int MENU_REMOTE_ORIGINAL = Menu.FIRST;
	private static final int MENU_REMOTE_TRANSLATION = Menu.FIRST + 1;
	private static final int MENU_REMOTE_LYRIC = Menu.FIRST + 2;
	private static final int MENU_REMOTE_DOWNLOAD = Menu.FIRST + 3;

	private static final int MENU_LOCAL_ORIGINAL = Menu.FIRST + 4;
	private static final int MENU_LOCAL_TRANSLATION = Menu.FIRST + 5;
	private static final int MENU_LOCAL_LYRIC = Menu.FIRST + 6;

	// Load remote page handler message type
	private static final int WHAT_LOAD_REMOTE_ORIGINAL_SUCCESS = 0;
	private static final int WHAT_LOAD_REMOTE_ORIGINAL_FAIL_IO = 1;
	private static final int WHAT_LOAD_REMOTE_ORIGINAL_FAIL_PARSE = 2;

	private static final int WHAT_LOAD_REMOTE_TRANSLATION_SUCCESS = 3;
	private static final int WHAT_LOAD_REMOTE_TRANSLATION_FAIL_IO = 4;
	private static final int WHAT_LOAD_REMOTE_TRANSLATION_FAIL_PARSE = 5;

	private static final int WHAT_LOAD_REMOTE_LYRIC_SUCCESS = 6;
	private static final int WHAT_LOAD_REMOTE_LYRIC_FAIL_IO = 7;

	// Load local page handler message type
	private static final int WHAT_LOAD_LOCAL_ORIGINAL_SUCCESS = 0;
	private static final int WHAT_LOAD_LOCAL_ORIGINAL_FAIL_IO = 1;
	private static final int WHAT_LOAD_LOCAL_ORIGINAL_FAIL_PARSE = 2;

	private static final int WHAT_LOAD_LOCAL_TRANSLATION_SUCCESS = 3;
	private static final int WHAT_LOAD_LOCAL_TRANSLATION_FAIL_IO = 4;
	private static final int WHAT_LOAD_LOCAL_TRANSLATION_FAIL_PARSE = 5;

	private static final int WHAT_LOAD_LOCAL_LYRIC_SUCCESS = 6;
	private static final int WHAT_LOAD_LOCAL_LYRIC_FAIL_IO = 7;

	// MediaPlayer handler message type
	private static final int WHAT_LYRIC_UPDATE = 0;

	

	private enum Error {
		LoadRemotePageError, LoadLocalPageError, PlayRemoteAudioError, PlayLocalAudioError, DownloadAudioError, DownloadTextError,
	}

	private int mCurrentView = VIEW_INVALID;
	private int mLastCommand;

	private Error mLastError;

	private App mApp;

	// Article current shown
	private Article mArticle;

	private ProgressDialog mProgressDialogSpin;
	private ProgressDialog mProgressDialogBar;

	private AdView mAdView;
	private ViewFlipper mViewFlipper;
	private WebView mWebViewEn;
	private WebView mWebViewZh;
	private ImageButton mBtnPause;
	private TextView mTvEllapsedTime;
	private TextView mTvTotalTime;
	private ProgressBar mProgressBar;

	private boolean mRemoteOriginalLoaded;
	private boolean mRemoteTranslationLoaded;
	private boolean mRemoteLyricLoaded;

	private boolean mLocalOriginalLoaded;
	private boolean mLocalTranslationLoaded;
	private boolean mLocalLyricLoaded;
	
	private StringBuilder mRecycle = new StringBuilder(10);


	private DatabaseHelper mDatabaseHelper;

	private Handler mLoadRemoteHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_LOAD_REMOTE_ORIGINAL_SUCCESS:
				mWebViewEn.loadDataWithBaseURL("", mArticle.text, "text/html",
						"utf-8", "");
				
				dismissDialog(DLG_PROGRESS_SPIN);
				setCurrentView(VIEW_ORIGINAL);
				break;
			case WHAT_LOAD_REMOTE_TRANSLATION_SUCCESS:
				mWebViewZh.loadDataWithBaseURL("", mArticle.textzh,
						"text/html", "utf-8", "");
				dismissDialog(DLG_PROGRESS_SPIN);
				setCurrentView(VIEW_TRANSLATION);
				break;
			case WHAT_LOAD_REMOTE_LYRIC_SUCCESS:
				dismissDialog(DLG_PROGRESS_SPIN);
				setCurrentView(VIEW_LYRIC);
				mLyricHandler.sendEmptyMessage(WHAT_LYRIC_UPDATE);
				break;
			case WHAT_LOAD_REMOTE_ORIGINAL_FAIL_IO:
			case WHAT_LOAD_REMOTE_ORIGINAL_FAIL_PARSE:
			case WHAT_LOAD_REMOTE_TRANSLATION_FAIL_IO:
			case WHAT_LOAD_REMOTE_TRANSLATION_FAIL_PARSE:
				dismissDialog(DLG_PROGRESS_SPIN);
				mLastError = Error.LoadRemotePageError;
				showDialog(DLG_ERROR);
				break;
			case WHAT_LOAD_REMOTE_LYRIC_FAIL_IO:
				dismissDialog(DLG_PROGRESS_SPIN);
				mLastError = Error.LoadRemotePageError;
				showDialog(DLG_ERROR);
				break;
			default:
				break;
			}
		}

	};

	private Handler mLoadLocalHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_LOAD_LOCAL_ORIGINAL_SUCCESS:
				mWebViewEn.loadDataWithBaseURL("", mArticle.text, "text/html",
						"utf-8", "");
				dismissDialog(DLG_PROGRESS_SPIN);
				setCurrentView(VIEW_ORIGINAL);
				break;
			case WHAT_LOAD_LOCAL_TRANSLATION_SUCCESS:
				mWebViewZh.loadDataWithBaseURL("", mArticle.textzh,
						"text/html", "utf-8", "");
				dismissDialog(DLG_PROGRESS_SPIN);
				setCurrentView(VIEW_TRANSLATION);
				break;
			case WHAT_LOAD_LOCAL_LYRIC_SUCCESS:
				dismissDialog(DLG_PROGRESS_SPIN);
				setCurrentView(VIEW_LYRIC);
				mLyricHandler.sendEmptyMessage(WHAT_LYRIC_UPDATE);
				break;
			case WHAT_LOAD_LOCAL_ORIGINAL_FAIL_IO:
			case WHAT_LOAD_LOCAL_ORIGINAL_FAIL_PARSE:
			case WHAT_LOAD_LOCAL_TRANSLATION_FAIL_IO:
			case WHAT_LOAD_LOCAL_TRANSLATION_FAIL_PARSE:
				dismissDialog(DLG_PROGRESS_SPIN);
				mLastError = Error.LoadLocalPageError;
				showDialog(DLG_ERROR);
				break;
			case WHAT_LOAD_LOCAL_LYRIC_FAIL_IO:
				dismissDialog(DLG_PROGRESS_SPIN);
				mLastError = Error.LoadLocalPageError;
				showDialog(DLG_ERROR);
				mViewFlipper.setDisplayedChild(mCurrentView);
				break;
			default:
				break;
			}
		}

	};

	private Handler mLyricHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_LYRIC_UPDATE:
				try {
					if (mService.isPlaying()) {
						// TODO update LyricView
						Log.d(TAG, "in mLyricHandler");
						if (mCurrentView == VIEW_LYRIC) {
							try {
								mLyricView.update(mService.position());
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							mLyricHandler.sendEmptyMessageDelayed(
									WHAT_LYRIC_UPDATE, 100);
						}
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}

	};

	private OnClickListener mPauseButtonClickListener = new OnClickListener() {

		public void onClick(View v) {
			doPauseResume();
		}
	};

	private void doPauseResume() {
		try {
			if (mService != null) {
				if (mService.isPlaying()) {
					mService.pause();
					refreshNow();
				} else {
					mService.play();
			        long next = refreshNow();
			        queueNextRefresh(next);
			        mLyricHandler.sendEmptyMessage(WHAT_LYRIC_UPDATE);
				}	
				setPauseButtonImage();
			}
		} catch (RemoteException ex) {
			ex.printStackTrace();
		}
	}

	private LyricView mLyricView;

	private OnClickListener mStopButtonClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (mService != null) {
				int state;
				try {
					state = mService.getState();

					if (state != MediaPlaybackService.STATE_IDLE
							&& state != MediaPlaybackService.STATE_INITIALIZED
							&& state != MediaPlaybackService.STATE_ERROR)
						mService.stop();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			
		}
	};

	protected void resetLyricView() {
		mLyricView.resetLyric();
	}

	private boolean isRemote() {
		return mArticle.id == -1;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		menu.add(MENU_REMOTE_GROUP, MENU_REMOTE_ORIGINAL, Menu.NONE,
				R.string.menu_original);
		menu.add(MENU_REMOTE_GROUP, MENU_REMOTE_TRANSLATION, Menu.NONE,
				R.string.menu_translation);
		menu.add(MENU_REMOTE_GROUP, MENU_REMOTE_LYRIC, Menu.NONE,
				R.string.menu_lrc);
		menu.add(MENU_REMOTE_GROUP, MENU_REMOTE_DOWNLOAD, Menu.NONE,
				R.string.menu_download).setIcon(R.drawable.file_download);

		menu.add(MENU_LOCAL_GROUP, MENU_LOCAL_ORIGINAL, Menu.NONE,
				R.string.menu_original);
		menu.add(MENU_LOCAL_GROUP, MENU_LOCAL_TRANSLATION, Menu.NONE,
				R.string.menu_translation);
		menu.add(MENU_LOCAL_GROUP, MENU_LOCAL_LYRIC, Menu.NONE,
				R.string.menu_lrc);

		return result;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (isRemote()) {
			menu.setGroupVisible(MENU_REMOTE_GROUP, true);
			menu.setGroupVisible(MENU_LOCAL_GROUP, false);

			switch (mCurrentView) {
			case 0:
				menu.findItem(MENU_REMOTE_ORIGINAL).setEnabled(false);
				menu.findItem(MENU_REMOTE_TRANSLATION).setEnabled(
						mArticle.hastextzh);
				menu.findItem(MENU_REMOTE_LYRIC).setEnabled(mArticle.haslrc);
				break;
			case 1:
				menu.findItem(MENU_REMOTE_ORIGINAL).setEnabled(true);
				menu.findItem(MENU_REMOTE_TRANSLATION).setEnabled(false);
				menu.findItem(MENU_REMOTE_LYRIC).setEnabled(mArticle.haslrc);
				break;
			case 2:
				menu.findItem(MENU_REMOTE_ORIGINAL).setEnabled(true);
				menu.findItem(MENU_REMOTE_TRANSLATION).setEnabled(
						mArticle.hastextzh);
				menu.findItem(MENU_REMOTE_LYRIC).setEnabled(false);
				break;
			default:
				throw new RuntimeException("mCurrentView invalid -- "
						+ mCurrentView);
			}
		} else {
			menu.setGroupVisible(MENU_REMOTE_GROUP, false);
			menu.setGroupVisible(MENU_LOCAL_GROUP, true);

			switch (mCurrentView) {
			case 0:
				menu.findItem(MENU_LOCAL_ORIGINAL).setEnabled(false);
				menu.findItem(MENU_LOCAL_TRANSLATION).setEnabled(
						mArticle.hastextzh);
				menu.findItem(MENU_LOCAL_LYRIC).setEnabled(mArticle.haslrc);
				break;
			case 1:
				menu.findItem(MENU_LOCAL_ORIGINAL).setEnabled(true);
				menu.findItem(MENU_LOCAL_TRANSLATION).setEnabled(false);
				menu.findItem(MENU_LOCAL_LYRIC).setEnabled(mArticle.haslrc);
				break;
			case 2:
				menu.findItem(MENU_LOCAL_ORIGINAL).setEnabled(true);
				menu.findItem(MENU_LOCAL_TRANSLATION).setEnabled(
						mArticle.hastextzh);
				menu.findItem(MENU_LOCAL_LYRIC).setEnabled(false);
				break;
			default:
				throw new RuntimeException("mCurrentView invalid -- "
						+ mCurrentView);
			}
		}

		return super.onPrepareOptionsMenu(menu);
	}

	private void setCurrentView(int view) {
		mCurrentView = view;
		mViewFlipper.setDisplayedChild(mCurrentView);
	}

	private void commandLoadRemoteOriginal() {
		if (mRemoteOriginalLoaded)
			setCurrentView(VIEW_ORIGINAL);
		else
			loadRemoteOriginal();
	}

	private void commandLoadRemoteTranslation() {
		if (mRemoteTranslationLoaded)
			setCurrentView(VIEW_TRANSLATION);
		else
			loadRemoteTranslation();
	}

	private void commandLoadRemoteLyric() {
		if (mRemoteLyricLoaded) {
			setCurrentView(VIEW_LYRIC);
			mLyricHandler.sendEmptyMessage(WHAT_LYRIC_UPDATE);
		} else
			loadRemoteLyricView();
	}

	private void commandRemoteDownload() {
		// check if the article has been downloaded
		if (mDatabaseHelper.isArticleExist(mArticle)) {
			showDialog(DLG_CONFIRM_DOWNLOAD);
		} else {
			downloadArticleInService(mArticle);
		}
	}

	private void commandLoadLocalOriginal() {
		if (mLocalOriginalLoaded)
			setCurrentView(VIEW_ORIGINAL);
		else
			loadLocalOriginal();

	}

	private void commandLoadLocalTranslation() {
		if (mLocalTranslationLoaded)
			setCurrentView(VIEW_TRANSLATION);
		else
			loadLocalTranslation();
	}

	private void commandLoadLocalLyric() {
		if (mLocalLyricLoaded) {
			setCurrentView(VIEW_LYRIC);
			mLyricHandler.sendEmptyMessage(WHAT_LYRIC_UPDATE);
		} else
			loadLocalLyricView();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mLastCommand = item.getItemId();
		switch (mLastCommand) {
		case MENU_REMOTE_ORIGINAL:
			commandLoadRemoteOriginal();

			return true;

		case MENU_REMOTE_TRANSLATION:
			commandLoadRemoteTranslation();

			return true;

		case MENU_REMOTE_LYRIC:
			commandLoadRemoteLyric();

			return true;

		case MENU_REMOTE_DOWNLOAD:
			commandRemoteDownload();

			return true;

		case MENU_LOCAL_ORIGINAL:
			commandLoadLocalOriginal();

			return true;

		case MENU_LOCAL_TRANSLATION:
			commandLoadLocalTranslation();

			return true;

		case MENU_LOCAL_LYRIC:
			commandLoadLocalLyric();

			return true;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void loadLocalLyricView() {
		showDialog(DLG_PROGRESS_SPIN);
		new Thread() {

			@Override
			public void run() {
				try {
					mLocalLyricLoaded = mLyricView
							.loadLyric(new FileInputStream(Utils
									.localLyricFile(mArticle)));
					if (mLocalLyricLoaded)
						mLoadLocalHandler
								.sendEmptyMessage(WHAT_LOAD_LOCAL_LYRIC_SUCCESS);
				} catch (FileNotFoundException e) {
					Log.d(TAG, "loadLocalLyricView fail.", e);
					mLocalLyricLoaded = false;
					mLyricView.clearLyricLoaded();
					mLoadLocalHandler
							.sendEmptyMessage(WHAT_LOAD_LOCAL_LYRIC_FAIL_IO);
				}
			}

		}.start();

	}

	private void loadRemoteLyricView() {
		showDialog(DLG_PROGRESS_SPIN);
		new Thread() {

			@Override
			public void run() {
				try {
					mRemoteLyricLoaded = mLyricView.loadLyric(Utils
							.getInputStreamFromUrl(mApp.mHttpClient,
									mArticle.urllrc));
					Log.d(TAG, "loadRemoteLyricView: "
							+ mRemoteLyricLoaded);
					if (mRemoteLyricLoaded) {
						mLoadRemoteHandler
								.sendEmptyMessage(WHAT_LOAD_REMOTE_LYRIC_SUCCESS);
					} else {
						mLyricView.clearLyricLoaded();
						mLoadRemoteHandler
								.sendEmptyMessage(WHAT_LOAD_REMOTE_LYRIC_FAIL_IO);
					}
				} catch (IOException e) {
					Log.d(TAG, "loadRemoteLyricView fail.", e);
					mRemoteLyricLoaded = false;
					mLyricView.clearLyricLoaded();
					mLoadRemoteHandler
							.sendEmptyMessage(WHAT_LOAD_REMOTE_LYRIC_FAIL_IO);
				}
			}

		}.start();
	}

	private void loadRemoteTranslation() {
		showDialog(DLG_PROGRESS_SPIN);
		new Thread() {

			@Override
			public void run() {
//				mApp.mPageGenerator.mParser = mApp.mDataSource
//						.getPageZhParsers().get(
//								mArticle.type + "_" + mArticle.subtype);
//				try {
//					mApp.mPageGenerator.getArticle(mArticle, true);
//					mRemoteTranslationLoaded = true;
//					mLoadRemoteHandler
//							.sendEmptyMessage(WHAT_LOAD_REMOTE_TRANSLATION_SUCCESS);
//				} catch (IOException e) {
//					mRemoteTranslationLoaded = false;
//					mLoadRemoteHandler
//							.sendEmptyMessage(WHAT_LOAD_REMOTE_TRANSLATION_FAIL_IO);
//				} catch (IllegalContentFormatException e) {
//					mRemoteTranslationLoaded = false;
//					mLoadRemoteHandler
//							.sendEmptyMessage(WHAT_LOAD_REMOTE_TRANSLATION_FAIL_PARSE);
//				}
				
				mRemoteTranslationLoaded = true;
				mLoadRemoteHandler.sendEmptyMessage(WHAT_LOAD_REMOTE_TRANSLATION_SUCCESS);
			}

		}.start();

	}

	private void loadLocalTranslation() {
		showDialog(DLG_PROGRESS_SPIN);
		new Thread() {

			@Override
			public void run() {
				try {
					mArticle.textzh = Utils.loadTextZh(mArticle);
					mLocalTranslationLoaded = true;
					mLoadLocalHandler.sendEmptyMessage(WHAT_LOAD_LOCAL_TRANSLATION_SUCCESS);
				} catch (IOException e) {
					mLocalTranslationLoaded = false;
					mLoadLocalHandler
							.sendEmptyMessage(WHAT_LOAD_LOCAL_TRANSLATION_FAIL_IO);
				}
			}

		}.start();
	}

	// private void downloadArticleModal() {
	// showDialog(DLG_PROGRESS_BAR);
	// DownloadTask task = new DownloadTask(mApp.mHttpClient, mDatabaseHelper,
	// mArticle);
	// task.addProgressListener(new HandlerProgressListener(mDownloadHandler));
	// new Thread(task).start();
	// }

	private void downloadArticleInService(Article article) {
		Intent intent = new Intent(this, DownloadService.class);

		Utils.putArticleToIntent(article, intent);

		startService(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// no title bar
		// requestWindowFeature(Window.FEATURE_NO_TITLE);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		Utils.setKeepScreenOn(this);

		setContentView(R.layout.show);

		mApp = (App) getApplication();

		mArticle = Utils.getArticleFromIntent(getIntent());

		setTitle(mArticle.title);

		// set up controls
		setupWidgets();

		// set up database
		mDatabaseHelper = new DatabaseHelper(this);
		mDatabaseHelper.open();

		// load original page
		if (isRemote()) {
			mLastCommand = MENU_REMOTE_ORIGINAL;
			commandLoadRemoteOriginal();
		} else {
			mLastCommand = MENU_LOCAL_ORIGINAL;
			commandLoadLocalOriginal();
		}
	}

	private void setupWidgets() {
		mAdView = (AdView) findViewById(R.id.ad);

		String keywords = KEYWORDS[new Random(System.currentTimeMillis())
				.nextInt(KEYWORDS.length)];
		Log.d(TAG, "keywords -- " + keywords);
		mAdView.setKeywords(keywords);

		mViewFlipper = (ViewFlipper) findViewById(R.id.flipper);

		mWebViewEn = (WebView) findViewById(R.id.webview_en);
		mWebViewZh = (WebView) findViewById(R.id.webview_zh);

		mLyricView = (LyricView) findViewById(R.id.lyricview);

		mBtnPause = (ImageButton) findViewById(R.id.btn_pause);
		
		mBtnPause.setOnClickListener(mPauseButtonClickListener);
		
//		mBtnStop = (ImageButton) findViewById(R.id.btn_stop);
//		mBtnStop.setOnClickListener(mStopButtonClickListener);

		mTvEllapsedTime = (TextView) findViewById(R.id.tv_ellapsed_time);
		mTvTotalTime = (TextView) findViewById(R.id.tv_total_time);

		mProgressBar = (ProgressBar) findViewById(R.id.pb_audio);
		mProgressBar.setMax(1000);
	}

	private void loadLocalOriginal() {
		showDialog(DLG_PROGRESS_SPIN);
		new Thread() {

			@Override
			public void run() {
				try {
					mArticle.text = Utils.loadText(mArticle);
					mLocalOriginalLoaded = true;
					mLoadLocalHandler
							.sendEmptyMessage(WHAT_LOAD_LOCAL_ORIGINAL_SUCCESS);
				} catch (IOException e) {
					mLocalOriginalLoaded = false;
					mLoadLocalHandler
							.sendEmptyMessage(WHAT_LOAD_LOCAL_ORIGINAL_FAIL_IO);
				}
			}

		}.start();
	}

	private void loadRemoteOriginal() {
		showDialog(DLG_PROGRESS_SPIN);
		new Thread() {

			@Override
			public void run() {
//				mApp.mPageGenerator.mParser = mApp.mDataSource.getPageParsers()
//						.get(mArticle.type + "_" + mArticle.subtype);
//				try {
//					mApp.mPageGenerator.getArticle(mArticle, false);
//					mRemoteOriginalLoaded = true;
//					mLoadRemoteHandler
//							.sendEmptyMessage(WHAT_LOAD_REMOTE_ORIGINAL_SUCCESS);
//				} catch (IOException e) {
//					mRemoteOriginalLoaded = false;
//					mLoadRemoteHandler
//							.sendEmptyMessage(WHAT_LOAD_REMOTE_ORIGINAL_FAIL_IO);
//				} catch (IllegalContentFormatException e) {
//					mRemoteOriginalLoaded = false;
//					mLoadRemoteHandler
//							.sendEmptyMessage(WHAT_LOAD_REMOTE_ORIGINAL_FAIL_PARSE);
//				}
				
				mRemoteOriginalLoaded = true;
				mLoadRemoteHandler.sendEmptyMessage(WHAT_LOAD_REMOTE_ORIGINAL_SUCCESS);
			}

		}.start();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
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
		case DLG_ERROR:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setTitle(R.string.alert_title_error);
			// without this statement, you would not be able to change
			// AlertDialog's message in onPrepareDialog
			builder.setMessage("");
			builder.setPositiveButton(R.string.btn_retry,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							switch (mLastCommand) {
							case MENU_REMOTE_ORIGINAL:
								commandLoadRemoteOriginal();
								break;
							case MENU_REMOTE_TRANSLATION:
								commandLoadRemoteTranslation();
								break;
							case MENU_REMOTE_LYRIC:
								commandLoadRemoteLyric();
								break;
							case MENU_LOCAL_ORIGINAL:
								commandLoadLocalOriginal();
								break;
							case MENU_LOCAL_TRANSLATION:
								commandLoadLocalTranslation();
								break;
							case MENU_LOCAL_LYRIC:
								commandLoadLocalLyric();
								break;
							default:
								break;
							}
						}
					});
			builder.setNegativeButton(R.string.btn_cancel,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							if (mCurrentView == VIEW_INVALID)
								Show.this.finish();
						}
					});
			return builder.create();
		case DLG_CONFIRM_DOWNLOAD:
			AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
			builder2.setIcon(android.R.drawable.ic_dialog_alert);
			builder2.setTitle(R.string.alert_title_confirm_download);
			// without this statement, you would not be able to change
			// AlertDialog's message in onPrepareDialog
			builder2.setMessage("");
			builder2.setPositiveButton(R.string.btn_yes,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							downloadArticleInService(mArticle);
						}
					});

			builder2.setNegativeButton(R.string.btn_no,
					new DialogInterface.OnClickListener() {

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
		case DLG_ERROR:
			AlertDialog alertDialog = (AlertDialog) dialog;
			switch (mLastError) {
			case LoadRemotePageError:
				alertDialog
						.setMessage(getString(R.string.alert_msg_load_remote_page_error));
				break;
			case LoadLocalPageError:
				alertDialog
						.setMessage(getString(R.string.alert_msg_load_local_page_error));
				break;
			case PlayRemoteAudioError:
				alertDialog
						.setMessage(getString(R.string.alert_msg_play_remote_audio_error));
				break;
			case PlayLocalAudioError:
				alertDialog
						.setMessage(getString(R.string.alert_msg_play_local_audio_error));
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
			alertDialog2.setMessage(getString(
					R.string.alert_msg_confirm_download, mArticle.title));
			break;
		default:
			break;
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName classname, IBinder obj) {
			mService = IMediaPlaybackService.Stub.asInterface(obj);
			if (Utils.sService == null) {
				Utils.sService = mService;
			}

			try {

				Article article = mService.getArticle();

				if (article == null) {
					mService.setArticle(mArticle);
				} else {
					if (article.id == mArticle.id && article.urlmp3.equals(mArticle.urlmp3)) {
						// Check the player's current state
						int state;

						state = mService.getState();
						Log.d(TAG, "[onServiceConnected] service state -- "
								+ state);

						switch (state) {
						case MediaPlaybackService.STATE_IDLE:
							mService.setArticle(mArticle);
							break;
						case MediaPlaybackService.STATE_PAUSED:

							if (mService.isPlaying())
								setPauseButtonImage();
							updateTrackInfo();
							refreshNow();
							break;
						case MediaPlaybackService.STATE_STARTED:
							if (mService.isPlaying()) {
								setPauseButtonImage();
								updateTrackInfo();
								long next = refreshNow();
								queueNextRefresh(next);
							}

						default:
							break;
						}
					} else {
						mService.stop();
						mService.setArticle(mArticle);
					}
				}

			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void onServiceDisconnected(ComponentName classname) {
			mService = null;
		}
	};

	@Override
	protected void onStart() {
		super.onStart();

		paused = false;

		if (false == Utils.bindToService(this, mConnection)) {
			// something went wrong
			Log.e(TAG, "[onStart] bind to service fail");
			mHandler.sendEmptyMessage(QUIT);
		}

		IntentFilter f = new IntentFilter();
		
		f.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
		f.addAction(MediaPlaybackService.META_CHANGED);
		f.addAction(MediaPlaybackService.PLAYBACK_COMPLETE);
		f.addAction(MediaPlaybackService.ASYNC_OPEN_COMPLETE);
		
		registerReceiver(mStatusListener, new IntentFilter(f));
		
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		paused = true;
		mHandler.removeMessages(REFRESH);
		unregisterReceiver(mStatusListener);
		Utils.unbindFromService(this);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDatabaseHelper.close();
	}

	private IMediaPlaybackService mService;
	private long mPosOverride = -1;
	private long mDuration;
	private boolean paused;

	private static final int REFRESH = 1;
	private static final int QUIT = 2;

	private void queueNextRefresh(long delay) {
		if (!paused) {
			Message msg = mHandler.obtainMessage(REFRESH);
			mHandler.removeMessages(REFRESH);
			mHandler.sendMessageDelayed(msg, delay);
		}
	}

	private long refreshNow() {
		if (mService == null)
			return 500;
		try {
			long pos = mPosOverride < 0 ? mService.position() : mPosOverride;
			long remaining = 1000 - (pos % 1000);
			if ((pos >= 0) && (mDuration > 0)) {
				mTvEllapsedTime.setText(DateUtils.formatElapsedTime(mRecycle,
						pos / 1000));

				if (mService.isPlaying()) {
					mTvEllapsedTime.setVisibility(View.VISIBLE);
				} else {
					// blink the counter
					int vis = mTvEllapsedTime.getVisibility();
					mTvEllapsedTime
							.setVisibility(vis == View.INVISIBLE ? View.VISIBLE
									: View.INVISIBLE);
					remaining = 500;
				}

				mProgressBar.setProgress((int) (1000 * pos / mDuration));
			} else {
				mTvEllapsedTime.setText("--:--");
				mProgressBar.setProgress(1000);
			}
			// return the number of milliseconds until the next full second, so
			// the counter can be updated at just the right time
			return remaining;
		} catch (RemoteException ex) {
		}
		return 500;
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case REFRESH:
				long next = refreshNow();
				queueNextRefresh(next);
				break;

			case QUIT:
				// This can be moved back to onCreate once the bug that prevents
				// Dialogs from being started from onCreate/onResume is fixed.
				new AlertDialog.Builder(Show.this).setTitle(
						R.string.service_start_error_title).setMessage(
						R.string.service_start_error_msg).setPositiveButton(
						R.string.service_start_error_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								finish();
							}
						}).setCancelable(false).show();
				break;

			default:
				break;
			}
		}
	};

	private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(MediaPlaybackService.META_CHANGED)) {
				// redraw the artist/title info and
				// set new max for progress bar
				updateTrackInfo();
				setPauseButtonImage();
				queueNextRefresh(1);
			} else if (action.equals(MediaPlaybackService.PLAYBACK_COMPLETE)) {
				setPauseButtonImage();
			} else if (action.equals(MediaPlaybackService.PLAYSTATE_CHANGED)) {
				setPauseButtonImage();
			} else if (action.equals(MediaPlaybackService.ASYNC_OPEN_COMPLETE)) {
				updateTrackInfo();
			}
		}
	};

	private void setPauseButtonImage() {
		try {
			if (mService != null && mService.isPlaying()) {
				mBtnPause.setImageResource(android.R.drawable.ic_media_pause);
			} else {
				mBtnPause.setImageResource(android.R.drawable.ic_media_play);
			}
		} catch (RemoteException ex) {
		}
	}

	private void updateTrackInfo() {
		if (mService == null) {
			return;
		}
		try {

			int state = mService.getState();
			if (state == MediaPlaybackService.STATE_IDLE || state == MediaPlaybackService.STATE_INITIALIZED
					|| state == MediaPlaybackService.STATE_ERROR) {
				finish();
				return;
			}
			mDuration = mService.duration();
			mTvTotalTime.setText(DateUtils.formatElapsedTime(mRecycle,
					mDuration / 1000));
		} catch (RemoteException ex) {
			finish();
		}
	}
}
