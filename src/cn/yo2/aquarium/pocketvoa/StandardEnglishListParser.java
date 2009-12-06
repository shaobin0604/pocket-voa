package cn.yo2.aquarium.pocketvoa;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;
import android.util.Log;

public class StandardEnglishListParser implements
		ListParser {
	private static final String CLASSTAG = StandardEnglishListParser.class.getSimpleName();
	
	private String mType;
	private String mSubtype;
	
	public StandardEnglishListParser(String type, String subtype) {
		this.mType = type;
		this.mSubtype = subtype;
	}

	public ArrayList<Article> parse(String body) throws IllegalContentFormatException {
		if (TextUtils.isEmpty(body))
			throw new IllegalContentFormatException("The respone body is empty.");
		ArrayList<Article> list = new ArrayList<Article>();
		// int ulStart = body.indexOf("id=\"list\"");
		Pattern pattern = Pattern
				.compile("<a href=\"(/VOA_Standard_English/VOA_Standard_English_\\d+\\.html)\" target=_blank>([^<]+)</a>\\s*\\((\\d+-\\d+-\\d+)\\)");
		Matcher matcher = pattern.matcher(body);
		while (matcher.find()) {
			String url = matcher.group(1);
			String title = matcher.group(2);
			String date = matcher.group(3);
			
			Log.d(CLASSTAG, "url -- " + url + " title -- " + title + " date -- " + date);
			
			Article article = new Article();
			article.url = url;
			article.title = title;
			article.date = date;
			article.type = mType;
			article.subtype = mSubtype;
			list.add(article);
		}
		return list;
	}
}
