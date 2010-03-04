package cn.yo2.aquarium.pocketvoa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
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
import android.widget.ImageView;
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
import cn.yo2.aquarium.pocketvoa.parser.IListParser;

public class Main extends Activity {
	private static final String CLASSTAG = Main.class.getSimpleName();

	private static final int MENU_SETTINGS = Menu.FIRST;
	private static final int MENU_TEST = Menu.FIRST + 1;
	private static final int MENU_ABOUT = Menu.FIRST + 2;
	private static final int MENU_EXIT = Menu.FIRST + 3;

	private static final int DLG_ERROR = 0;
	private static final int DLG_PROGRESS = 1;
	private static final int DLG_MENU_LOCAL_LIST_ = 2;
	private static final int DLG_MENU_REMOTE_LIST = 3;
	private static final int DLG_CONFIRM_DELETE = 4;
	private static final int DLG_CONFIRM_DOWNLOAD = 5;
	private static final int DLG_ABOUT = 6;
	private static final int DLG_INTERNET_STATUS_CONNECTED = 7;
	private static final int DLG_INTERNET_STATUS_DISCONNECTED = 8;

	private static final int WHAT_SUCCESS = 0;
	private static final int WHAT_FAIL_IO = 1;
	private static final int WHAT_FAIL_PARSE = 2;
	
	private static final String[] TYPES_REMOTE = { 
			"Standard English",
			"Special English", 
			"English Learning", 
	};
	
	private static final String[][] SUBTYPES_REMOTE = {
			{ "English News", },
			{ "Development Report", "This is America", "Agriculture Report",
					"Science in the News", "Health Report", "Explorations",
					"Education Report", "The Making of a Nation",
					"Economics Report", "American Mosaic", "In the News",
					"American Stories", "Words And Their Stories",
					"People in America", }, 
			{ "Popular American", }, 
	};
	
	private static final String[] TYPES_LOCAL = { "Standard English",
			"Special English", "English Learning", };

	private static final String[][] SUBTYPES_LOCAL = {
			{ "All", "English News", },
			{ "All", "Development Report", "This is America", "Agriculture Report",
					"Science in the News", "Health Report", "Explorations",
					"Education Report", "The Making of a Nation",
					"Economics Report", "American Mosaic", "In the News",
					"American Stories", "Words And Their Stories",
					"People in America", }, { "All", "Popular American", }, };

	private enum Error {
		LoadListError, DownloadError,
	}

	private Error mLastError;

	private App mApp;

	private Article mLongClickArticle;

	private ArrayAdapter<CharSequence>[] mAdaptersRemote;
	private ArrayAdapter<CharSequence>[] mAdaptersLocal;

	private ArrayList<Article> mList;

	private Cursor mCursor;
	private SimpleCursorAdapter mSimpleCursorAdapter;
	private DatabaseHelper mDatabaseHelper;

	private OnSharedPreferenceChangeListener mSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {

		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {

			if (getString(R.string.prefs_list_count_key).equals(key)) {

				int maxCount = mApp.getMaxCountFromPrefs(sharedPreferences);
				// Log.d(CLASSTAG, "max count: " + maxCount);
				for (Iterator<IListParser> i = mApp.mDataSource
						.getListParsers().values().iterator(); i.hasNext();) {
					i.next().setMaxCount(maxCount);
				}
			} else if (getString(R.string.prefs_datasource_key).equals(key)) {
				mApp.mDataSource = mApp
						.getDataSourceFromPrefs(sharedPreferences);
				updateTitle();
			}
		}
	};

	private Handler mDownloadHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_SUCCESS:
				dismissDialog(DLG_PROGRESS);
				// TODO check if the article has been downloaded
				if (mDatabaseHelper.isArticleExist(mLongClickArticle)) {
					showDialog(DLG_CONFIRM_DOWNLOAD);
				} else {
					downloadArticleInService(mLongClickArticle);
					Toast.makeText(Main.this, R.string.toast_download_start,
							Toast.LENGTH_SHORT).show();
				}

