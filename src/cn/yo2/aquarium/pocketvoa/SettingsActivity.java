package cn.yo2.aquarium.pocketvoa;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

public class SettingsActivity extends PreferenceActivity {
	private static final String TAG = SettingsActivity.class.getSimpleName();

	private static final int DLG_ABOUT = 1;
	private static final int DLG_CHANGE_LOG = 2;

	private Preference mHelp;
	private Preference mAbout;
	private Preference mChangeLog;
	
	

	private OnPreferenceClickListener mOnPreferenceClickListener = new OnPreferenceClickListener() {

		@Override
		public boolean onPreferenceClick(Preference preference) {
			if (preference == mAbout) {
				showDialog(DLG_ABOUT);
				return true;
			} else if (preference == mChangeLog) {
				showDialog(DLG_CHANGE_LOG);
				return true;
			} 
			
			
			return false;
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DLG_ABOUT:
			return Utils.createAboutDialog(this);
		case DLG_CHANGE_LOG:
			return Utils.createWhatsNewDialog(this);
		default:
			break;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs_settings);

		mHelp = findPreference(getString(R.string.prefs_key_help));
		mHelp.setIntent(new Intent(this, HelpActivity.class));

		mAbout = findPreference(getString(R.string.prefs_key_about));
		mAbout.setOnPreferenceClickListener(mOnPreferenceClickListener);

		mChangeLog = findPreference(getString(R.string.prefs_key_whatsnew));
		mChangeLog.setOnPreferenceClickListener(mOnPreferenceClickListener);

	}
}
