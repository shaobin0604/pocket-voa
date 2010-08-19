package cn.yo2.aquarium.pocketvoa.parser;

import java.util.HashMap;

public interface IDataSource {
	
	/*=========================================================================
	 * First category 
	 ========================================================================*/
	public static final String STANDARD_ENGLISH = "Standard English";
	public static final String SPECIAL_ENGLISH  = "Special English";
	public static final String ENGLISH_LEARNING = "English Learning";
	
	/*=========================================================================
	 * Second category 
	 ========================================================================*/
	/*
	 * Standard English
	 */
	public static final String ENGLISH_NEWS = "English News";
	/*
	 * Special English
	 */
	public static final String DEVELOPMENT_REPORT = "Development Report";
	public static final String THIS_IS_AMERICA = "This is America";
	public static final String AGRICULTURE_REPORT = "Agriculture Report";
	public static final String SCIENCE_IN_THE_NEWS = "Science in the News";
	public static final String HEALTH_REPORT = "Health Report";
	public static final String EXPLORATIONS = "Explorations";
	public static final String EDUCATION_REPORT = "Education Report";
	public static final String THE_MAKING_OF_A_NATION = "The Making of a Nation";
	public static final String ECONOMICS_REPORT = "Economics Report";
	public static final String AMERICAN_MOSAIC = "American Mosaic";
	public static final String IN_THE_NEWS = "In the News";
	public static final String AMERICAN_STORIES = "American Stories";
	public static final String WORDS_AND_THEIR_STORIES = "Words And Their Stories";
	public static final String PEOPLE_IN_AMERICA = "People in America";
	/*
	 * English Learning
	 */
	public static final String GO_ENGLISH = "Go English";
	public static final String WORD_MASTER = "Word Master";
	public static final String AMERICAN_CAFE = "American Cafe";
	public static final String POPULAR_AMERICAN = "Popular American";
	public static final String BUSINESS_ETIQUETTE = "Business Etiquette";
	public static final String SPORTS_ENGLISH = "Sports English";
	public static final String WORDS_AND_IDIOMS = "Words And Idioms";

		
	public void init(int maxCount);
	
	public HashMap<String, String> getListUrls();
	public HashMap<String, IListParser> getListParsers();
	public HashMap<String, IPageParser> getPageParsers();
	public HashMap<String, IPageParser> getPageZhParsers();
	
	public String getName();
}
