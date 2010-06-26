package cn.yo2.aquarium.pocketvoa;

import java.io.IOException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MediaPlaybackService extends Service {
	private static final String TAG = MediaPlaybackService.class
			.getSimpleName();
	
    public static final int PLAYBACKSERVICE_STATUS = 1;
	
	public static final String PLAYSTATE_CHANGED = "cn.yo2.aquarium.pocketvoa.playstatechanged";
	public static final String META_CHANGED = "cn.yo2.aquarium.pocketvoa.metachanged";
	public static final String QUEUE_CHANGED = "cn.yo2.aquarium.pocketvoa.queuechanged";
	public static final String PLAYBACK_COMPLETE = "cn.yo2.aquarium.pocketvoa.playbackcomplete";
	public static final String ASYNC_OPEN_COMPLETE = "cn.yo2.aquarium.pocketvoa.asyncopencomplete";
	public static final String BUFFERING_CHANGED = "cn.yo2.aquarium.pocketvoa.bufferingchanged";
	public static final String BUFFERING_CHANGED_EXTRA_KEY = "BUFFERING_CHANGED_EXTRA_KEY";
	
	public static final String SERVICECMD = "cn.yo2.aquarium.pocketvoa.musicservicecommand";
	public static final String CMDNAME = "command";
	public static final String CMDTOGGLEPAUSE = "togglepause";
	public static final String CMDSTOP = "stop";
	public static final String CMDPAUSE = "pause";

	public static final String TOGGLEPAUSE_ACTION = "cn.yo2.aquarium.pocketvoa.musicservicecommand.togglepause";
	public static final String PAUSE_ACTION = "cn.yo2.aquarium.pocketvoa.musicservicecommand.pause";

	private static final int TRACK_ENDED = 1;
	private static final int RELEASE_WAKELOCK = 2;
	private static final int SERVER_DIED = 3;
	private static final int FADEIN = 4;
	
	public static final int STATE_IDLE = 0;
	public static final int STATE_INITIALIZED = 1;
	public static final int STATE_PREPARING = 2;
	public static final int STATE_PREPARED = 3;
	public static final int STATE_STARTED = 4;
	public static final int STATE_PAUSED = 5;
	public static final int STATE_STOPPED = 6;
	public static final int STATE_PLAYBACK_COMPLETED = 7;
	public static final int STATE_END = 8;
	public static final int STATE_ERROR = 9;
	

	private MultiPlayer mPlayer;
	private Uri mFileToPlay;
	// interval after which we stop the service when idle
	private static final int IDLE_DELAY = 60000;

	private WakeLock mWakeLock;
	private int mServiceStartId = -1;
	private boolean mServiceInUse = false;
	private boolean mResumeAfterCall = false;
	private boolean mWasPlaying = false;

	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			if (state == TelephonyManager.CALL_STATE_RINGING) {
				AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				int ringvolume = audioManager
						.getStreamVolume(AudioManager.STREAM_RING);
				if (ringvolume > 0) {
					mResumeAfterCall = (isPlaying() || mResumeAfterCall);
					pause();
				}
			} else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
				// pause the music while a conversation is in progress
				mResumeAfterCall = (isPlaying() || mResumeAfterCall);
				pause();
			} else if (state == TelephonyManager.CALL_STATE_IDLE) {
				// start playing again
				if (mResumeAfterCall) {
					// resume playback only if music was playing
					// when the call was answered
					startAndFadeIn();
					mResumeAfterCall = false;
				}
			}
		}
	};

	private void startAndFadeIn() {
		mMediaplayerHandler.sendEmptyMessageDelayed(FADEIN, 10);
	}

	private Handler mMediaplayerHandler = new Handler() {
		float mCurrentVolume = 1.0f;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FADEIN:
				if (!isPlaying()) {
					mCurrentVolume = 0f;
					mPlayer.setVolume(mCurrentVolume);
					play();
					mMediaplayerHandler.sendEmptyMessageDelayed(FADEIN, 10);
				} else {
					mCurrentVolume += 0.01f;
					if (mCurrentVolume < 1.0f) {
						mMediaplayerHandler.sendEmptyMessageDelayed(FADEIN, 10);
					} else {
						mCurrentVolume = 1.0f;
					}
					mPlayer.setVolume(mCurrentVolume);
				}
				break;
			case SERVER_DIED:

				// the server died when we were idle, so just
				// reopen the same song (it will start again
				// from the beginning though when the user
				// restarts)
				openCurrent();
				break;
			case TRACK_ENDED:
				gotoIdleState();
				notifyChange(PLAYBACK_COMPLETE);
				break;
			case RELEASE_WAKELOCK:
				mWakeLock.release();
				break;
			default:
				break;
			}
		}
	};
	
	/**
     * Notify the change-receivers that something has changed.
     * The intent that is sent contains the following data
     * for the currently playing track:
     * "id" - Integer: the database row ID
     * "artist" - String: the name of the artist
     * "album" - String: the name of the album
     * "track" - String: the name of the track
     * The intent has an action that is one of
     * "cn.yo2.aquarium.pocketvoa.metachanged"
     * "cn.yo2.aquarium.pocketvoa.queuechanged",
     * "cn.yo2.aquarium.pocketvoa.playbackcomplete"
     * "cn.yo2.aquarium.pocketvoa.playstatechanged"
     * respectively indicating that a new track has
     * started playing, that the playback queue has
     * changed, that playback has stopped because
     * the last file in the list has been played,
     * or that the play-state changed (paused/resumed).
     */
    private void notifyChange(String what) {
        Intent i = new Intent(what);
        sendBroadcast(i);
    }
    
    /**
     * Notify buffering status of a media resource being streamed over the network
     * 
     * @param percent 0~100
     */
    private void notifyBufferingChange(int percent) {
    	Intent i = new Intent(BUFFERING_CHANGED);
    	i.putExtra(BUFFERING_CHANGED_EXTRA_KEY, percent);
    	sendBroadcast(i);
    }

	@Override
	public void onCreate() {
		super.onCreate();

		// Needs to be done in this thread, since otherwise
		// ApplicationContext.getPowerManager() crashes.
		mPlayer = new MultiPlayer();
		mPlayer.setHandler(mMediaplayerHandler);

		TelephonyManager tmgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		tmgr.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this
				.getClass().getName());
		mWakeLock.setReferenceCounted(false);

		// If the service was idle, but got killed before it stopped itself, the
		// system will relaunch it. Make sure it gets stopped again in that
		// case.
		Message msg = mDelayedStopHandler.obtainMessage();
		mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "[onDestroy]");
		// Check that we're not being destroyed while something is still
		// playing.
		
		
		if (isPlaying()) {
			Log.e(TAG, "[onDestroy] Service being destroyed while still playing.");
		}
		
		// remove notification icon
		gotoIdleState();
		
		// release all MediaPlayer resources, including the native player and
		// wakelocks
		mPlayer.release();
		mPlayer = null;

		// make sure there aren't any other messages coming
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		mMediaplayerHandler.removeCallbacksAndMessages(null);

		TelephonyManager tmgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		tmgr.listen(mPhoneStateListener, 0);

		mWakeLock.release();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "[onBind] intent -- " + intent);
		
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		mServiceInUse = true;
		return mBinder;
	}

	@Override
	public void onRebind(Intent intent) {
		Log.d(TAG, "[onReBind] intent -- " + intent);
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		mServiceInUse = true;
	}
	
	

	@Override
	public void onStart(Intent intent, int startId) {
		mServiceStartId = startId;
		mDelayedStopHandler.removeCallbacksAndMessages(null);

		if (intent != null) {
			String action = intent.getAction();
			String cmd = intent.getStringExtra("command");

			if (CMDTOGGLEPAUSE.equals(cmd) || TOGGLEPAUSE_ACTION.equals(action)) {
				if (isPlaying()) {
					pause();
				} else {
					play();
				}
			} else if (CMDPAUSE.equals(cmd) || PAUSE_ACTION.equals(action)) {
				pause();
			} else if (CMDSTOP.equals(cmd)) {
				pause();
				seek(0);
			}
		}

		// make sure the service will shut down on its own if it was
		// just started but not bound to and nothing is playing
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		Message msg = mDelayedStopHandler.obtainMessage();
		mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "[onUnbind] intent --" + intent);
		mServiceInUse = false;

		if (isPlaying() || mResumeAfterCall || isPaused()) {
			// something is currently playing, or will be playing once
			// an in-progress call ends, so don't stop the service now.
			return true;
		}

		// If there is a playlist but playback is paused, then wait a while
		// before stopping the service, so that pause/resume isn't slow.
		// Also delay stopping the service if we're transitioning between
		// tracks.
		if (mMediaplayerHandler.hasMessages(TRACK_ENDED)) {
			Message msg = mDelayedStopHandler.obtainMessage();
			mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
			return true;
		}

		// No active playlist, OK to stop the service right now
		stopSelf(mServiceStartId);
		return true;
	}

	private Handler mDelayedStopHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// Check again to make sure nothing is playing right now
			if (isPlaying() || mResumeAfterCall || mServiceInUse
					|| mMediaplayerHandler.hasMessages(TRACK_ENDED) || isPaused()) {
				return;
			}
			stopSelf(mServiceStartId);
		}
	};
	
	
	
    /**
     * Returns the path of the currently playing file, or null if
     * no file is currently playing.
     */
    public Uri getPath() {
        return mFileToPlay;
    }

	private void openCurrent() {
		synchronized (this) {
			stop(false);
			openAsync(mFileToPlay);
		}
	}

	public void openAsync(Uri uri) {
		synchronized (this) {
			if (uri == null) {
				return;
			}

			mFileToPlay = uri;
			mPlayer.setDataSourceAsync(mFileToPlay);
		}
	}

	/**
	 * Starts playback of a previously opened file.
	 */
	public void play() {
		if (mPlayer.isInitialized()) {
			mPlayer.start();
			
			setForeground(true);

			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			Notification status = new Notification();
			status.flags |= Notification.FLAG_ONGOING_EVENT;
			status.icon = R.drawable.media_play;
			
			Intent intent = new Intent(this, Show.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			Utils.putArticleToIntent(mArticle, intent);
			
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
			status.setLatestEventInfo(this, getString(R.string.app_name), mArticle.title, pendingIntent);
			
			nm.notify(PLAYBACKSERVICE_STATUS, status);
			
			if (!mWasPlaying) {
				mWasPlaying = true;
				notifyChange(PLAYSTATE_CHANGED);
			}
			
		}
	}
	
	

	private void stop(boolean remove_status_icon) {
		if (mPlayer.isInitialized()) {
			mPlayer.stop();
		}
		mFileToPlay = null;
		
		if (remove_status_icon) {
			gotoIdleState();
		}
		setForeground(false);
		if (remove_status_icon) {
			mWasPlaying = false;
		}
	}

	/**
	 * Stops playback.
	 */
	public void stop() {
		stop(true);
	}

	/**
	 * Pauses playback (call play() to resume)
	 */
	public void pause() {
		if (isPlaying()) {
			mPlayer.pause();
//			gotoIdleState();
			gotoPauseState();
			setForeground(false);
			mWasPlaying = false;
			notifyChange(PLAYSTATE_CHANGED);
		}
	}

	/**
	 * Returns whether playback is currently paused
	 * 
	 * @return true if playback is paused, false if not
	 */
	public boolean isPlaying() {
		if (mPlayer.isInitialized()) {
			return mPlayer.isPlaying();
		}
		return false;
	}
	
	private boolean isPaused() {
		return mPlayer.getState() == STATE_PAUSED;
	}
	
	private void gotoPauseState() {
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Notification status = new Notification();
		status.flags |= Notification.FLAG_ONGOING_EVENT;
		status.icon = R.drawable.media_pause;
		
		Intent intent = new Intent(this, Show.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Utils.putArticleToIntent(mArticle, intent);
		
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
		status.setLatestEventInfo(this, getString(R.string.app_name), mArticle.title, pendingIntent);
		
		nm.notify(PLAYBACKSERVICE_STATUS, status);
		
		
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		Message msg = mDelayedStopHandler.obtainMessage();
		mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
	}

	private void gotoIdleState() {
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(PLAYBACKSERVICE_STATUS);
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		Message msg = mDelayedStopHandler.obtainMessage();
		mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
	}

	/**
	 * Returns the duration of the file in milliseconds. Currently this method
	 * returns -1 for the duration of MIDI files.
	 */
	public long duration() {
		if (mPlayer.isInitialized()) {
			return mPlayer.duration();
		}
		return -1;
	}

	/**
	 * Returns the current playback position in milliseconds
	 */
	public long position() {
		if (mPlayer.isInitialized()) {
			return mPlayer.position();
		}
		return -1;
	}

	/**
	 * Seeks to the position specified.
	 * 
	 * @param pos
	 *            The position to seek to, in milliseconds
	 */
	public long seek(long pos) {
		if (mPlayer.isInitialized()) {
			if (pos < 0)
				pos = 0;
			if (pos > mPlayer.duration())
				pos = mPlayer.duration();
			return mPlayer.seek(pos);
		}
		return -1;
	}

	/**
	 * Provides a unified interface for dealing with midi files and other media
	 * files.
	 */
	private class MultiPlayer {
		private MediaPlayer mMediaPlayer = new MediaPlayer();
		private Handler mHandler;
		private boolean mIsInitialized = false;
		
		private int mState = STATE_IDLE;
		
		public MultiPlayer() {
			mMediaPlayer.setWakeMode(MediaPlaybackService.this,
					PowerManager.PARTIAL_WAKE_LOCK);
		}

		public void setDataSourceAsync(Uri uri) {
			try {
				mMediaPlayer.reset();
				mState = STATE_IDLE;
				
				Log.d(TAG, "[setDatasourceAsync] uri -- " + uri);
				
				mMediaPlayer.setDataSource(MediaPlaybackService.this, uri);
				mState = STATE_INITIALIZED;
				
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				mMediaPlayer.setOnPreparedListener(preparedlistener);
				mMediaPlayer.setOnBufferingUpdateListener(bufferingUpdateListener);
				mMediaPlayer.prepareAsync();
				mState = STATE_PREPARING;
			} catch (IOException ex) {
				// TODO: notify the user why the file couldn't be opened
				mIsInitialized = false;
				mState = STATE_ERROR;
				return;
			} catch (IllegalArgumentException ex) {
				// TODO: notify the user why the file couldn't be opened
				mIsInitialized = false;
				mState = STATE_ERROR;
				return;
			}
			mMediaPlayer.setOnCompletionListener(completionListener);
			mMediaPlayer.setOnErrorListener(errorListener);

			
		}

		public boolean isInitialized() {
			return mIsInitialized;
		}

		public void start() {
			mMediaPlayer.start();
			mState = STATE_STARTED;
		}

		public void stop() {
			mMediaPlayer.reset();
			mIsInitialized = false;
			mState = STATE_STOPPED;
		}

		/**
		 * You CANNOT use this player anymore after calling release()
		 */
		public void release() {
			stop();
			mMediaPlayer.release();
		}

		public void pause() {
			mMediaPlayer.pause();
			mState = STATE_PAUSED;
		}
		
        public boolean isPlaying() {
            return mMediaPlayer.isPlaying();
        }

		public void setHandler(Handler handler) {
			mHandler = handler;
		}
		
		MediaPlayer.OnBufferingUpdateListener bufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
			
			public void onBufferingUpdate(MediaPlayer mp, int percent) {
				notifyBufferingChange(percent);
			}
		};

		MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				// Acquire a temporary wakelock, since when we return from
				// this callback the MediaPlayer will release its wakelock
				// and allow the device to go to sleep.
				// This temporary wakelock is released when the RELEASE_WAKELOCK
				// message is processed, but just in case, put a timeout on it.
				
				mState = STATE_PLAYBACK_COMPLETED;
				
				mWakeLock.acquire(30000);
				mHandler.sendEmptyMessage(TRACK_ENDED);
				mHandler.sendEmptyMessage(RELEASE_WAKELOCK);
			}
		};

		MediaPlayer.OnPreparedListener preparedlistener = new MediaPlayer.OnPreparedListener() {
			public void onPrepared(MediaPlayer mp) {
				mIsInitialized = true;
				mState = STATE_PREPARED;
				notifyChange(ASYNC_OPEN_COMPLETE);
			}
		};

		MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
			public boolean onError(MediaPlayer mp, int what, int extra) {
				switch (what) {
				case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
					mIsInitialized = false;
					mMediaPlayer.release();
					// Creating a new MediaPlayer and settings its wakemode does
					// not
					// require the media service, so it's OK to do this now,
					// while the
					// service is still being restarted
					mMediaPlayer = new MediaPlayer();
					mState = STATE_IDLE;
					mMediaPlayer.setWakeMode(MediaPlaybackService.this,
							PowerManager.PARTIAL_WAKE_LOCK);
					mHandler.sendMessageDelayed(mHandler
							.obtainMessage(SERVER_DIED), 2000);
					return true;
				default:
					Log.d("MultiPlayer", "Error: " + what + "," + extra);
					break;
				}
				return false;
			}
		};

		public long duration() {
			return mMediaPlayer.getDuration();
		}

		public long position() {
			return mMediaPlayer.getCurrentPosition();
		}

		public long seek(long whereto) {
			mMediaPlayer.seekTo((int) whereto);
			return whereto;
		}

		public void setVolume(float vol) {
			mMediaPlayer.setVolume(vol, vol);
		}
		
		public int getState() {
			return mState;
		}
	}
	
	private int getState() {
		return mPlayer.getState();
	}
	
	private Article mArticle;
	
	private final IMediaPlaybackService.Stub mBinder = new IMediaPlaybackService.Stub()
    {
		@Override
		public Article getArticle(){
			return MediaPlaybackService.this.mArticle;
		}
		@Override
		public int getState() {
			return MediaPlaybackService.this.getState();
		}
		@Override
		public void setArticle(Article article)  {
			MediaPlaybackService.this.mArticle = article;
	
			Uri uri = mArticle.id == -1 ? Uri
					.parse(MediaPlaybackService.this.mArticle.urlmp3) : Uri
					.fromFile(Utils.localMp3File(MediaPlaybackService.this.mArticle));
			Log.d(TAG, "[setArticle] uri -- " + uri);
			MediaPlaybackService.this.openAsync(uri);
		}
		public boolean isPlaying() {
            return MediaPlaybackService.this.isPlaying();
        }
        public void stop() {
            MediaPlaybackService.this.stop();
        }
        public void pause() {
            MediaPlaybackService.this.pause();
        }
        public void play() {
            MediaPlaybackService.this.play();
        }
        public long position() {
            return MediaPlaybackService.this.position();
        }
        public long duration() {
            return MediaPlaybackService.this.duration();
        }
        public long seek(long pos) {
            return MediaPlaybackService.this.seek(pos);
        }
    };
}
