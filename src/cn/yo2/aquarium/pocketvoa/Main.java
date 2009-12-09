package cn.yo2.aquarium.pocketvoa;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class Main extends Activity {
	private static final String CLASSTAG = Main.class.getSimpleName();

	private static final int MENU_SETTINGS = Menu.FIRST;
	private static final int MENU_EXIT = Menu.FIRST + 1;

	private static final int MENU_LOCAL_VIEW = Menu.FIRST + 2;
	private static final int MENU_DELETE = Menu.FIRST + 3;

	private static final int ERROR_ALERT_DIALOG = 0;
	private static final int PROGRESS_DIALOG = 1;
	private static final int LOCAL_LIST_ACTION_DIALOG = 2;
	private static final int REMOTE_LIST_ACTION_DIALOG = 3;
	private static final int DELETE_CONFIRM_DIALOG = 4;

	private static final int WHAT_SUCCESS = 0;
	private static final int WHAT_FAIL_IO = 1;
	private static final int WHAT_FAIL_PARSE = 2;

	private enum Error {
		LoadListError,
	}

	private Error mLastError;

	private App mApp;

	private Article mLongClickArticle;

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
				dismissDialog(PROGRESS_DIALOG);
				bindRemoteList();
				break;
			case WHAT_FAIL_IO:
			case WHAT_FAIL_PARSE:
				dismissDialog(PROGRESS_DIALOG);
				mLastError = Error.LoadListError;
				showDialog(ERROR_ALERT_DIALOG);
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
				dismissDialog(PROGRESS_DIALOG);
				bindLocalList();
				break;
			case WHAT_FAIL_IO:
			case WHAT_FAIL_PARSE:
				dismissDialog(PROGRESS_DIALOG);
				mLastError = Error.LoadListError;
				showDialog(ERROR_ALERT_DIALOG);
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

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mApp = (App) getApplication();

		extractTypes();

		setupTabs();

		// set up database
		setupDatabase();

		// set up widgets for local tab
		setupLocalTabWidgets();

		// set up widgets for remote tab
		setupRemoteTabWidgets();

		refreshLocalList();
	}

	private void setupRemoteTabWidgets() {
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
				startShowActivity(mList.get(position));
			}

		});
		
		lvRemote.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				mLongClickArticle = mList.get(position);
				showDialog(REMOTE_LIST_ACTION_DIALOG);
				return true;
			}
			
		});
	}

	private void setupLocalTabWidgets() {
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
				startShowActivity(mDatabaseHelper.queryArticle(id));
			}

		});

		lvLocal.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				mLongClickArticle = mDatabaseHelper.queryArticle(id);
				showDialog(LOCAL_LIST_ACTION_DIALOG);
				return true;
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
		tabSpec.setIndicator(getString(R.string.tab_local), getResources()
				.getDrawable(R.drawable.hd_small));
		mTabHost.addTab(tabSpec);

		tabSpec = mTabHost.newTabSpec("Remote");
		tabSpec.setContent(R.id.tab_remote);
		tabSpec.setIndicator(getString(R.string.tab_remote), getResources()
				.getDrawable(R.drawable.web_small));
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
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_SETTINGS, Menu.NONE, R.string.menu_settings)
				.setIcon(R.drawable.settings);
		menu.add(Menu.NONE, MENU_EXIT, Menu.NONE, R.string.menu_exit).setIcon(
				R.drawable.stop);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SETTINGS:
			Intent intent = new Intent(this, Settings.class);
			startActivity(intent);
			break;
		case MENU_EXIT:
			finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// @Override
	// public void onCreateContextMenu(ContextMenu menu, View v,
	// ContextMenuInfo menuInfo) {
	// menu.add(Menu.NONE, MENU_LOCAL_VIEW, Menu.NONE, R.string.menu_view);
	// menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, R.string.menu_delete);
	//
	// super.onCreateContextMenu(menu, v, menuInfo);
	// }

	// @Override
	// public boolean onContextItemSelected(MenuItem item) {
	// AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
	// .getMenuInfo();
	// switch (item.getItemId()) {
	// case MENU_LOCAL_VIEW:
	// startShowActivity(mDatabaseHelper.queryArticle(info.id));
	// return true;
	// case MENU_DELETE:
	// Article article = mDatabaseHelper.queryArticle(info.id);
	// Utils.delete("/sdcard/pocket-voa/" + Utils.extractFilename(article.url));
	// Utils.delete("/sdcard/pocket-voa/" + Utils.extractFilename(article.mp3));
	// mDatabaseHelper.deleteArticle(article.id);
	// Toast.makeText(this, "Article deleted", Toast.LENGTH_LONG).show();
	// return true;
	// default:
	// break;
	// }
	// return super.onContextItemSelected(item);
	// }

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case ERROR_ALERT_DIALOG:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.alert_title_error);
			return builder.create();

		case PROGRESS_DIALOG:
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog
					.setMessage(getString(R.string.progressspin_loadlist_msg));
			return progressDialog;
		case LOCAL_LIST_ACTION_DIALOG:
			AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
			builder2.setTitle(R.string.alert_title_select_action);
			builder2.setItems(R.array.local_list_action,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							switch (which) {
							case 0:
								startShowActivity(mLongClickArticle);
								break;
							case 1:
								showDialog(DELETE_CONFIRM_DIALOG);
								break;
							default:
								break;
							}

						}
					});
			return builder2.create();
		case REMOTE_LIST_ACTION_DIALOG:
			AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
			builder3.setTitle(R.string.alert_title_select_action);
			builder3.setItems(R.array.remote_list_action,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							switch (which) {
							case 0:
								startShowActivity(mLongClickArticle);
								break;
							case 1:
								Toast.makeText(Main.this, "Download start",
										Toast.LENGTH_LONG).show();
								break;
							default:
								break;
							}

						}
					});
			return builder3.create();
		case DELETE_CONFIRM_DIALOG:
			AlertDialog.Builder builder4 = new AlertDialog.Builder(this);
			builder4.setTitle(R.string.alert_title_confirm_delete);
			builder4.setMessage(getString(R.string.alert_msg_confirm_delete));
			builder4.setPositiveButton(R.string.btn_yes,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							Utils
									.delete("/sdcard/pocket-voa/"
											+ Utils
													.extractFilename(mLongClickArticle.url));
							Utils
									.delete("/sdcard/pocket-voa/"
											+ Utils
													.extractFilename(mLongClickArticle.mp3));
							mDatabaseHelper.deleteArticle(mLongClickArticle.id);
							Toast.makeText(Main.this,
									R.string.toast_article_deleted,
									Toast.LENGTH_LONG).show();
						}
					});

			builder4.setNegativeButton(R.string.btn_no,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			return builder4.create();
		default:
			break;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case ERROR_ALERT_DIALOG:
			AlertDialog alertDialog = (AlertDialog) dialog;
			switch (mLastError) {
			case LoadListError:
				alertDialog
						.setMessage(getString(R.string.alert_msg_loadlist_error));
				break;

			default:
				break;
			}
			break;

		default:
			break;
		}
		super.onPrepareDialog(id, dialog);
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
			mSimpleCursorAdapter.setViewBinder(new ViewBinder() {

				public boolean setViewValue(View view, Cursor cursor,
						int columnIndex) {
					if (view.getId() == R.id.tv_title) {
						TextView tvTitle = (TextView) view;
						String title = cursor.getString(cursor
								.getColumnIndex(DatabaseHelper.C_TITLE));
						String date = Utils
								.convertDateString(cursor.getString(cursor
										.getColumnIndex(DatabaseHelper.C_DATE)));
						tvTitle.setText(String.format("%s (%s)", title, date));
						return true;
					}
					return false;

				}
			});
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
				if (subtypeIndex == -1) {
					subtypeIndex = 0;
				}

				Log.d(CLASSTAG, "typeIndex -- " + typeIndex);
				Log.d(CLASSTAG, "subtypeIndex -- " + subtypeIndex);

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

				if (subtypeIndex == -1) {
					subtypeIndex = 0;
				}

				String key = mTypes[typeIndex] + "_"
						+ mSubtypes[typeIndex][subtypeIndex];
				Log.d(CLASSTAG, "key -- " + key);
				mApp.mListGenerator.mParser = mApp.mListParsers.get(key);
				try {
					mList = mApp.mListGenerator.getArticleList(mApp.mListUrls
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

	private void startShowActivity(Article article) {
		mApp.article = article;

		Intent intent = new Intent(Main.this, Show.class);
		startActivity(intent);
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

		Article article = mList.get(position);
		if (TextUtils.isEmpty(article.date))
			wraper.getTitle().setText(article.title);
		else
			wraper.getTitle().setText(
					String.format("%s (%s)", article.title, Utils
							.convertDateString(article.date)));

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
