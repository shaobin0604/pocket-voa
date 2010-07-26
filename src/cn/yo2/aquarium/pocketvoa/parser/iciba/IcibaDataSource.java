package cn.yo2.aquarium.pocketvoa.parser.iciba;

import java.util.HashMap;

import cn.yo2.aquarium.pocketvoa.parser.IDataSource;
import cn.yo2.aquarium.pocketvoa.parser.IListParser;
import cn.yo2.aquarium.pocketvoa.parser.IPageParser;


public class IcibaDataSource implements IDataSource {

	static final String HOST = "http://news.iciba.com";

	// standard English
	static final String ENGLISH_NEWS = HOST + "/1598/index_1.html";

	// special English
	static final String DEVELOPMENT_REPORT = HOST
			+ "/1575/index_1.html";
	static final String THIS_IS_AMERICA = HOST + "/1574/index_1.html";
	static final String AGRICULTURE_REPORT = HOST
			+ "/1573/index_1.html";
	static final String SCIENCE_IN_THE_NEWS = HOST
			+ "/1572/index_1.html";
	static final String HEALTH_REPORT = HOST + "/1571/index_1.html";
	static final String EXPLORATIONS = HOST + "/1576/index_1.html";
	static final String EDUCATION_REPORT = HOST + "/1577/index_1.html";
	static final String THE_MAKING_OF_A_NATION = HOST
			+ "/1584/index_1.html";
	static final String ECONOMICS_REPORT = HOST + "/1578/index_1.html";
	static final String AMERICAN_MOSAIC = HOST + "/1581/index_1.html";
	static final String IN_THE_NEWS = HOST + "/1583/index_1.html";
	static final String AMERICAN_STORIES = HOST + "/1582/index_1.html";
	static final String WORDS_AND_THEIR_STORIES = HOST
			+ "/1580/index_1.html";
	static final String PEOPLE_IN_AMERICA = HOST + "/1579/index_1.html";

	// English learning
	static final String POPULAR_AMERICAN = HOST + "/1603/index_1.html";

	public String getName() {
		return "www.iciba.com";
	}

	// Article type_subtype ->
	public final HashMap<String, String> mListUrls = new HashMap<String, String>();
	// Article type_subtype ->
	public final HashMap<String, IListParser> mListParsers = new HashMap<String, IListParser>();
	// Article type_subtype ->
	public final HashMap<String, IPageParser> mPageParsers = new HashMap<String, IPageParser>();

	public HashMap<String, IListParser> getListParsers() {
		return mListParsers;
	}

	public HashMap<String, String> getListUrls() {
		return mListUrls;
	}

	public HashMap<String, IPageParser> getPageParsers() {
		return mPageParsers;
	}

	public void init(int maxCount) {
		setupListUrls();
		setupListParsers(maxCount);
		setupPageParsers();
	}

	private void setupListUrls() {
		// standard English
		mListUrls.put("Standard English_English News", ENGLISH_NEWS);

		// special English
		mListUrls.put("Special English_Development Report", DEVELOPMENT_REPORT);
		mListUrls.put("Special English_This is America", THIS_IS_AMERICA);
		mListUrls.put("Special English_Agriculture Report", AGRICULTURE_REPORT);
		mListUrls.put("Special English_Science in the News",
				SCIENCE_IN_THE_NEWS);
		mListUrls.put("Special English_Health Report", HEALTH_REPORT);
		mListUrls.put("Special English_Explorations", EXPLORATIONS);
		mListUrls.put("Special English_Education Report", EDUCATION_REPORT);
		mListUrls.put("Special English_The Making of a Nation",
				THE_MAKING_OF_A_NATION);
		mListUrls.put("Special English_Economics Report", ECONOMICS_REPORT);
		mListUrls.put("Special English_American Mosaic", AMERICAN_MOSAIC);
		mListUrls.put("Special English_In the News", IN_THE_NEWS);
		mListUrls.put("Special English_American Stories", AMERICAN_STORIES);
		mListUrls.put("Special English_Words And Their Stories",
				WORDS_AND_THEIR_STORIES);
		mListUrls.put("Special English_People in America", PEOPLE_IN_AMERICA);

		// English learning
		mListUrls.put("English Learning_Popular American",
				POPULAR_AMERICAN);
	}

