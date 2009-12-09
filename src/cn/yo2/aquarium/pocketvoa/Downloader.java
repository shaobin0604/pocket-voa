package cn.yo2.aquarium.pocketvoa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class Downloader extends HttpUtil {
	private static final String CLASSTAG = Downloader.class.getSimpleName();

	static final int WHAT_DOWNLOAD_PROGRESS = 2;
	static final int WHAT_DOWNLOAD_ERROR = 1;
	static final int WHAT_DOWNLOAD_SUCCESS = 0;

	Handler mDownloadHandler;

	private File getAppDir() {
		if (isExternalStorageReady()) {
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

	public boolean isExternalStorageReady() {
		return (Environment.getExternalStorageState()
				.equals(Environment.MEDIA_MOUNTED));
	}

	public void downloadMp3(Article article) throws IOException {
		if (mDownloadHandler == null)
			throw new NullPointerException(
					"You must provide a Handler to handle download progress event.");
		File appDir = getAppDir();
		if (appDir == null)
			throw new IOException("Cannot get app dir");
		FileOutputStream fos = null;
		InputStream is = null;
		HttpGet get = new HttpGet(article.mp3);
		try {
			File savedAudio = new File(appDir, Utils
					.extractFilename(article.mp3));
			if (!savedAudio.exists())
				savedAudio.createNewFile();
			fos = new FileOutputStream(savedAudio);
			HttpResponse response = mClient.execute(get);
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
		} catch (IOException e) {
			get.abort();
			throw e;
		} finally {
			if (fos != null)
				fos.close();
			if (is != null)
				is.close();
		}
	}

	public String loadText(Article article) throws IOException {
		File appDir = getAppDir();
		File downloadFile = new File(appDir, Utils.extractFilename(article.url));
		FileReader fr = new FileReader(downloadFile);
		StringBuilder text = new StringBuilder();
		char[] buf = new char[1024];
		while (fr.read(buf) != -1) {
			text.append(buf);
		}
		return text.toString();
	}

	public File localMp3File(Article article) {
		File appDir = getAppDir();
		return new File(appDir, Utils.extractFilename(article.mp3));
	}

	public void downloadText(Article article) throws IOException {
		if (TextUtils.isEmpty(article.text)) {
			Log.e(CLASSTAG, "text is empty");
		} else {
			FileWriter fw = null;
			File appDir = getAppDir();
			try {
				if (appDir == null)
					throw new IOException("Cannot get App dir");
				File downloadFile = new File(appDir, Utils
						.extractFilename(article.url));

				if (!downloadFile.exists())
					if (!downloadFile.createNewFile())
						throw new IOException("Cannot create file");

				fw = new FileWriter(downloadFile);
				fw.write(article.text);
			} catch (IOException e) {
				Log.e(CLASSTAG, "Error when save text.", e);
				throw e;
			} finally {
				if (fw != null)
					try {
						fw.close();
					} catch (IOException e) {
						Log.e(CLASSTAG, "Error when close fos", e);
					}
			}
		}
	}
}
