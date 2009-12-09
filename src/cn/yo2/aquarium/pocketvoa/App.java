package cn.yo2.aquarium.pocketvoa;

import java.util.HashMap;
import java.util.Iterator;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;
import cn.yo2.aquarium.pocketvoa.parser.IListParser;
import cn.yo2.aquarium.pocketvoa.parser.IPageParser;
import cn.yo2.aquarium.pocketvoa.parser.PopularAmericanListParser;
import cn.yo2.aquarium.pocketvoa.parser.PopularAmericanPageParser;
import cn.yo2.aquarium.pocketvoa.parser.StandardEnglishListParser;
import cn.yo2.aquarium.pocketvoa.parser.StandardEnglishPageParser;

public class App extends Application {
	private static final String CLASSTAG = App.class.getSimpleName();

	private SharedPreferences mSharedPreferences;

	private OnSharedPreferenceChangeListener mSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {

		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {

			if (getString(R.string.prefs_list_count_key).equals(key)) {

				int maxCount = getMaxCountFromPrefs();
				Log.d(CLASSTAG, "max count: " + maxCount);
				for (Iterator<IListParser> i = mListParsers.values().iterator(); i
						.hasNext();) {
					i.next().setMaxCount(maxCount);
				}
			}
		}
	};

	private void setupPreferenceChangeListener() {
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		mSharedPreferences
				.registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		setupPreferenceChangeListener();

		setupListUrls();

		setupListParsers();

		setupPageParsers();
	}

	public Article article;

	public static final String HOST = "http://www.51voa.com";

	// standard english
	public static final String STANDARD_ENGLISH_LIST_URL = HOST
			+ "/VOA_Standard_1.html";

	// special english
	public static final String DEVELOPMENT_REPORT_LIST_URL = HOST
			+ "/Development_Report_1.html";
	public static final String THIS_IS_AMERICA_LIST_URL = HOST
			+ "/This_is_America_1.html";

	// english learning
	public static final String POPULAR_AMERICAN_LIST_URL = HOST
			+ "/Popular_American_1.html";

	public final HashMap<String, String> mListUrls = new HashMap<String, String>();

	public final Downloader mDownloader = new Downloader();

	public final ListGenerator mListGenerator = new ListGenerator();

	public final PageGenerator mPageGenerator = new PageGenerator();

	// Article type_subtype ->
	public final HashMap<String, IListParser> mListParsers = new HashMap<String, IListParser>();

	// Article type_subtype ->
	public final HashMap<String, IPageParser> mPageParsers = new HashMap<String, IPageParser>();

	private void setupListUrls() {
		// standard english
		mListUrls.put("Standard English_Standard English",
				STANDARD_ENGLISH_LIST_URL);

		// special english
		mListUrls.put("Special English_Development Report",
				DEVELOPMENT_REPORT_LIST_URL);
		mListUrls.put("Special English_This is America",
				THIS_IS_AMERICA_LIST_URL);

		// english learning
		mListUrls.put("English Learning_Popular American",
				POPULAR_AMERICAN_LIST_URL);
	}

	private void setupListParsers() {
		int maxCount = getMaxCountFromPrefs();

		mListParsers.put("Standard English_Standard English",
				new StandardEnglishListParser("Standard English",
						"Standard English", maxCount));

		mListParsers.put("Special English_Development Report",
				new StandardEnglishListParser("Special English",
						"Development Report", maxCount));
		mListParsers.put("Special English_This is America",
				new StandardEnglishListParser("Special English",
						"This is America"));

		mListParsers.put("English Learning_Popular American",
				new PopularAmericanListParser("English Learning",
						"Popular American", maxCount));
	}

	private Integer getMaxCountFromPrefs() {
		return Integer.valueOf(mSharedPreferences.getString(
				getString(R.string.prefs_list_count_key), String
						.valueOf(IListParser.DEFAULT_MAX_COUNT)));
	}

	private void setupPageParsers() {
		mPageParsers.put("Standard English_Standard English",
				new StandardEnglishPageParser());

		mPageParsers.put("Special English_Development Report",
				new StandardEnglishPageParser());
		mPageParsers.put("Special English_This is America",
				new StandardEnglishPageParser());

		mPageParsers.put("English Learning_Popular American",
				new PopularAmericanPageParser());
	}
}
