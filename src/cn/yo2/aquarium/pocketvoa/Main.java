package cn.yo2.aquarium.pocketvoa;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class Main extends Activity {
	private static final String CLASSTAG = Main.class.getSimpleName();

	private static final int MENU_SETTINGS = Menu.FIRST;
	private static final int MENU_EXIT = Menu.FIRST + 1;

	private static final int PROGRESS_DIALOG = 1;

	private static final int WHAT_SUCCESS = 0;
	private static final int WHAT_FAIL_IO = 1;
	private static final int WHAT_FAIL_PARSE = 2;

	private String[] mTypes;
	private String[][] mSubtypes;

	private ArrayAdapter<CharSequence>[] mAdapters;

	private ArrayList<Article> mList;

	private Cursor mCursor;
	private SimpleCursorAdapter mSimpleCursorAdapter;
	private DatabaseHelper mDatabaseHelper;

	private Handler mRemoteListHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_SUCCESS:
				bindRemoteList();
				dismissDialog(PROGRESS_DIALOG);
				break;
			case WHAT_FAIL_IO:
				Toast.makeText(Main.this, "FAIL IO", Toast.LENGTH_LONG).show();
				break;
			case WHAT_FAIL_PARSE:
				Toast.makeText(Main.this, "FAIL PARSE", Toast.LENGTH_LONG)
						.show();
				break;
			default:
				break;
			}
		}

	};

	private Handler mLocalListHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_SUCCESS:
				bindLocalList();
				dismissDialog(PROGRESS_DIALOG);
				break;
			case WHAT_FAIL_IO:
				Toast.makeText(Main.this, "FAIL IO", Toast.LENGTH_LONG).show();
				break;
			case WHAT_FAIL_PARSE:
				Toast.makeText(Main.this, "FAIL PARSE", Toast.LENGTH_LONG)
						.show();
				break;
			default:
				break;
			}
		}

	};

	private TabHost mTabHost;

	private Button btnRefreshLocal;
	private TextView tvLocal;
	private ListView lvLocal;
	private Spinner spnTypeLocal;
	private Spinner spnSubtypeLocal;

	private Button btnRefreshRemote;
	private TextView tvRemote;
	private ListView lvRemote;
	private Spinner spnTypeRemote;
	private Spinner spnSubtypeRemote;

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		extractTypes();

		setupTabs();

		// set up database
		setupDatabase();

		// set up for local tab
		setupLocalTab();

		// set up for remote tab
		setupRemoteTab();

		// mList = (ArrayList<Article>) getLastNonConfigurationInstance();
		//
		// if (mList == null)
		// refreshRemoteList();
		// else
		// bindRemoteList();
	}

	private void setupRemoteTab() {
		spnTypeRemote = (Spinner) findViewById(R.id.spinner_type_remote);
		spnTypeRemote.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				spnSubtypeRemote.setAdapter(mAdapters[position]);
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}

		});
		spnSubtypeRemote = (Spinner) findViewById(R.id.spinner_subtype_remote);

		btnRefreshRemote = (Button) findViewById(R.id.btn_refresh_remote);
		btnRefreshRemote.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				refreshRemoteList();
			}
		});

		tvRemote = (TextView) findViewById(R.id.empty_remote);
		lvRemote = (ListView) findViewById(R.id.list_remote);
		lvRemote.setEmptyView(tvRemote);

		lvRemote.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				App.article = mList.get(position);

				Intent intent = new Intent(Main.this, Show.class);
				startActivity(intent);
			}

		});
	}

	private void setupLocalTab() {
		spnTypeLocal = (Spinner) findViewById(R.id.spinner_type_local);
		spnTypeLocal.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				spnSubtypeLocal.setAdapter(mAdapters[position]);
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}

		});

		spnSubtypeLocal = (Spinner) findViewById(R.id.spinner_subtype_local);

		btnRefreshLocal = (Button) findViewById(R.id.btn_refresh_local);
		btnRefreshLocal.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				refreshLocalList();
			}
		});

		tvLocal = (TextView) findViewById(R.id.empty_local);
		lvLocal = (ListView) findViewById(R.id.list_local);
		lvLocal.setEmptyView(tvLocal);
		lvLocal.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				App.article = mDatabaseHelper.queryArticle(id);

				Intent intent = new Intent(Main.this, Show.class);
				startActivity(intent);
			}

		});
	}

	private void setupDatabase() {
		mDatabaseHelper = new DatabaseHelper(this);
		mDatabaseHelper.open();
	}

	private void setupTabs() {
		mTabHost = (TabHost) findViewById(R.id.tabhost);
		mTabHost.setup();

		TabHost.TabSpec tabSpec = mTabHost.newTabSpec("Local");
		tabSpec.setContent(R.id.tab_local);
		tabSpec.setIndicator("Local", getResources().getDrawable(R.drawable.hd));
		mTabHost.addTab(tabSpec);

		tabSpec = mTabHost.newTabSpec("Remote");
		tabSpec.setContent(R.id.tab_remote);
		tabSpec.setIndicator("Remote", getResources().getDrawable(R.drawable.web));
		mTabHost.addTab(tabSpec);

		mTabHost.setCurrentTab(0);
	}

	private void extractTypes() {
		mTypes = getResources().getStringArray(R.array.type);

		mSubtypes = new String[][] {
				getResources().getStringArray(R.array.standard_english),
				getResources().getStringArray(R.array.special_english),
				getResources().getStringArray(R.array.english_learning), };

		ArrayAdapter<CharSequence> stAdapter = ArrayAdapter.createFromResource(
				this, R.array.standard_english,
				android.R.layout.simple_spinner_item);
		stAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		ArrayAdapter<CharSequence> spAdapter = ArrayAdapter.createFromResource(
				this, R.array.special_english,
				android.R.layout.simple_spinner_item);
		spAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		ArrayAdapter<CharSequence> elAdapter = ArrayAdapter.createFromResource(
				this, R.array.english_learning,
				android.R.layout.simple_spinner_item);
		elAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		mAdapters = new ArrayAdapter[] { stAdapter, spAdapter, elAdapter, };
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return mList;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SETTINGS:

			break;
		case MENU_EXIT:

			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_SETTINGS, Menu.NONE, "Settings").setIcon(
				android.R.drawable.ic_menu_preferences);
		menu.add(Menu.NONE, MENU_EXIT, Menu.NONE, "Exit").setIcon(
				android.R.drawable.ic_menu_close_clear_cancel);

		return super.onCreateOptionsMenu(menu);
	}

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
	protected void onDestroy() {
		if (null != mCursor && !mCursor.isClosed())
			mCursor.close();
		mDatabaseHelper.close();

		super.onDestroy();
	}

	private void bindRemoteList() {
		RowAdapter adapter = new RowAdapter(this, mList);
		lvRemote.setAdapter(adapter);
	}

	private void bindLocalList() {
		if (mSimpleCursorAdapter == null) {
			mSimpleCursorAdapter = new SimpleCursorAdapter(this,
					R.layout.list_item, mCursor,
					new String[] { DatabaseHelper.C_TITLE },
					new int[] { R.id.tv_title });
			lvLocal.setAdapter(mSimpleCursorAdapter);
		} else {
			mSimpleCursorAdapter.changeCursor(mCursor);
		}

	}

	private void refreshLocalList() {
		showDialog(PROGRESS_DIALOG);

		new Thread() {

			@Override
			public void run() {
				int typeIndex = spnTypeLocal.getSelectedItemPosition();
				int subtypeIndex = spnSubtypeLocal.getSelectedItemPosition();

				mCursor = mDatabaseHelper.queryArticles(mTypes[typeIndex],
						mSubtypes[typeIndex][subtypeIndex]);
				mLocalListHandler.sendEmptyMessage(WHAT_SUCCESS);
			}

		}.start();
	}

	private void refreshRemoteList() {
		showDialog(PROGRESS_DIALOG);
		new Thread() {

			@Override
			public void run() {
				int typeIndex = spnTypeRemote.getSelectedItemPosition();
				int subtypeIndex = spnSubtypeRemote.getSelectedItemPosition();

				String key = mTypes[typeIndex] + "_"
						+ mSubtypes[typeIndex][subtypeIndex];
				Log.d(CLASSTAG, "key -- " + key);
				App.LIST_GENERATOR.mParser = App.LIST_PARSERS.get(key);
				try {
					mList = App.LIST_GENERATOR.getArticleList(App.LIST_URLS
							.get(key));
					mRemoteListHandler.sendEmptyMessage(WHAT_SUCCESS);
				} catch (IOException e) {
					mRemoteListHandler.sendEmptyMessage(WHAT_FAIL_IO);
				} catch (IllegalContentFormatException e) {
					mRemoteListHandler.sendEmptyMessage(WHAT_FAIL_PARSE);
				}

			}

		}.start();
	}
}

class RowAdapter extends BaseAdapter {

	private ArrayList<Article> mList;
	private Context mContext;
	private LayoutInflater mInflater;

	public RowAdapter(Context context, ArrayList<Article> list) {
		super();
		mContext = context;
		mList = list;
		mInflater = LayoutInflater.from(mContext);
	}

	public int getCount() {
		return mList.size();
	}

	public Object getItem(int position) {
		return mList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewWraper wraper = null;
		if (row == null) {
			row = mInflater.inflate(R.layout.list_item, null);
			wraper = new ViewWraper(row);
			row.setTag(wraper);
		} else {
			wraper = (ViewWraper) row.getTag();
		}

		wraper.getTitle().setText(mList.get(position).title);

		return row;
	}

}

class ViewWraper {
	View root;
	TextView tvTitle;

	public ViewWraper(View root) {
		this.root = root;
	}

	TextView getTitle() {
		if (tvTitle == null) {
			tvTitle = (TextView) root.findViewById(R.id.tv_title);
		}
		return tvTitle;
	}
}
