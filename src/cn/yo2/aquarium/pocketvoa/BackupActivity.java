package cn.yo2.aquarium.pocketvoa;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class BackupActivity extends PreferenceActivity implements OnPreferenceClickListener {
	
	private static final String TAG = BackupActivity.class.getSimpleName();
	private static final java.text.DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private Preference mBackup;
	private Preference mRestore;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}
	
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs_backup_restore);
		
		mBackup = findPreference(getString(R.string.prefs_key_data_backup));
		mBackup.setOnPreferenceClickListener(this);
		
		mRestore = findPreference(getString(R.string.prefs_key_data_restore));
		mRestore.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference == mBackup) {
			backupDatabase();
			return true;
		} else if (preference == mRestore) {
			restoreDatabase();
			return true;
		}	
		
		return false;
	}
	
	
	private void restoreDatabase() {
		new restoreDatabaseTask(this).execute();
	}

	private void backupDatabase() {
		new backupDatabaseTask(this).execute();
	}
	
	public class restoreDatabaseTask extends AsyncTask<Void, Void, Boolean> {
		
		private Context mContext;
		
		

		public restoreDatabaseTask(Context mContext) {
			super();
			this.mContext = mContext;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			return Utils.restoreDatabase();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				Toast.makeText(mContext, "Restore successful!", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, "Restore failed", Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	
	public class backupDatabaseTask extends AsyncTask<Void, Void, Boolean> {
		private Context mContext;
		
		

		public backupDatabaseTask(Context mContext) {
			super();
			this.mContext = mContext;
		}

		

		@Override
		protected Boolean doInBackground(Void... params) {
			return Utils.backupDatabase();
		}



		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				Toast.makeText(mContext, "Backup successful!", Toast.LENGTH_SHORT).show();
				updateBackupInfo();
			} else {
				Toast.makeText(mContext, "Backup failed", Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		startActivity(new Intent(this, MainActivity.class));
	}
	
	private void updateBackupInfo() {
		long lastBackup = Utils.lastBackupTime();
		if (lastBackup != -1L) {
			mRestore.setEnabled(true);
			mRestore.setSummary(getString(R.string.prefs_summary_data_restore, DATE_FORMAT.format(new Date(lastBackup))));
		} else {
			mRestore.setEnabled(false);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		updateBackupInfo();
	}
}
