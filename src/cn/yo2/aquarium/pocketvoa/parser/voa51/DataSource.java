package cn.yo2.aquarium.pocketvoa.parser.voa51;

import java.util.HashMap;

import cn.yo2.aquarium.pocketvoa.parser.IDataSource;
import cn.yo2.aquarium.pocketvoa.parser.IListParser;
import cn.yo2.aquarium.pocketvoa.parser.IPageParser;

public class DataSource implements IDataSource {
	static final String HOST = "http://www.51voa.com";

	// standard English
	static final String ENGLISH_NEWS = HOST + "/VOA_Standard_1.html";

	// special English
	static final String DEVELOPMENT_REPORT = HOST
			+ "/Development_Report_1.html";
	static final String THIS_IS_AMERICA = HOST + "/This_is_America_1.html";
	static final String AGRICULTURE_REPORT = HOST
			+ "/Agriculture_Report_1.html";
	static final String SCIENCE_IN_THE_NEWS = HOST
			+ "/Science_in_the_News_1.html";
	static final String HEALTH_REPORT = HOST + "/Health_Report_1.html";
	static final String EXPLORATIONS = HOST + "/Explorations_1.html";
	static final String EDUCATION_REPORT = HOST + "/Education_Report_1.html";
	static final String THE_MAKING_OF_A_NATION = HOST
			+ "/The_Making_of_a_Nation_1.html";
	static final String ECONOMICS_REPORT = HOST + "/Economics_Report_1.html";
	static final String AMERICAN_MOSAIC = HOST + "/American_Mosaic_1.html";
	static final String IN_THE_NEWS = HOST + "/In_the_News_1.html";
	static final String AMERICAN_STORIES = HOST + "/American_Stories_1.html";
	static final String WORDS_AND_THEIR_STORIES = HOST
			+ "/Words_And_Their_Stories_1.html";
	static final String PEOPLE_IN_AMERICA = HOST + "/People_in_America_1.html";

	// English learning
	static final String POPULAR_AMERICAN = HOST
			+ "/Popular_American_1.html";

	// Article type_subtype ->
	private final HashMap<String, String> mListUrls = new HashMap<String, String>();
	// Article type_subtype ->
	private final HashMap<String, IListParser> mListParsers = new HashMap<String, IListParser>();
	// Article type_subtype ->
	private final HashMap<String, IPageParser> mPageParsers = new HashMap<String, IPageParser>();
	// Article type_subtype ->
	private final HashMap<String, IPageParser> mPageZhParsers = new HashMap<String, IPageParser>();

	public void init(int maxCount) {
		setupListUrls();
		setupListParsers(maxCount);
		setupPageParsers();
		setupPageZhParsers();
	}

	public HashMap<String, IListParser> getListParsers() {
		return mListParsers;
	}

	public HashMap<String, String> getListUrls() {
		return mListUrls;
	}

	public HashMap<String, IPageParser> getPageParsers() {
		return mPageParsers;
	}
	
	public HashMap<String, IPageParser> getPageZhParsers() {
		return mPageZhParsers;
	}

	public String getName() {

		return "www.51voa.com";
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
						"English News", maxCount));
		// special English
		mListParsers.put("Special English_Development Report",
				new StandardEnglishListParser("Special English",
						"Development Report", maxCount));
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
						"Popular American", maxCount));
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
	
	private void setupPageZhParsers() {
		// standard English
		

		// special English
		mPageZhParsers.put("Special English_Development Report",
				new StandardEnglishPageZhParser());
		mPageZhParsers.put("Special English_This is America",
				new StandardEnglishPageZhParser());
		mPageZhParsers.put("Special English_Agriculture Report",
				new StandardEnglishPageZhParser());
		mPageZhParsers.put("Special English_Science in the News",
				new StandardEnglishPageZhParser());
		mPageZhParsers.put("Special English_Health Report",
				new StandardEnglishPageZhParser());
		mPageZhParsers.put("Special English_Explorations",
				new StandardEnglishPageZhParser());
		mPageZhParsers.put("Special English_Education Report",
				new StandardEnglishPageZhParser());
		mPageZhParsers.put("Special English_The Making of a Nation",
				new StandardEnglishPageZhParser());
		mPageZhParsers.put("Special English_Economics Report",
				new StandardEnglishPageZhParser());
		mPageZhParsers.put("Special English_American Mosaic",
				new StandardEnglishPageZhParser());
		mPageZhParsers.put("Special English_In the News",
				new StandardEnglishPageZhParser());
		mPageZhParsers.put("Special English_American Stories",
				new StandardEnglishPageZhParser());
		mPageZhParsers.put("Special English_Words And Their Stories",
				new StandardEnglishPageZhParser());
		mPageZhParsers.put("Special English_People in America",
				new StandardEnglishPageZhParser());
		
		// English learning
		
	}
}
