package cn.yo2.aquarium.pocketvoa.parser.voa51;

import java.util.HashMap;

import cn.yo2.aquarium.pocketvoa.parser.IDataSource;
import cn.yo2.aquarium.pocketvoa.parser.IListParser;
import cn.yo2.aquarium.pocketvoa.parser.IPageParser;

public class Voa51DataSource implements IDataSource {
	private static final String SEPERATOR = "_";

	static final String HOST = "http://www.51voa.com";

	// standard English
	static final String URL_ENGLISH_NEWS            = HOST + "/VOA_Standard_%d.html";

	// special English
	static final String URL_DEVELOPMENT_REPORT      = HOST + "/Development_Report_%d.html";
	static final String URL_THIS_IS_AMERICA         = HOST + "/This_is_America_%d.html";
	static final String URL_AGRICULTURE_REPORT      = HOST + "/Agriculture_Report_%d.html";
	static final String URL_SCIENCE_IN_THE_NEWS     = HOST + "/Science_in_the_News_%d.html";
	static final String URL_HEALTH_REPORT           = HOST + "/Health_Report_%d.html";
	static final String URL_EXPLORATIONS            = HOST + "/Explorations_%d.html";
	static final String URL_EDUCATION_REPORT        = HOST + "/Education_Report_%d.html";
	static final String URL_THE_MAKING_OF_A_NATION  = HOST+ "/The_Making_of_a_Nation_%d.html";
	static final String URL_ECONOMICS_REPORT        = HOST + "/Economics_Report_%d.html";
	static final String URL_AMERICAN_MOSAIC         = HOST + "/American_Mosaic_%d.html";
	static final String URL_IN_THE_NEWS             = HOST + "/In_the_News_%d.html";
	static final String URL_AMERICAN_STORIES        = HOST + "/American_Stories_%d.html";
	static final String URL_WORDS_AND_THEIR_STORIES = HOST + "/Words_And_Their_Stories_%d.html";
	static final String URL_PEOPLE_IN_AMERICA       = HOST + "/People_in_America_%d.html";

	// English learning
	static final String URL_GO_ENGLISH              = HOST + "/Go_English_%d.html";
	static final String URL_WORD_MASTER             = HOST + "/Word_Master_%d.html";
	static final String URL_AMERICAN_CAFE           = HOST + "/American_Cafe_%d.html";
	static final String URL_POPULAR_AMERICAN        = HOST + "/Popular_American_%d.html";
	static final String URL_BUSINESS_ETIQUETTE      = HOST + "/Business_Etiquette_%d.html";
	static final String URL_SPORTS_ENGLISH          = HOST + "/Sports_English_%d.html";
	static final String URL_WORDS_AND_IDIOMS        = HOST + "/Words_And_Idioms_%d.html";
	

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
		mListUrls.put(STANDARD_ENGLISH + SEPERATOR + ENGLISH_NEWS, URL_ENGLISH_NEWS);

		// special English
		mListUrls.put(SPECIAL_ENGLISH + SEPERATOR + DEVELOPMENT_REPORT,      URL_DEVELOPMENT_REPORT);
		mListUrls.put(SPECIAL_ENGLISH + SEPERATOR + THIS_IS_AMERICA,         URL_THIS_IS_AMERICA);
		mListUrls.put(SPECIAL_ENGLISH + SEPERATOR + AGRICULTURE_REPORT,      URL_AGRICULTURE_REPORT);
		mListUrls.put(SPECIAL_ENGLISH + SEPERATOR + SCIENCE_IN_THE_NEWS,     URL_SCIENCE_IN_THE_NEWS);
		mListUrls.put(SPECIAL_ENGLISH + SEPERATOR + HEALTH_REPORT,           URL_HEALTH_REPORT);
		mListUrls.put(SPECIAL_ENGLISH + SEPERATOR + EXPLORATIONS,            URL_EXPLORATIONS);
		mListUrls.put(SPECIAL_ENGLISH + SEPERATOR + EDUCATION_REPORT,        URL_EDUCATION_REPORT);
		mListUrls.put(SPECIAL_ENGLISH + SEPERATOR + THE_MAKING_OF_A_NATION,  URL_THE_MAKING_OF_A_NATION);
		mListUrls.put(SPECIAL_ENGLISH + SEPERATOR + ECONOMICS_REPORT,        URL_ECONOMICS_REPORT);
		mListUrls.put(SPECIAL_ENGLISH + SEPERATOR + AMERICAN_MOSAIC,         URL_AMERICAN_MOSAIC);
		mListUrls.put(SPECIAL_ENGLISH + SEPERATOR + IN_THE_NEWS,             URL_IN_THE_NEWS);
		mListUrls.put(SPECIAL_ENGLISH + SEPERATOR + AMERICAN_STORIES,        URL_AMERICAN_STORIES);
		mListUrls.put(SPECIAL_ENGLISH + SEPERATOR + WORDS_AND_THEIR_STORIES, URL_WORDS_AND_THEIR_STORIES);
		mListUrls.put(SPECIAL_ENGLISH + SEPERATOR + PEOPLE_IN_AMERICA,       URL_PEOPLE_IN_AMERICA);

