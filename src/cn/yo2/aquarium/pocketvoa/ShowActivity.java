package cn.yo2.aquarium.pocketvoa;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.TextSize;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.SeekBar.OnSeekBarChangeListener;
import cn.yo2.aquarium.pocketvoa.lyric.LyricView2;
import cn.yo2.aquarium.pocketvoa.ui.TabBar;
import cn.yo2.aquarium.pocketvoa.ui.TabBar.OnTabChangeListener;

import com.adview.AdViewInterface;
import com.adview.AdViewLayout;
import com.adview.AdViewTargeting;
import com.adview.AdViewTargeting.RunMode;
import com.adview.AdViewTargeting.UpdateMode;


public class ShowActivity extends Activity implements AdViewInterface {
	private static final int MAX_TABS = 3;

	private static final String TAG = ShowActivity.class.getSimpleName();

	private static final String[] ADMOB_KEYWORDS = { "android game farm",
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

	private static final int MENU_REMOTE_HOME = Menu.FIRST + 7;
	private static final int MENU_LOCAL_HOME = Menu.FIRST + 8;

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
	private static final int WHAT_REFRESH_LYRIC = 0;

	private static final int PROGRESS_MAX = 1000;

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
	
	private int mPannelWidth;

	// private AdView mAdView;
	
	private TabBar mTabBar;
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
	
	private int[] mCommand = new int[MAX_TABS];

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
				refreshLyric();
				try {
					if (mService.isPlaying())
						queueNextRefreshLyric();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
				refreshLyric();
				try {
					if (mService.isPlaying())
						queueNextRefreshLyric();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
			case WHAT_REFRESH_LYRIC:
				refreshLyric();
				queueNextRefreshLyric();
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
				if (mService.isInitialized()) {

					if (mService.isPlaying()) {
						mService.pause();
						refreshNow();
						if (mCurrentView == VIEW_LYRIC)
							stopRefreshLyric();
					} else {
						mService.play();
						long next = refreshNow();
						queueNextRefreshProgress(next);

						if (mCurrentView == VIEW_LYRIC) {
							refreshLyric();
							queueNextRefreshLyric();
						}

					}
					setPauseButtonImage();
				} else {
					mService.init();
					mBtnPause.setEnabled(false);
				}
			}
		} catch (RemoteException ex) {
			ex.printStackTrace();
		}
	}

