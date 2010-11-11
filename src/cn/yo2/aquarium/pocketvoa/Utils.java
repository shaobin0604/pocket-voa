package cn.yo2.aquarium.pocketvoa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Locale;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Toast;

public class Utils {
	
	private static final String TAG = Utils.class.getSimpleName();
	private static final String PKG = Utils.class.getPackage().getName();
	
	private static final String KEY_VERSION_CODE = "versionCode";
	
	public static IMediaPlaybackService sService = null;
    private static HashMap<Context, ServiceBinder> sConnectionMap = new HashMap<Context, ServiceBinder>();

    public static boolean bindToService(Context context) {
        return bindToService(context, null);
    }

    public static boolean bindToService(Context context, ServiceConnection callback) {
        context.startService(new Intent(context, MediaPlaybackService.class));
        ServiceBinder sb = new ServiceBinder(callback);
        sConnectionMap.put(context, sb);
        return context.bindService((new Intent()).setClass(context,
                MediaPlaybackService.class), sb, 0);
    }
    
    public static void unbindFromService(Context context) {
        ServiceBinder sb = (ServiceBinder) sConnectionMap.remove(context);
        if (sb == null) {
            Log.e("MusicUtils", "Trying to unbind for unknown Context");
            return;
        }
        context.unbindService(sb);
        if (sConnectionMap.isEmpty()) {
            // presumably there is nobody interested in the service at this point,
            // so don't hang on to the ServiceConnection
            sService = null;
        }
    }

    private static class ServiceBinder implements ServiceConnection {
        ServiceConnection mCallback;
        ServiceBinder(ServiceConnection callback) {
            mCallback = callback;
        }
        
        public void onServiceConnected(ComponentName className, android.os.IBinder service) {
            sService = IMediaPlaybackService.Stub.asInterface(service);

            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }
        
