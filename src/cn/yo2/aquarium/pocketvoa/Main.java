package cn.yo2.aquarium.pocketvoa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Main extends Activity {
	private static final String CLASSTAG = Main.class.getSimpleName();

	private static final String HOST = "http://www.51voa.com";
	private static final String STANDARD_ENGLISH_LIST_URL = HOST
			+ "/VOA_Standard_1.html";
	private static final String DEVELOPMENT_REPORT_LIST_URL = HOST
			+ "/Development_Report_1.html";
	private static final String THIS_IS_AMERICA_LIST_URL = HOST
			+ "/This_is_America_1.html";

	private static final HashMap<String, String> LIST_URLS = new HashMap<String, String>();

	static {
		LIST_URLS.put("Standard English_Standard English",
				STANDARD_ENGLISH_LIST_URL);
		LIST_URLS.put("Special English_Development Report",
				DEVELOPMENT_REPORT_LIST_URL);
		LIST_URLS.put("Special English_This is America",
				THIS_IS_AMERICA_LIST_URL);
	}

	private static final int MENU_REMOTE = Menu.FIRST;
	private static final int MENU_LOCAL = Menu.FIRST + 1;

	private static final int PROGRESS_DIALOG = 1;

	protected static final int WHAT_SUCCESS = 0;
	protected static final int WHAT_FAIL_IO = 1;
	protected static final int WHAT_FAIL_PARSE = 2;

	private boolean mIsLocal;

	private String[] mTypes;
	private String[][] mSubtypes;

	private String mType;
	private String mSubtype;

	// Article type_subtype ->
	private HashMap<String, ListParser> mListParsers = new HashMap<String, ListParser>();

	// Article type_subtype ->
	private HashMap<String, PageParser> mPageParsers = new HashMap<String, PageParser>();

	private ArrayList<Article> mList;

	private Article mArticle;

	private ListGenerator mListGenerator = new ListGenerator();

	private PageGenerator mPageGenerator = new PageGenerator();

	private Handler mListHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_SUCCESS:
				bindList();
				dismissDialog(PROGRESS_DIALOG);
				break;
			case WHAT_FAIL_IO:
				Toast.makeText(Main.this, "FAIL IO", Toast.LENGTH_LONG);
				break;
			case WHAT_FAIL_PARSE:
				Toast.makeText(Main.this, "FAIL PARSE", Toast.LENGTH_LONG);
				break;
			default:
				break;
			}
		}

	};

	private Handler mPageHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_SUCCESS:
				dismissDialog(PROGRESS_DIALOG);
				Intent intent = new Intent(Main.this, Show.class);
				intent.putExtra(Article.K_TITLE, mArticle.title);
				intent.putExtra(Article.K_TEXT, mArticle.text);
				intent.putExtra(Article.K_URL, mArticle.url);
				intent.putExtra(Article.K_MP3, mArticle.mp3);
				intent.putExtra(Article.K_SUBTYPE, mArticle.subtype);
				intent.putExtra(Article.K_TYPE, mArticle.type);
				intent.putExtra(Article.K_DATE, mArticle.date);
				startActivity(intent);
				break;
			case WHAT_FAIL_IO:
				Toast.makeText(Main.this, "FAIL IO", Toast.LENGTH_LONG);
				break;
			case WHAT_FAIL_PARSE:
				Toast.makeText(Main.this, "FAIL PARSE", Toast.LENGTH_LONG);
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

		mListParsers.put("Standard English_Standard English",
				new StandardEnglishListParser("Standard English",
						"Standard English"));
		mPageParsers.put("Standard English_Standard English",
				new StandardEnglishPageParser());

		spnStandard = (Spinner) findViewById(R.id.spinner_standard);
		btnRefreshStandard = (Button) findViewById(R.id.btn_refresh_standard);
		tvStandard = (TextView) findViewById(R.id.empty_standard);
		lvStandard = (ListView) findViewById(R.id.list_standard);
		lvStandard.setEmptyView(tvStandard);

		lvStandard.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mArticle = mList.get(position);
				getPage();
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
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_REMOTE, Menu.NONE, "Remote").setEnabled(false);
		menu.add(Menu.NONE, MENU_LOCAL, Menu.NONE, "Local").setIntent(
				new Intent(this, Local.class));

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

	private void bindList() {
		RowAdapter adapter = new RowAdapter(this, mList);
		lvStandard.setAdapter(adapter);
	}

	private void getPage() {
		showDialog(PROGRESS_DIALOG);
		new Thread() {

			@Override
			public void run() {
				try {
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
					String key = mTypes[tabIndex] + "_" + mSubtypes[tabIndex][spinIndex];
					Log.d(CLASSTAG, "key -- " + key);
					mPageGenerator.mParser = mPageParsers.get(key);
					mPageGenerator.getArticle(HOST + mArticle.url, mArticle);
					mPageHandler.sendEmptyMessage(WHAT_SUCCESS);
				} catch (IOException e) {
					mPageHandler.sendEmptyMessage(WHAT_FAIL_IO);
				} catch (IllegalContentFormatException e) {
					mPageHandler.sendEmptyMessage(WHAT_FAIL_PARSE);
				}
			}

		}.start();
	}

	private void refreshList() {
		showDialog(PROGRESS_DIALOG);
		new Thread() {

			@Override
			public void run() {
				try {
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
					String key = mTypes[tabIndex] + "_" + mSubtypes[tabIndex][spinIndex];
					Log.d(CLASSTAG, "key -- " + key);
					mListGenerator.mParser = mListParsers.get(key);
					mList = mListGenerator.getArticleList(LIST_URLS.get(key));
					mListHandler.sendEmptyMessage(WHAT_SUCCESS);
				} catch (IOException e) {
					mListHandler.sendEmptyMessage(WHAT_FAIL_IO);
				} catch (IllegalContentFormatException e) {
					mListHandler.sendEmptyMessage(WHAT_FAIL_PARSE);
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
