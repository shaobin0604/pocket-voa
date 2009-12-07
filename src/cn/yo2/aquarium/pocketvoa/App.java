package cn.yo2.aquarium.pocketvoa;

import java.util.HashMap;

public class App {
	static Article article;

	static final String HOST = "http://www.51voa.com";
	static final String STANDARD_ENGLISH_LIST_URL = HOST
			+ "/VOA_Standard_1.html";
	static final String DEVELOPMENT_REPORT_LIST_URL = HOST
			+ "/Development_Report_1.html";
	static final String THIS_IS_AMERICA_LIST_URL = HOST
			+ "/This_is_America_1.html";

	static final HashMap<String, String> LIST_URLS = new HashMap<String, String>();

	static final Downloader DOWNLOADER = new Downloader();

	static final ListGenerator LIST_GENERATOR = new ListGenerator();

	static final PageGenerator PAGE_GENERATOR = new PageGenerator();

	// Article type_subtype ->
	static final HashMap<String, ListParser> LIST_PARSERS = new HashMap<String, ListParser>();

	// Article type_subtype ->
	static final HashMap<String, PageParser> PAGE_PARSERS = new HashMap<String, PageParser>();

	static {
		LIST_URLS.put("Standard English_Standard English",
				STANDARD_ENGLISH_LIST_URL);
		LIST_URLS.put("Special English_Development Report",
				DEVELOPMENT_REPORT_LIST_URL);
		LIST_URLS.put("Special English_This is America",
				THIS_IS_AMERICA_LIST_URL);

		LIST_PARSERS.put("Standard English_Standard English",
				new StandardEnglishListParser("Standard English",
						"Standard English"));
		
		LIST_PARSERS.put("Special English_Development Report",
				new StandardEnglishListParser("Special English",
						"Development Report"));
		
		LIST_PARSERS.put("Special English_This is America",
				new StandardEnglishListParser("Special English",
						"This is America"));
		
		PAGE_PARSERS.put("Standard English_Standard English",
				new StandardEnglishPageParser());
		PAGE_PARSERS.put("Special English_Development Report",
				new StandardEnglishPageParser());
		PAGE_PARSERS.put("Special English_This is America",
				new StandardEnglishPageParser());
	}
}
