package cn.yo2.aquarium.pocketvoa;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Local extends Activity {
	private static final String CLASSTAG = Local.class.getSimpleName();

	private static final int MENU_REMOTE = Menu.FIRST;
	private static final int MENU_LOCAL = Menu.FIRST + 1;
	
	private static final int WHAT_SUCCESS = 0;
	private static final int WHAT_FAIL_DB = 1;

	private static final int PROGRESS_DIALOG = 1;

	private Button btnRefreshStandard;
	private TextView tvStandard;
	private ListView lvStandard;
	private Button btnRefreshSpecial;
	private TextView tvSpecial;
	private ListView lvSpecial;

	private Cursor mCursor;
	private DatabaseHelper mDatabaseHelper;
	
	private Handler mRefreshListHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_SUCCESS:
				bindList();
				dismissDialog(PROGRESS_DIALOG);
				break;
			case WHAT_FAIL_DB:
				dismissDialog(PROGRESS_DIALOG);
				Toast.makeText(Local.this, "Query DB fail.", Toast.LENGTH_LONG);
			default:
				break;
			}
		}
		
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_DIALOG:
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setTitle("Loading");
			progressDialog.setMessage("Loading");
			return progressDialog;
		default:
			break;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local);

		mDatabaseHelper = new DatabaseHelper(this);
		mDatabaseHelper.open();

		TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
		tabHost.setup();

		TabHost.TabSpec tabSpec = tabHost.newTabSpec("Standard");
		tabSpec.setContent(R.id.tab_standard);
		tabSpec.setIndicator("Standard");
		tabHost.addTab(tabSpec);

		tabSpec = tabHost.newTabSpec("Special");
		tabSpec.setContent(R.id.tab_special);
		tabSpec.setIndicator("Special");
		tabHost.addTab(tabSpec);

		tabHost.setCurrentTab(0);

		btnRefreshStandard = (Button) findViewById(R.id.btn_refresh_standard);
		tvStandard = (TextView) findViewById(R.id.empty_standard);
		lvStandard = (ListView) findViewById(R.id.list_standard);
		lvStandard.setEmptyView(tvStandard);

		lvStandard.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mCursor.moveToPosition(position);
				Intent intent = new Intent(Local.this, Show.class);
				intent.putExtra(DatabaseHelper.C_TITLE, mCursor
						.getString(mCursor
								.getColumnIndex(DatabaseHelper.C_TITLE)));
				intent.putExtra(DatabaseHelper.C_URL, mCursor.getString(mCursor
						.getColumnIndex(DatabaseHelper.C_URL)));
				intent.putExtra(DatabaseHelper.C_MP3, mCursor.getString(mCursor
						.getColumnIndex(DatabaseHelper.C_MP3)));
				startActivity(intent);
			}

		});

		btnRefreshStandard.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				refreshList();
			}
		});

		btnRefreshSpecial = (Button) findViewById(R.id.btn_refresh_special);
		tvSpecial = (TextView) findViewById(R.id.empty_special);
		lvSpecial = (ListView) findViewById(R.id.list_special);
		lvSpecial.setEmptyView(tvSpecial);

		mCursor = (Cursor) getLastNonConfigurationInstance();

		if (mCursor == null)
			refreshList();
		else
			bindList();
	}

	protected void refreshList() {
		showDialog(PROGRESS_DIALOG);
		new Thread() {

			@Override
			public void run() {
				mCursor = mDatabaseHelper.queryArticles(null, null);
				mRefreshListHandler.sendEmptyMessage(WHAT_SUCCESS);
			}
		}.start();
	}

	private void bindList() {
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.list_item, mCursor,
				new String[] { DatabaseHelper.C_TITLE },
				new int[] { R.id.tv_title });
		lvStandard.setAdapter(adapter);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return mCursor;
	}

	@Override
	protected void onDestroy() {
		if (null != mCursor && !mCursor.isClosed())
			mCursor.close();
		mDatabaseHelper.close();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_REMOTE, Menu.NONE, "Remote");
		menu.add(Menu.NONE, MENU_LOCAL, Menu.NONE, "Local").setEnabled(false);

		return super.onCreateOptionsMenu(menu);
	}
}