        public void onServiceDisconnected(ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }
            sService = null;
        }
    }
	
    /**
     * set screen on or off
     * 
     * @param activity
     * @param on
     */
	public static void setKeepScreenOn(Activity activity, boolean on) {
		if (on)
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		else 
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	public static Article getArticleFromIntent(Intent intent) {
		Article article = new Article();

		article.id = intent.getLongExtra(Article.K_ID, -1);
		article.title = intent.getStringExtra(Article.K_TITLE);
		article.text = intent.getStringExtra(Article.K_TEXT);
		article.urlmp3 = intent.getStringExtra(Article.K_URLMP3);
		article.type = intent.getStringExtra(Article.K_TYPE);
		article.subtype = intent.getStringExtra(Article.K_SUBTYPE);
		article.urltext = intent.getStringExtra(Article.K_URLTEXT);
		article.date = intent.getStringExtra(Article.K_DATE);

		article.textzh = intent.getStringExtra(Article.K_TEXTZH);
		article.haslrc = intent.getBooleanExtra(Article.K_HASLRC, false);
		article.hastextzh = intent.getBooleanExtra(Article.K_HASTEXTZH, false);
		article.urllrc = intent.getStringExtra(Article.K_URLLRC);
		article.urltextzh = intent.getStringExtra(Article.K_URLTEXTZH);

		return article;
	}

	public static void putArticleToIntent(Article article, Intent intent) {
		intent.putExtra(Article.K_ID, article.id);
		intent.putExtra(Article.K_TITLE, article.title);
		intent.putExtra(Article.K_TEXT, article.text);
		intent.putExtra(Article.K_DATE, article.date);
		intent.putExtra(Article.K_TYPE, article.type);
		intent.putExtra(Article.K_SUBTYPE, article.subtype);
		intent.putExtra(Article.K_URLTEXT, article.urltext);
		intent.putExtra(Article.K_URLMP3, article.urlmp3);

		intent.putExtra(Article.K_TEXTZH, article.textzh);
		intent.putExtra(Article.K_HASLRC, article.haslrc);
		intent.putExtra(Article.K_HASTEXTZH, article.hastextzh);
		intent.putExtra(Article.K_URLLRC, article.urllrc);
		intent.putExtra(Article.K_URLTEXTZH, article.urltextzh);
	}

	public static String loadText(Article article) throws IOException {
		return loadTextFile(localTextFile(article));
	}

	public static String loadTextZh(Article article) throws IOException {
		return loadTextFile(localTextZhFile(article));
	}

	public static String loadTextFile(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		StringBuilder text = new StringBuilder();
		String line = null;
		while ((line = br.readLine()) != null) {
			text.append(line);
		}
		return text.toString();
	}

	public static File localTextFile(Article article) {
		return new File(localArticleDir(article), Utils
				.extractFilename(article.urltext));
	}

	public static File localMp3File(Article article) {
		return new File(localArticleDir(article), Utils
				.extractFilename(article.urlmp3));
	}

	public static File localTextZhFile(Article article) {
		return new File(localArticleDir(article), Utils
				.extractFilename(article.urltextzh));
	}

	public static File localLyricFile(Article article) {
		return new File(localArticleDir(article), Utils
				.extractFilename(article.urllrc));
	}

	public static File localArticleDir(Article article) {
		File dir = new File("/sdcard/pocket-voa/" + article.type + '/'
				+ article.subtype + '/');
		if (!dir.exists())
			dir.mkdirs();
		return dir;
	}

	public static boolean hasInternet(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null)
			return false;

		NetworkInfo[] info = cm.getAllNetworkInfo();
		if (info != null) {
			final int len = info.length;
			for (int i = 0; i < len; i++) {
				if (info[i].getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}

		return false;
	}
	
	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if (cm == null) 
			return false;
		
		NetworkInfo info = cm.getActiveNetworkInfo();
		
		return (info != null && info.isConnected());
	}
	
	public static int getCurrentVersionCode(Context context) {
		PackageManager manager = context.getPackageManager();
		String packageName = context.getPackageName();
		// Log.d(CLASSTAG, "package name -- " + packageName);
		try {
			PackageInfo info = manager.getPackageInfo(packageName, 0);
			return info.versionCode;
		} catch (NameNotFoundException e) {
			return 1;
		}
	}

	/**
	 * Check if application is just upgraded
	 * 
	 * @return
	 */
	public static boolean isJustUpgraded(Context context) {
		int currentVersionCode = getCurrentVersionCode(context);
		int savedVersionCode = getSavedVersionCode(context);
		// Log.d(CLASSTAG, "current version code -- " + currentVersionCode);
		// Log.d(CLASSTAG, "saved version code -- " + savedVersionCode);
		return currentVersionCode != savedVersionCode;
	}

	/**
	 * Get saved package version code from preferences
	 * 
	 * @return the saved package version code
	 */
	public static int getSavedVersionCode(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getInt(KEY_VERSION_CODE, 0);
	}

	/**
	 * Save current package version code to preferences
	 */
	public static void setSavedVersionCode(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = preferences.edit();
		editor.putInt(KEY_VERSION_CODE, getCurrentVersionCode(context));
		editor.commit();
	}
	
	public static void showNoNetworkToast(Context context) {
		Toast.makeText(context, R.string.toast_no_network, Toast.LENGTH_SHORT).show();
	}

	public static File getAppDir() {
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

	public static boolean isExternalStorageReady() {
		return (Environment.getExternalStorageState()
				.equals(Environment.MEDIA_MOUNTED));
	}

	public static String extractFilename(String url) {
		return url.substring(url.lastIndexOf('/') + 1);
	}

	/**
	 * Delete file or directory
	 * 
	 * @param fileName
	 *            the name of the file or directory to be deleted
	 * @return true if successful, false otherwise
	 */
	public static boolean delete(String fileName) {
		File file = new File(fileName);
		if (!file.exists()) {
			return false;
		} else {
			if (file.isFile()) {
				return deleteFile(fileName);
			} else {
				return deleteDirectory(fileName);
			}
		}
	}

	/**
	 * Delete a single file
	 * 
	 * @param fileName
	 *            the name of the file to be deleted
	 * @return true if successful, false otherwise
	 */
	public static boolean deleteFile(String fileName) {
		File file = new File(fileName);
		if (file.isFile() && file.exists()) {
			return file.delete();
		} else {
			return false;
		}
	}

	/**
	 * Recursive delete directory and files under it
	 * 
	 * @param dir
	 *            the directory to be deleted
	 * @return true if successful, false otherwise
	 */
	public static boolean deleteDirectory(String dir) {
		if (!dir.endsWith(File.separator)) {
			dir = dir + File.separator;
		}
		File dirFile = new File(dir);

		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		boolean flag = true;

		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {

			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag) {
					break;
				}
			}

			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag) {
					break;
				}
			}
		}

		if (!flag) {
			return false;
		}

		// delete current directory
		return dirFile.delete();
	}

	/**
	 * convert date string from yyyyMMdd to yyyy-MM-dd
	 * 
	 * @param date
	 * @return the converted date string
	 */
	public static String convertDateString(String date) {
		StringBuilder sb = new StringBuilder(date);
		sb.insert(4, '-');
		sb.insert(7, '-');
		return sb.toString();
	}

	/**
	 * convert date string from /\d{2,4}-\d{1,2}-\d{1,2}/ to yyyyMMdd
	 * 
	 * @param date
	 * @return the formated date string
	 */
	public static String formatDateString(String date) {
		String[] parts = date.split("-", 3);
		StringBuilder sb = new StringBuilder();
		if (parts[0].length() == 2) {
			sb.append("20");
		}
		sb.append(parts[0]);
		if (parts[1].length() == 1) {
			sb.append('0');
		}
		sb.append(parts[1]);
		if (parts[2].length() == 1) {
			sb.append('0');
		}
		sb.append(parts[2]);
		return sb.toString();
	}

	public static InputStream getInputStreamFromUrl(HttpClient client,
			String url) throws IOException {
		HttpGet get = new HttpGet(url);
		try {
			return client.execute(get).getEntity().getContent();
		} catch (IllegalStateException e) {
			get.abort();
			throw e;
		} catch (ClientProtocolException e) {
			get.abort();
			throw e;
		} catch (IOException e) {
			get.abort();
			throw e;
		}
	}
	
	/**
	 * Support just zh_CN, zh_TW, en
	 * 
	 * @param context
	 * @param name
	 * @return
	 */
	public static String getLocaleName(Context context, String name) {
		Log.d(TAG, "[getLocaleName] name -- " + name);
		String localeSuffix = getLocaleSuffix(context);
		
		if (localeSuffix == null)
			return name;
		
		StringBuilder sb = new StringBuilder(name);
		int lastDotIdx = name.lastIndexOf('.');
		if (lastDotIdx > -1) {
			sb.insert(lastDotIdx, localeSuffix);
		} else {
			sb.append(localeSuffix);
		}
		
		Log.d(TAG, "[getLocaleName] localeName -- " + sb);
		
		return sb.toString();
			
	}

	/**
	 * 
	 * @param context
	 * @return null if current locale is not in [zh_CN, zh_TW]
	 */
	private static String getLocaleSuffix(Context context) {
		Locale locale = context.getResources().getConfiguration().locale;
		
		String localeSuffix = null;
		
		if (locale.equals(Locale.SIMPLIFIED_CHINESE)) {
			localeSuffix = "_zh_CN";
		} else if (locale.equals(Locale.TRADITIONAL_CHINESE)) {
			localeSuffix = "_zh_TW";
		}
		
		return localeSuffix;
	}
	
	public static CharSequence getTextFromAssets(Context context, String name) {
		
		String filename = getLocaleName(context, name);
		
		BufferedReader in = null;
		StringBuilder sb = new StringBuilder();
		try {
			in = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));

			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			
		} catch (IOException e) {
			Log.e(TAG, "[getTextFromAssets]", e);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return sb;
	}
	
	public static Dialog createAboutDialog(final Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(inflater.inflate(R.layout.about, null));
		builder.setTitle(R.string.alert_title_about);
		builder.setNeutralButton(R.string.btn_more_apps,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(App.URL_MY_APPS)));
					}
				});
		builder.setPositiveButton(R.string.btn_ok, null);
		return builder.create();
	}
	
	public static Dialog createWhatsNewDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(R.string.alert_title_what_is_new);
		
		builder.setMessage(Utils.getTextFromAssets(context, "whatsnew.txt"));
		
		builder.setNeutralButton(R.string.btn_ok,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// 

					}
				});
		return builder.create();
	}
	
	public static boolean backupDatabase() {
		File dbFile = new File(Environment.getDataDirectory() + "/data/" + PKG + "/databases/" + DatabaseHelper.DB_NAME);

		File exportDir = new File(Environment.getExternalStorageDirectory(), "pocket-voa");
		
		if (!exportDir.exists()) {
			exportDir.mkdirs();
		}
		
		File file = new File(exportDir, dbFile.getName());

		try {
			file.createNewFile();
			copyFile(dbFile, file);
			return true;
		} catch (IOException e) {
			Log.e(TAG, "[backupDatabase] error", e);
			return false;
		}
	}
	
	public static boolean restoreDatabase() {
		File dbFile = new File(Environment.getDataDirectory() + "/data/" + PKG + "/databases/" + DatabaseHelper.DB_NAME);

		File exportDbFile = new File(Environment.getExternalStorageDirectory() + "/pocket-voa/" + DatabaseHelper.DB_NAME);
		
		if (!exportDbFile.exists())
			return false;

		try {
			dbFile.createNewFile();
			copyFile(exportDbFile, dbFile);
			return true;
		} catch (IOException e) {
			Log.e(TAG, "[restoreDatabase] error", e);
			return false;
		}
	}
	
	private static void copyFile(File src, File dst) throws IOException {
		FileChannel inChannel = new FileInputStream(src).getChannel();
		FileChannel outChannel = new FileOutputStream(dst).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}
	
	public static boolean isBackupExist() {
		File exportDbFile = new File(Environment.getExternalStorageDirectory() + "/pocket-voa/" + DatabaseHelper.DB_NAME);
		return exportDbFile.exists();
	}
	
	/**
	 * 
	 * @return -1 if file not exist
	 */
	public static long lastBackupTime() {
		File exportDbFile = new File(Environment.getExternalStorageDirectory() + "/pocket-voa/" + DatabaseHelper.DB_NAME);
		if (exportDbFile.exists()) {
			return exportDbFile.lastModified();
		} else {
			return -1;
		}
			
	}
}
