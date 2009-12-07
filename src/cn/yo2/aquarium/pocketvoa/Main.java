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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Main extends Activity {
	private static final String CLASSTAG = Main.class.getSimpleName();

	private static final int MENU_REMOTE = Menu.FIRST;
	private static final int MENU_LOCAL = Menu.FIRST + 1;

	private static final int PROGRESS_DIALOG = 1;

	private static final int WHAT_SUCCESS = 0;
	private static final int WHAT_FAIL_IO = 1;
	private static final int WHAT_FAIL_PARSE = 2;

	private boolean mIsRemote;

	private String[] mTypes;
	private String[][] mSubtypes;

	private String mType;
	private String mSubtype;

	private ArrayList<Article> mList;

	private Cursor mCursor;
	private DatabaseHelper mDatabaseHelper;

	private Handler mListHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_SUCCESS:
				bindList();
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

	private Button btnRefreshStandard;
	private TextView tvStandard;
	private ListView lvStandard;
	private Spinner spnStandard;

	private Button btnRefreshSpecial;
	private TextView tvSpecial;
	private ListView lvSpecial;
	private Spinner spnSpecial;

	private Button btnRefreshLearning;
	private TextView tvLearning;
	private ListView lvLearning;
	private Spinner spnLearning;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		extractTypes();

		setupTabs();

		mDatabaseHelper = new DatabaseHelper(this);
		mDatabaseHelper.open();

		spnStandard = (Spinner) findViewById(R.id.spinner_standard);
		btnRefreshStandard = (Button) findViewById(R.id.btn_refresh_standard);
		tvStandard = (TextView) findViewById(R.id.empty_standard);
		lvStandard = (ListView) findViewById(R.id.list_standard);
		lvStandard.setEmptyView(tvStandard);

		lvStandard.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mIsRemote)
					App.article = mList.get(position);
				else
					App.article = mDatabaseHelper.queryArticle(id);

				Intent intent = new Intent(Main.this, Show.class);
				startActivity(intent);
			}

		});

		btnRefreshStandard.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				refreshList();
			}
		});

		spnSpecial = (Spinner) findViewById(R.id.spinner_special);
		btnRefreshSpecial = (Button) findViewById(R.id.btn_refresh_special);
		tvSpecial = (TextView) findViewById(R.id.empty_special);
		lvSpecial = (ListView) findViewById(R.id.list_special);
		lvSpecial.setEmptyView(tvSpecial);

		spnLearning = (Spinner) findViewById(R.id.spinner_learning);
		btnRefreshLearning = (Button) findViewById(R.id.btn_refresh_learning);
		tvLearning = (TextView) findViewById(R.id.empty_learning);
		lvLearning = (ListView) findViewById(R.id.list_learning);
		lvLearning.setEmptyView(tvLearning);

		mList = (ArrayList<Article>) getLastNonConfigurationInstance();

		if (mList == null)
			refreshList();
		else
			bindList();
	}

	private void setupTabs() {
		mTabHost = (TabHost) findViewById(R.id.tabhost);
		mTabHost.setup();

		TabHost.TabSpec tabSpec = mTabHost.newTabSpec("Standard English");
		tabSpec.setContent(R.id.tab_standard);
		tabSpec.setIndicator("Standard English");
		mTabHost.addTab(tabSpec);

		tabSpec = mTabHost.newTabSpec("Special English");
		tabSpec.setContent(R.id.tab_special);
		tabSpec.setIndicator("Special English");
		mTabHost.addTab(tabSpec);

		tabSpec = mTabHost.newTabSpec("English Learning");
		tabSpec.setContent(R.id.tab_learning);
		tabSpec.setIndicator("English Learning");
		mTabHost.addTab(tabSpec);

		mTabHost.setCurrentTab(0);
	}

	private void extractTypes() {
		mTypes = getResources().getStringArray(R.array.type);
		mSubtypes = new String[][] {
				getResources().getStringArray(R.array.standard_english),
				getResources().getStringArray(R.array.special_english),
				getResources().getStringArray(R.array.english_learning), };
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return mList;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REMOTE:
			mIsRemote = true;
			refreshList();
			break;
		case MENU_LOCAL:
			mIsRemote = false;
			refreshList();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_REMOTE, Menu.NONE, "Remote");
		menu.add(Menu.NONE, MENU_LOCAL, Menu.NONE, "Local");

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		menu.getItem(0).setEnabled(!mIsRemote);
		menu.getItem(1).setEnabled(mIsRemote);

		return super.onPrepareOptionsMenu(menu);
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

	private void bindList() {
		ListAdapter adapter;
		if (mIsRemote)
			adapter = new RowAdapter(this, mList);
		else {
			adapter = new SimpleCursorAdapter(this, R.layout.list_item,
					mCursor, new String[] { DatabaseHelper.C_TITLE },
					new int[] { R.id.tv_title });
		}
		lvStandard.setAdapter(adapter);
	}

	private void refreshList() {
		showDialog(PROGRESS_DIALOG);
		new Thread() {

			@Override
			public void run() {
				int tabIndex = mTabHost.getCurrentTab();
				int spinIndex = 0;
				switch (tabIndex) {
				case 0:
					spinIndex = spnStandard.getSelectedItemPosition();
					break;
				case 1:
					spinIndex = spnSpecial.getSelectedItemPosition();
					break;
				case 2:
					spinIndex = spnLearning.getSelectedItemPosition();
					break;
				default:
					break;
				}
				if (mIsRemote) {
					String key = mTypes[tabIndex] + "_"
							+ mSubtypes[tabIndex][spinIndex];
					Log.d(CLASSTAG, "key -- " + key);
					App.LIST_GENERATOR.mParser = App.LIST_PARSERS.get(key);
					try {
						mList = App.LIST_GENERATOR.getArticleList(App.LIST_URLS
								.get(key));
						mListHandler.sendEmptyMessage(WHAT_SUCCESS);
					} catch (IOException e) {
						mListHandler.sendEmptyMessage(WHAT_FAIL_IO);
					} catch (IllegalContentFormatException e) {
						mListHandler.sendEmptyMessage(WHAT_FAIL_PARSE);
					}
				} else {
					if (null != mCursor && !mCursor.isClosed())
						mCursor.close();
					mCursor = mDatabaseHelper.queryArticles(mTypes[tabIndex],
							mSubtypes[tabIndex][spinIndex]);
					mListHandler.sendEmptyMessage(WHAT_SUCCESS);
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