		// English learning
		mListUrls.put(ENGLISH_LEARNING + SEPERATOR + GO_ENGLISH,         URL_GO_ENGLISH);
		mListUrls.put(ENGLISH_LEARNING + SEPERATOR + WORD_MASTER,        URL_WORD_MASTER);
		mListUrls.put(ENGLISH_LEARNING + SEPERATOR + AMERICAN_CAFE,      URL_AMERICAN_CAFE);
		mListUrls.put(ENGLISH_LEARNING + SEPERATOR + POPULAR_AMERICAN,   URL_POPULAR_AMERICAN);
		mListUrls.put(ENGLISH_LEARNING + SEPERATOR + BUSINESS_ETIQUETTE, URL_BUSINESS_ETIQUETTE);
		mListUrls.put(ENGLISH_LEARNING + SEPERATOR + SPORTS_ENGLISH,     URL_SPORTS_ENGLISH);
		mListUrls.put(ENGLISH_LEARNING + SEPERATOR + WORDS_AND_IDIOMS,   URL_WORDS_AND_IDIOMS);
	}

	private void setupListParsers(int maxCount) {
		/*
		 *  standard English
		 */
		mListParsers.put(STANDARD_ENGLISH + SEPERATOR + ENGLISH_NEWS,
				new StandardEnglishListParser(STANDARD_ENGLISH, ENGLISH_NEWS));
		/*
		 *  special English
		 */
		mListParsers.put(SPECIAL_ENGLISH + SEPERATOR + DEVELOPMENT_REPORT,
				new StandardEnglishListParser(SPECIAL_ENGLISH, DEVELOPMENT_REPORT));
		mListParsers.put(SPECIAL_ENGLISH + SEPERATOR + THIS_IS_AMERICA,
				new StandardEnglishListParser(SPECIAL_ENGLISH, THIS_IS_AMERICA));
		mListParsers.put(SPECIAL_ENGLISH + SEPERATOR + AGRICULTURE_REPORT,
				new StandardEnglishListParser(SPECIAL_ENGLISH, AGRICULTURE_REPORT));
		mListParsers.put(SPECIAL_ENGLISH + SEPERATOR + SCIENCE_IN_THE_NEWS,
				new StandardEnglishListParser(SPECIAL_ENGLISH, SCIENCE_IN_THE_NEWS));
		mListParsers.put(SPECIAL_ENGLISH + SEPERATOR + HEALTH_REPORT,
				new StandardEnglishListParser(SPECIAL_ENGLISH, HEALTH_REPORT));
		mListParsers.put(SPECIAL_ENGLISH + SEPERATOR + EXPLORATIONS,
				new StandardEnglishListParser(SPECIAL_ENGLISH, EXPLORATIONS));
		mListParsers.put(SPECIAL_ENGLISH + SEPERATOR + EDUCATION_REPORT,
				new StandardEnglishListParser(SPECIAL_ENGLISH, EDUCATION_REPORT));
		mListParsers.put(SPECIAL_ENGLISH + SEPERATOR + THE_MAKING_OF_A_NATION,
				new StandardEnglishListParser(SPECIAL_ENGLISH, THE_MAKING_OF_A_NATION));
		mListParsers.put(SPECIAL_ENGLISH + SEPERATOR + ECONOMICS_REPORT,
				new StandardEnglishListParser(SPECIAL_ENGLISH, ECONOMICS_REPORT));
		mListParsers.put(SPECIAL_ENGLISH + SEPERATOR + AMERICAN_MOSAIC,
				new StandardEnglishListParser(SPECIAL_ENGLISH, AMERICAN_MOSAIC));
		mListParsers.put(SPECIAL_ENGLISH + SEPERATOR + IN_THE_NEWS,
				new StandardEnglishListParser(SPECIAL_ENGLISH, IN_THE_NEWS));
		mListParsers.put(SPECIAL_ENGLISH + SEPERATOR + AMERICAN_STORIES,
				new StandardEnglishListParser(SPECIAL_ENGLISH, AMERICAN_STORIES));
		mListParsers.put(SPECIAL_ENGLISH + SEPERATOR + WORDS_AND_THEIR_STORIES,
				new StandardEnglishListParser(SPECIAL_ENGLISH, WORDS_AND_THEIR_STORIES));
		mListParsers.put(SPECIAL_ENGLISH + SEPERATOR + PEOPLE_IN_AMERICA,
				new StandardEnglishListParser(SPECIAL_ENGLISH, PEOPLE_IN_AMERICA));
		/*
		 *  English learning
		 */
		mListParsers.put(ENGLISH_LEARNING + SEPERATOR + GO_ENGLISH, 
				new PopularAmericanListParser(ENGLISH_LEARNING, GO_ENGLISH));
		mListParsers.put(ENGLISH_LEARNING + SEPERATOR + WORD_MASTER, 
				new PopularAmericanListParser(ENGLISH_LEARNING, WORD_MASTER));
		mListParsers.put(ENGLISH_LEARNING + SEPERATOR + AMERICAN_CAFE, 
				new PopularAmericanListParser(ENGLISH_LEARNING, AMERICAN_CAFE));
		mListParsers.put(ENGLISH_LEARNING + SEPERATOR + POPULAR_AMERICAN,
				new PopularAmericanListParser(ENGLISH_LEARNING, POPULAR_AMERICAN));
		mListParsers.put(ENGLISH_LEARNING + SEPERATOR + BUSINESS_ETIQUETTE, 
				new PopularAmericanListParser(ENGLISH_LEARNING, BUSINESS_ETIQUETTE));
		mListParsers.put(ENGLISH_LEARNING + SEPERATOR + SPORTS_ENGLISH, 
				new PopularAmericanListParser(ENGLISH_LEARNING, SPORTS_ENGLISH));
		mListParsers.put(ENGLISH_LEARNING + SEPERATOR + WORDS_AND_IDIOMS, 
				new PopularAmericanListParser(ENGLISH_LEARNING, WORDS_AND_IDIOMS));
		
	}

	private void setupPageParsers() {
		/*
		 *  standard English
		 */
		mPageParsers.put(STANDARD_ENGLISH + SEPERATOR + ENGLISH_NEWS,
				new StandardEnglishPageParser());

		/*
		 *  special English
		 */
		mPageParsers.put(SPECIAL_ENGLISH + SEPERATOR + DEVELOPMENT_REPORT,
				new StandardEnglishPageParser());
		mPageParsers.put(SPECIAL_ENGLISH + SEPERATOR + THIS_IS_AMERICA,
				new StandardEnglishPageParser());
		mPageParsers.put(SPECIAL_ENGLISH + SEPERATOR + AGRICULTURE_REPORT,
				new StandardEnglishPageParser());
		mPageParsers.put(SPECIAL_ENGLISH + SEPERATOR + SCIENCE_IN_THE_NEWS,
				new StandardEnglishPageParser());
		mPageParsers.put(SPECIAL_ENGLISH + SEPERATOR + HEALTH_REPORT,
				new StandardEnglishPageParser());
		mPageParsers.put(SPECIAL_ENGLISH + SEPERATOR + EXPLORATIONS,
				new StandardEnglishPageParser());
		mPageParsers.put(SPECIAL_ENGLISH + SEPERATOR + EDUCATION_REPORT,
				new StandardEnglishPageParser());
		mPageParsers.put(SPECIAL_ENGLISH + SEPERATOR + THE_MAKING_OF_A_NATION,
				new StandardEnglishPageParser());
		mPageParsers.put(SPECIAL_ENGLISH + SEPERATOR + ECONOMICS_REPORT,
				new StandardEnglishPageParser());
		mPageParsers.put(SPECIAL_ENGLISH + SEPERATOR + AMERICAN_MOSAIC,
				new StandardEnglishPageParser());
		mPageParsers.put(SPECIAL_ENGLISH + SEPERATOR + IN_THE_NEWS,
				new StandardEnglishPageParser());
		mPageParsers.put(SPECIAL_ENGLISH + SEPERATOR + AMERICAN_STORIES,
				new StandardEnglishPageParser());
		mPageParsers.put(SPECIAL_ENGLISH + SEPERATOR + WORDS_AND_THEIR_STORIES,
				new StandardEnglishPageParser());
		mPageParsers.put(SPECIAL_ENGLISH + SEPERATOR + PEOPLE_IN_AMERICA,
				new StandardEnglishPageParser());
		
		/*
		 *  English learning
		 */
		mPageParsers.put(ENGLISH_LEARNING + SEPERATOR + GO_ENGLISH, 
				new PopularAmericanPageParser());
		mPageParsers.put(ENGLISH_LEARNING + SEPERATOR + WORD_MASTER, 
				new PopularAmericanPageParser());
		mPageParsers.put(ENGLISH_LEARNING + SEPERATOR + AMERICAN_CAFE, 
				new PopularAmericanPageParser());
		mPageParsers.put(ENGLISH_LEARNING + SEPERATOR + POPULAR_AMERICAN,
				new PopularAmericanPageParser());
		mPageParsers.put(ENGLISH_LEARNING + SEPERATOR + BUSINESS_ETIQUETTE,
				new PopularAmericanPageParser());
		mPageParsers.put(ENGLISH_LEARNING + SEPERATOR + SPORTS_ENGLISH,
				new PopularAmericanPageParser());
		mPageParsers.put(ENGLISH_LEARNING + SEPERATOR + WORDS_AND_IDIOMS,
				new PopularAmericanPageParser());
	}
	
	private void setupPageZhParsers() {
		// standard English
		
		
		/*=====================================================================
		 *  special English
		 ====================================================================*/
		mPageZhParsers.put(SPECIAL_ENGLISH + SEPERATOR + DEVELOPMENT_REPORT,
				new StandardEnglishPageZhParser());
		mPageZhParsers.put(SPECIAL_ENGLISH + SEPERATOR + THIS_IS_AMERICA,
				new StandardEnglishPageZhParser());
		mPageZhParsers.put(SPECIAL_ENGLISH + SEPERATOR + AGRICULTURE_REPORT,
				new StandardEnglishPageZhParser());
		mPageZhParsers.put(SPECIAL_ENGLISH + SEPERATOR + SCIENCE_IN_THE_NEWS,
				new StandardEnglishPageZhParser());
		mPageZhParsers.put(SPECIAL_ENGLISH + SEPERATOR + HEALTH_REPORT,
				new StandardEnglishPageZhParser());
		mPageZhParsers.put(SPECIAL_ENGLISH + SEPERATOR + EXPLORATIONS,
				new StandardEnglishPageZhParser());
		mPageZhParsers.put(SPECIAL_ENGLISH + SEPERATOR + EDUCATION_REPORT,
				new StandardEnglishPageZhParser());
		mPageZhParsers.put(SPECIAL_ENGLISH + SEPERATOR + THE_MAKING_OF_A_NATION,
				new StandardEnglishPageZhParser());
		mPageZhParsers.put(SPECIAL_ENGLISH + SEPERATOR + ECONOMICS_REPORT,
				new StandardEnglishPageZhParser());
		mPageZhParsers.put(SPECIAL_ENGLISH + SEPERATOR + AMERICAN_MOSAIC,
				new StandardEnglishPageZhParser());
		mPageZhParsers.put(SPECIAL_ENGLISH + SEPERATOR + IN_THE_NEWS,
				new StandardEnglishPageZhParser());
		mPageZhParsers.put(SPECIAL_ENGLISH + SEPERATOR + AMERICAN_STORIES,
				new StandardEnglishPageZhParser());
		mPageZhParsers.put(SPECIAL_ENGLISH + SEPERATOR + WORDS_AND_THEIR_STORIES,
				new StandardEnglishPageZhParser());
		mPageZhParsers.put(SPECIAL_ENGLISH + SEPERATOR + PEOPLE_IN_AMERICA,
				new StandardEnglishPageZhParser());
		
		// English learning
		
	}
}
