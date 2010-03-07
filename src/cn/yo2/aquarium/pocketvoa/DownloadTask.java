package cn.yo2.aquarium.pocketvoa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.text.TextUtils;
import android.util.Log;

public class DownloadTask implements Runnable {
	private static final String CLASSTAG = DownloadTask.class.getSimpleName();

	public static final int WHICH_DOWNLOAD_TEXT = 0;
	public static final int WHICH_DOWNLOAD_MP3 = 1;
	public static final int WHICH_DOWNLOAD_TEXTZH = 2;
	public static final int WHICH_DOWNLOAD_LYRIC = 3;

	private DefaultHttpClient mClient;

	private Article mArticle;

	private DatabaseHelper mDatabaseHelper;

	private List<IProgressListener> mListeners = new ArrayList<IProgressListener>();

	public void addProgressListener(IProgressListener listener) {
		mListeners.add(listener);
	}

	public void removeProgressListener(IProgressListener listener) {
		mListeners.remove(listener);
	}

	public void clearProgressListener() {
		mListeners.clear();
	}

	public DownloadTask(DefaultHttpClient httpClient,
			DatabaseHelper databaseHelper, Article article) {
		super();
		this.mDatabaseHelper = databaseHelper;
		this.mClient = httpClient;
		this.mArticle = article;
	}

	public void run() {
		boolean result = downloadText();
		if (!result)
			return;
		
		result = downloadMp3();
		if (!result)
			return;
		
		if (mArticle.hastextzh)
			result = downloadTextZh();
		if (!result)
			return;
		
		if (mArticle.haslrc)
			result = downloadLyric();
		if (!result)
			return;
		
		mDatabaseHelper.createArticle(mArticle);
	}

	private boolean downloadText() {
		return downloadTextFile(mArticle.text, Utils.localTextFile(mArticle),
				WHICH_DOWNLOAD_TEXT, "Error when download text.");
	}

	private boolean downloadTextZh() {
		return downloadTextFile(mArticle.textzh, Utils
				.localTextZhFile(mArticle), WHICH_DOWNLOAD_TEXTZH,
				"Error when downloading text translated.");
	}

	private boolean downloadTextFile(String text, File local, int which,
			String error) {
		if (TextUtils.isEmpty(text)) {
			Log.e(CLASSTAG, "text is empty");
			for (IProgressListener listener : mListeners) {
				listener.setError(which, error);
			}
			return false;
		} else {
			FileWriter fw = null;
			try {
				if (!local.exists())
					local.createNewFile();

				fw = new FileWriter(local);
				fw.write(text);
				for (IProgressListener listener : mListeners) {
					listener.setSuccess(which);
				}
				return true;
			} catch (IOException e) {
				Log.e(CLASSTAG, error, e);
				for (IProgressListener listener : mListeners) {
					listener.setError(which, error);
				}
				return false;
			} finally {
				if (fw != null)
					try {
						fw.close();
					} catch (IOException e) {
						// ignore
					}
			}
		}
	}

	private boolean downloadLyric() {
		return downloadFile(mArticle.urllrc, Utils.localLyricFile(mArticle),
				WHICH_DOWNLOAD_LYRIC, "Error when downloading lyric.");
	}

	private boolean downloadMp3() {
		return downloadFile(mArticle.urlmp3, Utils.localMp3File(mArticle),
				WHICH_DOWNLOAD_MP3, "Error when downloading audio.");
	}

	/**
	 * Download file from url to local path
	 * 
	 * @param url
	 * @param local
	 *            the local path you want to save the downloaded file
	 * @param which
	 *            the type id to send to download listener
	 * @param error
	 *            the error message to send when error occured
	 * @return true if success, false otherwise
	 */
	private boolean downloadFile(String url, File local, int which, String error) {
		FileOutputStream fos = null;
		InputStream is = null;
		HttpGet get = new HttpGet(url);
		try {
			if (!local.exists())
				local.createNewFile();
			fos = new FileOutputStream(local);
			HttpResponse response = mClient.execute(get);
			HttpEntity entity = response.getEntity();
			long length = entity.getContentLength();
			// Log.d(CLASSTAG, "content-length: " + length);
			is = entity.getContent();
			byte[] buffer = new byte[1024];
			int len = 0;
			long read = 0;
			while ((len = is.read(buffer)) != -1) {
				read += len;
				fos.write(buffer, 0, len);
				for (IProgressListener listener : mListeners) {
					listener.updateProgress(which, read, length);
				}
			}
			for (IProgressListener listener : mListeners) {
				listener.setSuccess(which);
			}
			return true;
		} catch (IOException e) {
			get.abort();
			Log.e(CLASSTAG, error, e);
			for (IProgressListener listener : mListeners) {
				listener.setError(which, error);
			}
			return false;
		} finally {
			if (fos != null)
				try {
					fos.close();
				} catch (IOException e) {
					// ignore
				}
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					// ignore
				}
		}
	}

}
