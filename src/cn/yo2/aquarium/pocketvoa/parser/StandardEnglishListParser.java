package cn.yo2.aquarium.pocketvoa.parser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;
import android.util.Log;
import cn.yo2.aquarium.pocketvoa.App;
import cn.yo2.aquarium.pocketvoa.Article;
import cn.yo2.aquarium.pocketvoa.IllegalContentFormatException;
import cn.yo2.aquarium.pocketvoa.Utils;

public class StandardEnglishListParser extends AbstractListParser {
	private static final String CLASSTAG = StandardEnglishListParser.class.getSimpleName();
	
	public StandardEnglishListParser(String type, String subtype) {
		super(type, subtype);
	}

	public StandardEnglishListParser(String type, String subtype, int maxCount) {
		super(type, subtype, maxCount);
	}

	public ArrayList<Article> parse(String body) throws IllegalContentFormatException {
		if (TextUtils.isEmpty(body))
			throw new IllegalContentFormatException("The respone body is empty.");
		ArrayList<Article> list = new ArrayList<Article>();
		// int ulStart = body.indexOf("id=\"list\"");
		Pattern pattern = Pattern
				.compile("<a href=\"([^\\s]+)\" target=_blank>([^<]+)</a>\\s*\\((\\d+-\\d+-\\d+)\\)");
		Matcher matcher = pattern.matcher(body);
		int count = 0;
		while (matcher.find() && count < this.mMaxCount) {
			
			count++;
			
			String url = matcher.group(1);
			String title = matcher.group(2);
			String date = matcher.group(3);
			
			Log.d(CLASSTAG, "url -- " + url + " title -- " + title + " date -- " + date);
			
			Article article = new Article();
			article.url = App.HOST + (url.startsWith("/") ? url : "/" + url);
			article.title = title;
			article.date = Utils.formatDateString(date);
			article.type = mType;
			article.subtype = mSubtype;
			list.add(article);
		}
		return list;
	}
}
