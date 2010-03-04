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
		if (downloadText() && downloadMp3())
			mDatabaseHelper.createArticle(mArticle);
	}

	private boolean downloadMp3() {
		FileOutputStream fos = null;
		InputStream is = null;
		HttpGet get = new HttpGet(mArticle.urlmp3);
		try {

			File savedAudio = Utils.localMp3File(mArticle);
			if (!savedAudio.exists())
				savedAudio.createNewFile();
			fos = new FileOutputStream(savedAudio);
			HttpResponse response = mClient.execute(get);
			HttpEntity entity = response.getEntity();
			long length = entity.getContentLength();
//			Log.d(CLASSTAG, "content-length: " + length);
			is = entity.getContent();
			byte[] buffer = new byte[1024];
			int len = 0;
			long read = 0;
			while ((len = is.read(buffer)) != -1) {
				read += len;
				fos.write(buffer, 0, len);
				for (IProgressListener listener : mListeners) {
					listener.updateProgress(WHICH_DOWNLOAD_MP3, read, length);
				}
			}
			for (IProgressListener listener : mListeners) {
				listener.setSuccess(WHICH_DOWNLOAD_MP3);
			}
			return true;
		} catch (IOException e) {
			get.abort();
			String msg = "Error when save mp3.";
//			Log.e(CLASSTAG, msg, e);
			for (IProgressListener listener : mListeners) {
				listener.setError(WHICH_DOWNLOAD_MP3, msg);
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

	private boolean downloadText() {
		if (TextUtils.isEmpty(mArticle.text)) {
//			Log.e(CLASSTAG, "text is empty");
			for (IProgressListener listener : mListeners) {
				listener.setError(WHICH_DOWNLOAD_TEXT, "text is empty.");
			}
			return false;
		} else {
			FileWriter fw = null;
			try {
				File downloadFile = Utils.localTextFile(mArticle);

				if (!downloadFile.exists())
					downloadFile.createNewFile();
			
				fw = new FileWriter(downloadFile);
				fw.write(mArticle.text);
				for (IProgressListener listener : mListeners) {
					listener.setSuccess(WHICH_DOWNLOAD_TEXT);
				}
				return true;
			} catch (IOException e) {
				String msg = "Error when save text.";
				Log.e(CLASSTAG, msg, e);
				for (IProgressListener listener : mListeners) {
					listener.setError(WHICH_DOWNLOAD_TEXT, msg);
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
}