	private void refreshLyric() {
		if (mService == null)
			return;
		try {
			long position = mService.position();
			if (position < 0)
				position = 0;
			// Log.d(TAG, "[refreshLyric] position -- " + position);
			mLyricView.update(position);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void queueNextRefreshLyric() {
		mLyricHandler.removeMessages(WHAT_REFRESH_LYRIC);
		if (mCurrentView == VIEW_LYRIC)
			mLyricHandler.sendEmptyMessageDelayed(WHAT_REFRESH_LYRIC, 100);
	}

	private void stopRefreshLyric() {
		mLyricHandler.removeMessages(WHAT_REFRESH_LYRIC);
	}

//	private LyricView mLyricView;
	private LyricView2 mLyricView;

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

	private OnTabChangeListener mOnTabChangeListener = new OnTabChangeListener() {
		
		@Override
		public void onTabChanged(int index) {
			mLastCommand = mCommand[index];
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
	};

	protected void resetLyricView() {
		if (mLyricView.isLyricLoaded())
			mLyricView.resetLyric();
	}

	private boolean isRemote() {
		return mArticle.id == -1;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		menu.add(MENU_REMOTE_GROUP, MENU_REMOTE_HOME, Menu.NONE,
				R.string.menu_main).setIcon(R.drawable.home_48);
		menu.add(MENU_REMOTE_GROUP, MENU_REMOTE_DOWNLOAD, Menu.NONE,
				R.string.menu_download).setIcon(R.drawable.download_48);

		menu.add(MENU_LOCAL_GROUP, MENU_LOCAL_HOME, Menu.NONE,
				R.string.menu_main).setIcon(R.drawable.home_48);

		return result;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (isRemote()) {
			menu.setGroupVisible(MENU_REMOTE_GROUP, true);
			menu.setGroupVisible(MENU_LOCAL_GROUP, false);
		} else {
			menu.setGroupVisible(MENU_REMOTE_GROUP, false);
			menu.setGroupVisible(MENU_LOCAL_GROUP, true);
		}

		return super.onPrepareOptionsMenu(menu);
	}
	
	private Animation mAniPushLeftIn;
	private Animation mAniPushLeftOut;
	private Animation mAniPushRightIn;
	private Animation mAniPushRightOut;
	
	private void setCurrentView(int view) {
		if (view > mCurrentView) {
			mViewFlipper.setInAnimation(mAniPushLeftIn);
			mViewFlipper.setOutAnimation(mAniPushLeftOut);
		} else if (view < mCurrentView) {
			mViewFlipper.setInAnimation(mAniPushRightIn);
			mViewFlipper.setOutAnimation(mAniPushRightOut);
		} else {
			mViewFlipper.clearAnimation();
		}
		
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
			refreshLyric();
			try {
				if (mService.isPlaying())
					queueNextRefreshLyric();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else
			loadRemoteLyricView();
	}

	private void commandRemoteDownload() {
		// check if the article has been downloaded
		if (mDatabaseHelper.isArticleExist(mArticle)) {
			showDialog(DLG_CONFIRM_DOWNLOAD);
		} else {
			Toast.makeText(this, R.string.toast_download_start,
					Toast.LENGTH_SHORT).show();
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
			refreshLyric();
			try {
				if (mService.isPlaying())
					queueNextRefreshLyric();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			loadLocalLyricView();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mLastCommand = item.getItemId();
		switch (mLastCommand) {
		case MENU_REMOTE_DOWNLOAD:
			commandRemoteDownload();
			return true;
		case MENU_REMOTE_HOME:
		case MENU_LOCAL_HOME:
			finish();
			startActivity(new Intent(this, MainActivity.class));
			break;
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
									.localLyricFile(mArticle)), mPannelWidth);
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
									mArticle.urllrc), mPannelWidth);
					Log.d(TAG, "loadRemoteLyricView: " + mRemoteLyricLoaded);
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
				// mApp.mPageGenerator.mParser = mApp.mDataSource
				// .getPageZhParsers().get(
				// mArticle.type + "_" + mArticle.subtype);
				// try {
				// mApp.mPageGenerator.getArticle(mArticle, true);
				// mRemoteTranslationLoaded = true;
				// mLoadRemoteHandler
				// .sendEmptyMessage(WHAT_LOAD_REMOTE_TRANSLATION_SUCCESS);
				// } catch (IOException e) {
				// mRemoteTranslationLoaded = false;
				// mLoadRemoteHandler
				// .sendEmptyMessage(WHAT_LOAD_REMOTE_TRANSLATION_FAIL_IO);
				// } catch (IllegalContentFormatException e) {
				// mRemoteTranslationLoaded = false;
				// mLoadRemoteHandler
				// .sendEmptyMessage(WHAT_LOAD_REMOTE_TRANSLATION_FAIL_PARSE);
				// }

				mRemoteTranslationLoaded = true;
				mLoadRemoteHandler
						.sendEmptyMessage(WHAT_LOAD_REMOTE_TRANSLATION_SUCCESS);
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
					mLoadLocalHandler
							.sendEmptyMessage(WHAT_LOAD_LOCAL_TRANSLATION_SUCCESS);
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
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		setContentView(R.layout.show);

		mApp = (App) getApplication();

		mArticle = Utils.getArticleFromIntent(getIntent());

		setTitle(mArticle.title);

		// set up controls
		setupWidgets();
		
		// set up animation
		setupAnimation();
		
		// set up AdView
		setupAdView();
		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		mPannelWidth = metrics.widthPixels - 20;

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
		// mAdView = (AdView) findViewById(R.id.ad);
		//
		// String keywords = ADMOB_KEYWORDS[new
		// Random(System.currentTimeMillis())
		// .nextInt(ADMOB_KEYWORDS.length)];
		// Log.d(TAG, "keywords -- " + keywords);
		// mAdView.setKeywords(keywords);

		LinearLayout root = (LinearLayout) findViewById(R.id.root);

//		WoobooAdView ad = new WoobooAdView(this,
//				"190d2111a7d140fc8ca9deea453b9003", Color.TRANSPARENT,
//				Color.WHITE, false, 30, 48, 8);
//		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
//				LayoutParams.WRAP_CONTENT);
//		ad.setLayoutParams(params);

//		root.addView(ad);
		
		mTabBar = (TabBar) findViewById(R.id.tab_bar);
		
		int tabCount = 0;
		mTabBar.addTab(new TabBar.TabSpec(getString(R.string.tab_origin), null));
		if (isRemote()) {
			mCommand[tabCount] = MENU_REMOTE_ORIGINAL;
		} else {
			mCommand[tabCount] = MENU_LOCAL_ORIGINAL;
		}
		tabCount++;
		if (mArticle.hastextzh) {
			mTabBar.addTab(new TabBar.TabSpec(getString(R.string.tab_translation), null));
			if (isRemote()) {
				mCommand[tabCount] = MENU_REMOTE_TRANSLATION;
			} else {
				mCommand[tabCount] = MENU_LOCAL_TRANSLATION;
			}
		}
		tabCount++;
		if (mArticle.haslrc) {
			mTabBar.addTab(new TabBar.TabSpec(getString(R.string.tab_lyric), null));
			if (isRemote()) {
				mCommand[tabCount] = MENU_REMOTE_LYRIC;
			} else {
				mCommand[tabCount] = MENU_LOCAL_LYRIC;
			}
		} 
		mTabBar.pack();
		mTabBar.setOnTabChangeListener(mOnTabChangeListener);
		mTabBar.setCurrentTab(0);

		mViewFlipper = (ViewFlipper) findViewById(R.id.flipper);

		mWebViewEn = (WebView) findViewById(R.id.webview_en);
		mWebViewZh = (WebView) findViewById(R.id.webview_zh);

		String fontSize = mApp.mSharedPreferences.getString(
				getString(R.string.prefs_key_font_size),
				getString(R.string.prefs_default_font_size));

		TextSize[] textSizeValues = WebSettings.TextSize.values();

		WebSettings settingsEn = mWebViewEn.getSettings();
		WebSettings settingsZh = mWebViewZh.getSettings();
		for (int i = 0; i < textSizeValues.length; i++) {
			TextSize textSize = textSizeValues[i];
			if (textSize.toString().equalsIgnoreCase(fontSize)) {
				settingsEn.setTextSize(textSize);
				settingsZh.setTextSize(textSize);

				break;
			}
		}
		settingsEn.setSupportZoom(true);
		settingsEn.setBuiltInZoomControls(true);

		settingsZh.setSupportZoom(true);
		settingsZh.setBuiltInZoomControls(true);

		mLyricView = (LyricView2) findViewById(R.id.lyricview);
//		mlyricview = (LyricView) findViewById(R.id.lyricview);

		mBtnPause = (ImageButton) findViewById(R.id.btn_pause);

		mBtnPause.setOnClickListener(mPauseButtonClickListener);

		// mBtnStop = (ImageButton) findViewById(R.id.btn_stop);
		// mBtnStop.setOnClickListener(mStopButtonClickListener);

		mTvEllapsedTime = (TextView) findViewById(R.id.tv_ellapsed_time);
		mTvTotalTime = (TextView) findViewById(R.id.tv_total_time);

		ProgressBar progressBar = (ProgressBar) findViewById(R.id.pb_audio);
		SeekBar seekBar = (SeekBar) findViewById(R.id.sb_audio);

		if (isRemote()) {
			progressBar.setVisibility(View.VISIBLE);
			seekBar.setVisibility(View.GONE);
			
			mProgressBar = progressBar;
		} else {
			progressBar.setVisibility(View.GONE);
			seekBar.setVisibility(View.VISIBLE);
			seekBar.setOnSeekBarChangeListener(mSeekListener);
			seekBar.setSecondaryProgress(PROGRESS_MAX);
			mProgressBar = seekBar;
		}
		
		mProgressBar.setMax(PROGRESS_MAX);
	}
	
	private void setupAdView() {
		LinearLayout layout = (LinearLayout)findViewById(R.id.adLayout);
        if (layout == null) 
            return;
        /*下面两行只用于测试,完成后一定要去掉,参考文挡说明*/
//      AdViewTargeting.setUpdateMode(UpdateMode.EVERYTIME); //保证每次都从服务器取配置
//      AdViewTargeting.setRunMode(RunMode.TEST);         //保证所有选中的广告公司都为测试状态
        /*下面这句方便开发者进行发布渠道统计,详细调用可以参考java doc  */
        //AdViewTargeting.setChannel(Channel.GOOGLEMARKET);
        AdViewLayout adViewLayout = new AdViewLayout(this, "SDK20112331411026bz86cfvj90oy3iv");
        adViewLayout.setAdViewInterface(this);
        layout.addView(adViewLayout);
        layout.invalidate();   
	}
	
	private void setupAnimation() {
		mAniPushLeftIn = AnimationUtils.loadAnimation(this,
	            R.anim.push_left_in);
		mAniPushLeftOut = AnimationUtils.loadAnimation(this,
	            R.anim.push_left_out);
		mAniPushRightIn = AnimationUtils.loadAnimation(this, 
				R.anim.push_right_in);
		mAniPushRightOut = AnimationUtils.loadAnimation(this, 
				R.anim.push_right_out);
	}
	
	private long mLastSeekEventTime;
    private boolean mFromTouch = false;
	
	private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            mLastSeekEventTime = 0;
            mFromTouch = true;
        }
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser || (mService == null)) return;
            long now = SystemClock.elapsedRealtime();
            if ((now - mLastSeekEventTime) > 250) {
                mLastSeekEventTime = now;
                mPosOverride = mDuration * progress / 1000;
                try {
                    mService.seek(mPosOverride);
                } catch (RemoteException ex) {
                }

                // trackball event, allow progress updates
                if (!mFromTouch) {
                    refreshNow();
                    mPosOverride = -1;
                }
            }
        }
        public void onStopTrackingTouch(SeekBar bar) {
            mPosOverride = -1;
            mFromTouch = false;
        }
    };

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
				// mApp.mPageGenerator.mParser =
				// mApp.mDataSource.getPageParsers()
				// .get(mArticle.type + "_" + mArticle.subtype);
				// try {
				// mApp.mPageGenerator.getArticle(mArticle, false);
				// mRemoteOriginalLoaded = true;
				// mLoadRemoteHandler
				// .sendEmptyMessage(WHAT_LOAD_REMOTE_ORIGINAL_SUCCESS);
				// } catch (IOException e) {
				// mRemoteOriginalLoaded = false;
				// mLoadRemoteHandler
				// .sendEmptyMessage(WHAT_LOAD_REMOTE_ORIGINAL_FAIL_IO);
				// } catch (IllegalContentFormatException e) {
				// mRemoteOriginalLoaded = false;
				// mLoadRemoteHandler
				// .sendEmptyMessage(WHAT_LOAD_REMOTE_ORIGINAL_FAIL_PARSE);
				// }

				mRemoteOriginalLoaded = true;
				mLoadRemoteHandler
						.sendEmptyMessage(WHAT_LOAD_REMOTE_ORIGINAL_SUCCESS);
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
								ShowActivity.this.finish();
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
							Toast.makeText(ShowActivity.this,
									R.string.toast_download_start,
									Toast.LENGTH_SHORT).show();
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
	protected void onSaveInstanceState(Bundle outState) {
		dismissDialog(DLG_PROGRESS_SPIN);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		dismissDialog(DLG_PROGRESS_SPIN);
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

				// 1. start from Main Activity or Notification, no article in
				// MediaPlaybackService
				// 2. start from Main Activity or Notification, same article in
				// MediaPlaybackService
				// 3. start from Main Activity or Notification, different
				// article in MediaPlaybackService
				if (article == null) {
					mService.setArticle(mArticle);
				} else {

					// same article
					if (article.id == mArticle.id
							&& article.urlmp3.equals(mArticle.urlmp3)) {
						// Check the player's current state
						int state;

						state = mService.getState();
						Log.d(TAG, "[onServiceConnected] service state -- "
								+ state);

						switch (state) {
						case MediaPlaybackService.STATE_STARTED:
						case MediaPlaybackService.STATE_PAUSED:
							setPauseButtonImage();
							updateTrackInfo();
							long next = refreshNow();
							queueNextRefreshProgress(next);
							break;
						case MediaPlaybackService.STATE_PLAYBACK_COMPLETED:
							setPauseButtonImage();
							refreshNow();
							break;
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

		boolean keepScreenOn = mApp.mSharedPreferences.getBoolean(
				getString(R.string.prefs_key_keep_screen_on), true);

		Utils.setKeepScreenOn(this, keepScreenOn);

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
		f.addAction(MediaPlaybackService.BUFFERING_CHANGED);

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
		mService = null;
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

	private void queueNextRefreshProgress(long delay) {
		if (!paused) {
			Message msg = mHandler.obtainMessage(REFRESH);
			mHandler.removeMessages(REFRESH);
			mHandler.sendMessageDelayed(msg, delay);
		}
	}

	private void stopRefreshProgress() {
		mHandler.removeMessages(REFRESH);
	}

	private long refreshNow() {
		if (mService == null)
			return 500;
		try {
			long pos = mPosOverride < 0 ? mService.position() : mPosOverride;

			// Log.d(TAG, "[refreshProgress] position -- " + pos);

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
				mProgressBar.setProgress(0);
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
				queueNextRefreshProgress(next);
				break;

			case QUIT:
				// This can be moved back to onCreate once the bug that prevents
				// Dialogs from being started from onCreate/onResume is fixed.
				new AlertDialog.Builder(ShowActivity.this).setTitle(
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
				queueNextRefreshProgress(1);
			} else if (action.equals(MediaPlaybackService.PLAYBACK_COMPLETE)) {
				setPauseButtonImage();
				stopRefreshProgress();
				stopRefreshLyric();
				// resetLyricView();

			} else if (action.equals(MediaPlaybackService.PLAYSTATE_CHANGED)) {
				setPauseButtonImage();
			} else if (action.equals(MediaPlaybackService.ASYNC_OPEN_COMPLETE)) {
				updateTrackInfo();
				mBtnPause.setEnabled(true);
				doPauseResume();
			} else if (action.equals(MediaPlaybackService.BUFFERING_CHANGED)) {
				updateBuffering(intent.getExtras().getInt(
						MediaPlaybackService.BUFFERING_CHANGED_EXTRA_KEY));
			} else if (action.equals(MediaPlaybackService.SET_DATASOURCE_ERROR)) {
				mLastError = isRemote() ? Error.PlayRemoteAudioError
						: Error.PlayLocalAudioError;
				showDialog(DLG_ERROR);
				mBtnPause.setEnabled(false);
			}
		}

	};

	private void updateBuffering(int percent) {
		mProgressBar.setSecondaryProgress(percent * 10);
	}

	private void setPauseButtonImage() {
		try {
			if (mService != null && mService.isPlaying()) {
				mBtnPause.setBackgroundResource(R.drawable.nowplaying_pause_button);;
			} else {
				mBtnPause.setBackgroundResource(R.drawable.nowplaying_play_button);
			}
		} catch (RemoteException ex) {
			Log.e(TAG, "[setPauseButtonImage] RemoteException ", ex);

		}
	}

	private void updateTrackInfo() {
		if (mService == null) {
			return;
		}
		try {

			int state = mService.getState();
			if (state == MediaPlaybackService.STATE_IDLE
					|| state == MediaPlaybackService.STATE_INITIALIZED
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

	@Override
	public void onClickAd() {
		Log.d(TAG, "[onClickAd]");
	}

	@Override
	public void onDisplayAd() {
		Log.d(TAG, "[onDisplayAd]");
	}
	
	
}