	private void setupListParsers(int maxCount) {
		// standard English
		mListParsers.put("Standard English_English News",
				new StandardEnglishListParser("Standard English",
						"English News"));
		// special English
		mListParsers.put("Special English_Development Report",
				new StandardEnglishListParser("Special English",
						"Development Report"));
		mListParsers.put("Special English_This is America",
				new StandardEnglishListParser("Special English",
						"This is America"));
		mListParsers.put("Special English_Agriculture Report",
				new StandardEnglishListParser("Special English",
						"Agriculture Report"));
		mListParsers.put("Special English_Science in the News",
				new StandardEnglishListParser("Special English",
						"Science in the News"));
		mListParsers.put("Special English_Health Report",
				new StandardEnglishListParser("Special English",
						"Health Report"));
		mListParsers.put("Special English_Explorations",
				new StandardEnglishListParser("Special English",
						"Explorations"));
		mListParsers.put("Special English_Education Report",
				new StandardEnglishListParser("Special English",
						"Education Report"));
		mListParsers.put("Special English_The Making of a Nation",
				new StandardEnglishListParser("Special English",
						"The Making of a Nation"));
		mListParsers.put("Special English_Economics Report",
				new StandardEnglishListParser("Special English",
						"Economics Report"));
		mListParsers.put("Special English_American Mosaic",
				new StandardEnglishListParser("Special English",
						"American Mosaic"));
		mListParsers.put("Special English_In the News",
				new StandardEnglishListParser("Special English",
						"In the News"));
		mListParsers.put("Special English_American Stories",
				new StandardEnglishListParser("Special English",
						"American Stories"));
		mListParsers.put("Special English_Words And Their Stories",
				new StandardEnglishListParser("Special English",
						"Words And Their Stories"));
		mListParsers.put("Special English_People in America",
				new StandardEnglishListParser("Special English",
						"People in America"));
		// English learning
		mListParsers.put("English Learning_Popular American",
				new PopularAmericanListParser("English Learning",
						"Popular American"));
	}

	private void setupPageParsers() {
		// standard English
		mPageParsers.put("Standard English_English News",
				new StandardEnglishPageParser());

		// special English
		mPageParsers.put("Special English_Development Report",
				new StandardEnglishPageParser());
		mPageParsers.put("Special English_This is America",
				new StandardEnglishPageParser());
		mPageParsers.put("Special English_Agriculture Report",
				new StandardEnglishPageParser());
		mPageParsers.put("Special English_Science in the News",
				new StandardEnglishPageParser());
		mPageParsers.put("Special English_Health Report",
				new StandardEnglishPageParser());
		mPageParsers.put("Special English_Explorations",
				new StandardEnglishPageParser());
		mPageParsers.put("Special English_Education Report",
				new StandardEnglishPageParser());
		mPageParsers.put("Special English_The Making of a Nation",
				new StandardEnglishPageParser());
		mPageParsers.put("Special English_Economics Report",
				new StandardEnglishPageParser());
		mPageParsers.put("Special English_American Mosaic",
				new StandardEnglishPageParser());
		mPageParsers.put("Special English_In the News",
				new StandardEnglishPageParser());
		mPageParsers.put("Special English_American Stories",
				new StandardEnglishPageParser());
		mPageParsers.put("Special English_Words And Their Stories",
				new StandardEnglishPageParser());
		mPageParsers.put("Special English_People in America",
				new StandardEnglishPageParser());
		
		// English learning
		mPageParsers.put("English Learning_Popular American",
				new PopularAmericanPageParser());
	}

	public HashMap<String, IPageParser> getPageZhParsers() {
		// TODO Auto-generated method stub
		return null;
	}
}
