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

public class Main extends Activity {
	private static final String CLASSTAG = Main.class.getSimpleName();

	private static final int MENU_SETTINGS = Menu.FIRST;
	private static final int MENU_ABOUT = Menu.FIRST + 1;
	private static final int MENU_EXIT = Menu.FIRST + 2;

	private static final int DLG_ERROR_ALERT = 0;
	private static final int DLG_PROGRESS = 1;
	private static final int DLG_LOCAL_LIST_ACTION = 2;
	private static final int DLG_REMOTE_LIST_ACTION = 3;
	private static final int DLG_DELETE_CONFIRM = 4;
	private static final int DLG_ABOUT = 5;

	private static final int WHAT_SUCCESS = 0;
	private static final int WHAT_FAIL_IO = 1;
	private static final int WHAT_FAIL_PARSE = 2;

	private enum Error {
		LoadListError,
	}

	private Error mLastError;

	private App mApp;

	private Article mLongClickArticle;

	private String[] mTypesRemote;
	private String[][] mSubtypesRemote;
	private ArrayAdapter<CharSequence>[] mAdaptersRemote;

	private String[] mTypesLocal;
	private String[][] mSubtypesLocal;
	private ArrayAdapter<CharSequence>[] mAdaptersLocal;

	private ArrayList<Article> mList;

	private Cursor mCursor;
	private SimpleCursorAdapter mSimpleCursorAdapter;
	private DatabaseHelper mDatabaseHelper;

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
				showDialog(DLG_ERROR_ALERT);
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
				showDialog(DLG_ERROR_ALERT);
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

	private void setupRemoteTabWidgets() {
		spnTypeRemote = (Spinner) findViewById(R.id.spinner_type_remote);
		spnTypeRemote.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				spnSubtypeRemote.setAdapter(mAdaptersRemote[position]);
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
				showDialog(DLG_REMOTE_LIST_ACTION);
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
				showDialog(DLG_LOCAL_LIST_ACTION);
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
		mTypesRemote = getResources().getStringArray(R.array.type);

		mSubtypesRemote = new String[][] {
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

		mAdaptersRemote = new ArrayAdapter[] { stAdapter, spAdapter, elAdapter, };

	}

	private void extractTypesLocal() {
		// local types: add all option
		mTypesLocal = getResources().getStringArray(R.array.type_local);

		mSubtypesLocal = new String[][] {
				getResources().getStringArray(R.array.standard_english_local),
				getResources().getStringArray(R.array.special_english_local),
				getResources().getStringArray(R.array.english_learning_local), };

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
		case MENU_ABOUT:
			showDialog(DLG_ABOUT);
			return true;
		case MENU_EXIT:
			finish();
			return true;

		default:
			break;
		}
		return result;
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
		case DLG_ERROR_ALERT:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setTitle(R.string.alert_title_error);
			// without this statement, you would not be able to change
			// AlertDialog's message in onPreparedDialog
			builder.setMessage("");
			builder.setNeutralButton(R.string.btn_ok,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub

						}
					});
			return builder.create();

		case DLG_PROGRESS:
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog
					.setMessage(getString(R.string.progressspin_loadlist_msg));
			return progressDialog;
		case DLG_LOCAL_LIST_ACTION:
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
								showDialog(DLG_DELETE_CONFIRM);
								break;
							default:
								break;
							}

						}
					});
			return builder2.create();
		case DLG_REMOTE_LIST_ACTION:
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
										Toast.LENGTH_SHORT).show();
								break;
							default:
								break;
							}

						}
					});
			return builder3.create();
		case DLG_DELETE_CONFIRM:
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
									Toast.LENGTH_SHORT).show();
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
			LayoutInflater inflater = LayoutInflater.from(this);
			View layout = inflater.inflate(R.layout.about,
					(ViewGroup) findViewById(R.id.root_about));
			builder5.setView(layout);
			builder5.setTitle(R.string.alert_title_about);
			builder5.setNeutralButton(R.string.btn_ok,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub

						}
					});
			return builder5.create();
		default:
			break;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DLG_ERROR_ALERT:
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
		case DLG_DELETE_CONFIRM:
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
		showDialog(DLG_PROGRESS);

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

				mCursor = mDatabaseHelper.queryArticles(mTypesLocal[typeIndex],
						subtypeIndex == 0 ? null
								: mSubtypesLocal[typeIndex][subtypeIndex]);
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

				String key = mTypesRemote[typeIndex] + "_"
						+ mSubtypesRemote[typeIndex][subtypeIndex];
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