				break;
			case WHAT_FAIL_IO:
			case WHAT_FAIL_PARSE:
				dismissDialog(DLG_PROGRESS);
				mLastError = Error.DownloadError;
				showDialog(DLG_ERROR);
				break;
			default:
				break;
			}
		}

	};

	private Handler mRemoteListHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_SUCCESS:
				dismissDialog(DLG_PROGRESS);
				bindRemoteList();
				break;
			case WHAT_FAIL_IO:
			case WHAT_FAIL_PARSE:
				dismissDialog(DLG_PROGRESS);
				mLastError = Error.LoadListError;
				showDialog(DLG_ERROR);
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
				dismissDialog(DLG_PROGRESS);
				bindLocalList();
				break;
			case WHAT_FAIL_IO:
			case WHAT_FAIL_PARSE:
				dismissDialog(DLG_PROGRESS);
				mLastError = Error.LoadListError;
				showDialog(DLG_ERROR);
				break;
			default:
				break;
			}
		}

	};

	private Handler mInternetStatusHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_SUCCESS:
				dismissDialog(DLG_PROGRESS);
				showDialog(DLG_INTERNET_STATUS_CONNECTED);
				break;
			case WHAT_FAIL_IO:
				dismissDialog(DLG_PROGRESS);
				showDialog(DLG_INTERNET_STATUS_DISCONNECTED);
				break;
			default:
				break;
			}
		}

	};

	private TabHost mTabHost;

	private Button btnRefreshLocal;
	private ImageView tvLocalEmpty;
	private ListView lvLocal;
	private Spinner spnTypeLocal;
	private Spinner spnSubtypeLocal;

	private Button btnRefreshRemote;
	private ImageView tvRemoteEmpty;
	private ListView lvRemote;
	private Spinner spnTypeRemote;
	private Spinner spnSubtypeRemote;

	private LayoutInflater mInflater;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mInflater = LayoutInflater.from(this);

		mApp = (App) getApplication();

		mApp.mSharedPreferences
				.registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);

		updateTitle();

		extractTypesLocal();
		extractTypesRemote();

		setupTabs();

		// set up database
		setupDatabase();

		// set up widgets for local tab
		setupLocalTabWidgets();

		// set up widgets for remote tab
		setupRemoteTabWidgets();

		refreshLocalList();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Toast.makeText(this, R.string.toast_open_context_menu,
		// Toast.LENGTH_SHORT).show();
	}

	private void updateTitle() {
		setTitle(getString(R.string.app_name) + " - "
				+ mApp.mDataSource.getName());
	}

	private void setupRemoteTabWidgets() {
		spnTypeRemote = (Spinner) findViewById(R.id.spinner_type_remote);
		spnTypeRemote.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				spnSubtypeRemote.setAdapter(mAdaptersRemote[position]);
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// 

			}

		});

		spnSubtypeRemote = (Spinner) findViewById(R.id.spinner_subtype_remote);

		btnRefreshRemote = (Button) findViewById(R.id.btn_refresh_remote);
		btnRefreshRemote.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				refreshRemoteList();
			}
		});

		tvRemoteEmpty = (ImageView) findViewById(R.id.empty_remote);
		lvRemote = (ListView) findViewById(R.id.list_remote);
		lvRemote.setEmptyView(tvRemoteEmpty);

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
				showDialog(DLG_MENU_REMOTE_LIST);
				return true;
			}

		});
	}

	private void setupLocalTabWidgets() {
		spnTypeLocal = (Spinner) findViewById(R.id.spinner_type_local);
		spnTypeLocal.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				spnSubtypeLocal.setAdapter(mAdaptersLocal[position]);

			}

			public void onNothingSelected(AdapterView<?> parent) {
				// 

			}

		});

		spnSubtypeLocal = (Spinner) findViewById(R.id.spinner_subtype_local);

		btnRefreshLocal = (Button) findViewById(R.id.btn_refresh_local);
		btnRefreshLocal.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				refreshLocalList();
			}
		});

		tvLocalEmpty = (ImageView) findViewById(R.id.empty_local);
		lvLocal = (ListView) findViewById(R.id.list_local);
		lvLocal.setEmptyView(tvLocalEmpty);
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
				showDialog(DLG_MENU_LOCAL_LIST_);
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

	private void extractTypesRemote() {
//		mTypesRemote = getResources().getStringArray(R.array.type);
//
//		mSubtypesRemote = new String[][] {
//				getResources().getStringArray(R.array.standard_english),
//				getResources().getStringArray(R.array.special_english),
//				getResources().getStringArray(R.array.english_learning), };

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

		mAdaptersRemote = new ArrayAdapter[] { stAdapter, spAdapter, elAdapter, };

	}

	private void extractTypesLocal() {
		// local types: add all option
//		mTypesLocal = getResources().getStringArray(R.array.type_local);
//
//		mSubtypesLocal = new String[][] {
//				getResources().getStringArray(R.array.standard_english_local),
//				getResources().getStringArray(R.array.special_english_local),
//				getResources().getStringArray(R.array.english_learning_local), };

		ArrayAdapter<CharSequence> stAdapterLocal = ArrayAdapter
				.createFromResource(this, R.array.standard_english_local,
						android.R.layout.simple_spinner_item);
		stAdapterLocal
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		ArrayAdapter<CharSequence> spAdapterLocal = ArrayAdapter
				.createFromResource(this, R.array.special_english_local,
						android.R.layout.simple_spinner_item);
		spAdapterLocal
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		ArrayAdapter<CharSequence> elAdapterLocal = ArrayAdapter
				.createFromResource(this, R.array.english_learning_local,
						android.R.layout.simple_spinner_item);
		elAdapterLocal
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		mAdaptersLocal = new ArrayAdapter[] { stAdapterLocal, spAdapterLocal,
				elAdapterLocal, };
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_SETTINGS, Menu.NONE, R.string.menu_settings)
				.setIcon(android.R.drawable.ic_menu_preferences);
		menu
				.add(Menu.NONE, MENU_TEST, Menu.NONE,
						R.string.menu_internet_status).setIcon(
						R.drawable.signal);
		menu.add(Menu.NONE, MENU_ABOUT, Menu.NONE, R.string.menu_about)
				.setIcon(android.R.drawable.ic_menu_info_details);
		menu.add(Menu.NONE, MENU_EXIT, Menu.NONE, R.string.menu_exit).setIcon(
				android.R.drawable.ic_menu_close_clear_cancel);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case MENU_SETTINGS:
			Intent intent = new Intent(this, Settings.class);
			startActivity(intent);
			return true;
		case MENU_TEST:
			testInternet();
			return true;
		case MENU_ABOUT:
			showDialog(DLG_ABOUT);
			return true;
		case MENU_EXIT:
			stopService(new Intent(this, DownloadService.class));
			finish();
			return true;

		default:
			break;
		}
		return result;
	}

	private void testInternet() {
		showDialog(DLG_PROGRESS);
		new Thread() {

			@Override
			public void run() {
				if (Utils.hasInternet(Main.this))
					mInternetStatusHandler.sendEmptyMessage(WHAT_SUCCESS);
				else
					mInternetStatusHandler.sendEmptyMessage(WHAT_FAIL_IO);
			}

		}.start();
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
		case DLG_ERROR:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setTitle(R.string.alert_title_error);
			// without this statement, you would not be able to change
			// AlertDialog's message in onPreparedDialog
			builder.setMessage("");
			builder.setNeutralButton(R.string.btn_ok,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// 

						}
					});
			return builder.create();

		case DLG_PROGRESS:
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog
					.setMessage(getString(R.string.progressspin_loadlist_msg));
			return progressDialog;
		case DLG_MENU_LOCAL_LIST_:
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
								showDialog(DLG_CONFIRM_DELETE);
								break;
							default:
								break;
							}

						}
					});
			return builder2.create();
		case DLG_MENU_REMOTE_LIST:
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
								loadRemoteArticle(mLongClickArticle);
								break;
							default:
								break;
							}

						}
					});
			return builder3.create();
		case DLG_CONFIRM_DELETE:
			AlertDialog.Builder builder4 = new AlertDialog.Builder(this);
			builder4.setIcon(android.R.drawable.ic_dialog_alert);
			builder4.setTitle(R.string.alert_title_confirm_delete);
			// without this statement, you would not be able to change
			// AlertDialog's message in onPreparedDialog
			builder4.setMessage("");
			builder4.setPositiveButton(R.string.btn_yes,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							Utils.delete(Utils.localTextFile(mLongClickArticle)
									.getAbsolutePath());
							Utils.delete(Utils.localMp3File(mLongClickArticle)
									.getAbsolutePath());
							mDatabaseHelper.deleteArticle(mLongClickArticle.id);
							Toast.makeText(Main.this,
									R.string.toast_article_deleted,
									Toast.LENGTH_SHORT).show();
							refreshLocalList();
						}
					});

			builder4.setNegativeButton(R.string.btn_no,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			return builder4.create();
		case DLG_ABOUT:
			AlertDialog.Builder builder5 = new AlertDialog.Builder(this);

			builder5.setView(mInflater.inflate(R.layout.about, null));
			builder5.setTitle(R.string.alert_title_about);
			builder5.setNeutralButton(R.string.btn_ok,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// 

						}
					});
			return builder5.create();
		case DLG_CONFIRM_DOWNLOAD:
			AlertDialog.Builder builder6 = new AlertDialog.Builder(this);
			builder6.setIcon(android.R.drawable.ic_dialog_alert);
			builder6.setTitle(R.string.alert_title_confirm_download);
			// without this statement, you would not be able to change
			// AlertDialog's message in onPreparedDialog
			builder6.setMessage("");
			builder6.setPositiveButton(R.string.btn_yes,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							downloadArticleInService(mLongClickArticle);
							Toast.makeText(Main.this,
									R.string.toast_download_start,
									Toast.LENGTH_SHORT).show();
						}
					});

			builder6.setNegativeButton(R.string.btn_no,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();

						}
					});

			return builder6.create();
		case DLG_INTERNET_STATUS_CONNECTED:
			AlertDialog.Builder builder7 = new AlertDialog.Builder(this);
			builder7.setIcon(android.R.drawable.ic_dialog_info);
			builder7.setTitle(R.string.alert_title_internet_status);
			builder7.setView(mInflater.inflate(R.layout.connect_established,
					null));
			builder7.setNeutralButton(R.string.btn_ok,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// 
						}
					});
			return builder7.create();
		case DLG_INTERNET_STATUS_DISCONNECTED:
			AlertDialog.Builder builder8 = new AlertDialog.Builder(this);
			builder8.setIcon(android.R.drawable.ic_dialog_info);
			builder8.setTitle(R.string.alert_title_internet_status);

			builder8.setView(mInflater.inflate(R.layout.connect_no, null));

			builder8.setNeutralButton(R.string.btn_ok,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// 

						}
					});
			return builder8.create();
		default:
			break;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DLG_ERROR:
			AlertDialog alertDialog = (AlertDialog) dialog;
			switch (mLastError) {
			case LoadListError:
				alertDialog
						.setMessage(getString(R.string.alert_msg_loadlist_error));
				break;
			case DownloadError:
				alertDialog
						.setMessage(getString(R.string.alert_msg_download_error));
			default:
				break;
			}
			break;
		case DLG_CONFIRM_DELETE:
			AlertDialog alertDialog2 = (AlertDialog) dialog;
			alertDialog2
					.setMessage(getString(R.string.alert_msg_confirm_delete,
							mLongClickArticle.title));
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
					new String[] { DatabaseHelper.C_HASLRC, DatabaseHelper.C_HASTEXTZH, DatabaseHelper.C_TITLE, },
					new int[] { R.id.iv_lrc, R.id.iv_textzh, R.id.tv_title, });
			mSimpleCursorAdapter.setViewBinder(new ViewBinder() {

				public boolean setViewValue(View view, Cursor cursor,
						int columnIndex) {
					switch (view.getId()) {
					case R.id.iv_lrc:
						ImageView ivLrc = (ImageView) view;
						boolean haslrc = cursor.getInt(cursor
								.getColumnIndex(DatabaseHelper.C_HASLRC)) == 1;
						if (haslrc) 
							ivLrc.setImageResource(R.drawable.lrc);
						else 
							ivLrc.setImageResource(R.drawable.no);
						
						return true;
					case R.id.iv_textzh:
						ImageView ivTextzh = (ImageView) view;
						boolean hastextzh = cursor.getInt(cursor
								.getColumnIndex(DatabaseHelper.C_HASTEXTZH)) == 1;
						
						if (hastextzh)
							ivTextzh.setImageResource(R.drawable.textzh);
						else 
							ivTextzh.setImageResource(R.drawable.no);
						
						return true;
					case R.id.tv_title:
						TextView tvTitle = (TextView) view;
						
						String title = cursor.getString(cursor
								.getColumnIndex(DatabaseHelper.C_TITLE));
						String date = cursor.getString(cursor
								.getColumnIndex(DatabaseHelper.C_DATE));
						if (TextUtils.isEmpty(date))
							tvTitle.setText(title);
						else
							tvTitle.setText(String.format("%s (%s)", title,
									Utils.convertDateString(date)));
						return true;
					default:
						break;
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
		showDialog(DLG_PROGRESS);

		new Thread() {

			@Override
			public void run() {
				int typeIndex = spnTypeLocal.getSelectedItemPosition();
				int subtypeIndex = spnSubtypeLocal.getSelectedItemPosition();
				if (subtypeIndex == -1) {
					subtypeIndex = 0;
				}

				// Log.d(CLASSTAG, "typeIndex -- " + typeIndex);
				// Log.d(CLASSTAG, "subtypeIndex -- " + subtypeIndex);

				mCursor = mDatabaseHelper.queryArticles(TYPES_LOCAL[typeIndex],
						subtypeIndex == 0 ? null
								: SUBTYPES_LOCAL[typeIndex][subtypeIndex]);
				mLocalListHandler.sendEmptyMessage(WHAT_SUCCESS);
			}

		}.start();
	}

	private void refreshRemoteList() {
		showDialog(DLG_PROGRESS);
		new Thread() {

			@Override
			public void run() {
				int typeIndex = spnTypeRemote.getSelectedItemPosition();
				int subtypeIndex = spnSubtypeRemote.getSelectedItemPosition();

				if (subtypeIndex == -1) {
					subtypeIndex = 0;
				}

				// TODO change here to support i18n
				String key = TYPES_REMOTE[typeIndex] + "_"
						+ SUBTYPES_REMOTE[typeIndex][subtypeIndex];
				// Log.d(CLASSTAG, "key -- " + key);
				mApp.mListGenerator.mParser = mApp.mDataSource.getListParsers()
						.get(key);
				try {
					mList = mApp.mListGenerator.getArticleList(mApp.mDataSource
							.getListUrls().get(key));
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
		Intent intent = new Intent(Main.this, Show.class);
		Utils.putArticleToIntent(article, intent);
		startActivity(intent);
	}

	private void downloadArticleInService(Article article) {
		Intent intent = new Intent(this, DownloadService.class);
		Utils.putArticleToIntent(article, intent);
		startService(intent);
	}

	private void loadRemoteArticle(final Article article) {
		showDialog(DLG_PROGRESS);
		new Thread() {

			@Override
			public void run() {

				int typeIndex = spnTypeRemote.getSelectedItemPosition();
				int subtypeIndex = spnSubtypeRemote.getSelectedItemPosition();

				if (subtypeIndex == -1) {
					subtypeIndex = 0;
				}

				String key = TYPES_REMOTE[typeIndex] + "_"
						+ SUBTYPES_REMOTE[typeIndex][subtypeIndex];
				// Log.d(CLASSTAG, "key -- " + key);
				mApp.mPageGenerator.mParser = mApp.mDataSource.getPageParsers()
						.get(key);

				try {
					mApp.mPageGenerator.getArticle(article, false);
					mDownloadHandler.sendEmptyMessage(WHAT_SUCCESS);
				} catch (IOException e) {
					mDownloadHandler.sendEmptyMessage(WHAT_FAIL_IO);
				} catch (IllegalContentFormatException e) {
					mDownloadHandler.sendEmptyMessage(WHAT_FAIL_PARSE);
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

		Article article = mList.get(position);
		
		if (TextUtils.isEmpty(article.date))
			wraper.getTitle().setText(article.title);
		else
			wraper.getTitle().setText(
					String.format("%s (%s)", article.title, Utils
							.convertDateString(article.date)));
		
		if (article.haslrc) 
			wraper.getLrc().setImageResource(R.drawable.lrc);
		else 
			wraper.getLrc().setImageResource(R.drawable.no);
		
		if (article.hastextzh)
			wraper.getTextZh().setImageResource(R.drawable.textzh);
		else 
			wraper.getTextZh().setImageResource(R.drawable.no);

		return row;
	}

}

class ViewWraper {
	View root;
	TextView tvTitle;
	ImageView ivLrc;
	ImageView ivTextzh;

	public ViewWraper(View root) {
		this.root = root;
	}

	TextView getTitle() {
		if (tvTitle == null) {
			tvTitle = (TextView) root.findViewById(R.id.tv_title);
		}
		return tvTitle;
	}
	
	ImageView getLrc() {
		if (ivLrc == null) {
			ivLrc = (ImageView) root.findViewById(R.id.iv_lrc);
		}
		return ivLrc;
	}
	
	ImageView getTextZh() {
		if (ivTextzh == null) {
			ivTextzh = (ImageView) root.findViewById(R.id.iv_textzh);
		}
		return ivTextzh;
			
	}
}
