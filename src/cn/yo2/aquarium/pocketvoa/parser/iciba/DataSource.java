package cn.yo2.aquarium.pocketvoa.parser.iciba;

import java.util.HashMap;

import cn.yo2.aquarium.pocketvoa.parser.IDataSource;
import cn.yo2.aquarium.pocketvoa.parser.IListParser;
import cn.yo2.aquarium.pocketvoa.parser.IPageParser;

public class DataSource implements IDataSource {
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
		// standard english
		mListUrls.put("Standard English_Standard English",
				Constant.STANDARD_ENGLISH_LIST_URL);

		// special english
		mListUrls.put("Special English_Development Report",
				Constant.DEVELOPMENT_REPORT_LIST_URL);
		mListUrls.put("Special English_This is America",
				Constant.THIS_IS_AMERICA_LIST_URL);

		// english learning
		mListUrls.put("English Learning_Popular American",
				Constant.POPULAR_AMERICAN_LIST_URL);
	}

	private void setupListParsers(int maxCount) {

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
