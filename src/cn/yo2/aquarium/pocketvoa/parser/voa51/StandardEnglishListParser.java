package cn.yo2.aquarium.pocketvoa.parser.voa51;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;
import android.util.Log;
import cn.yo2.aquarium.pocketvoa.Article;
import cn.yo2.aquarium.pocketvoa.IllegalContentFormatException;
import cn.yo2.aquarium.pocketvoa.Utils;
import cn.yo2.aquarium.pocketvoa.parser.AbstractListParser;

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
			throw new IllegalContentFormatException("The response body is empty.");
		ArrayList<Article> list = new ArrayList<Article>();
		// int ulStart = body.indexOf("id=\"list\"");
		Pattern pattern = Pattern
				.compile("<li>\\s*(<img src=/images/lrc\\.gif>)?\\s*(<img src=/images/yi\\.gif>)?\\s*<a href=\"([^\\s]+)\" target=_blank>([^<]+)</a>\\s*\\((\\d+-\\d+-\\d+)\\)\\s*</li>");
		Matcher matcher = pattern.matcher(body);
		int count = 0;
		int maxCount = this.mMaxCount;
		while (matcher.find() && count < maxCount) {
			
			count++;
			
			String haslrc = matcher.group(1);
			String hastextzh = matcher.group(2);
			String urltext = matcher.group(3);
			String title = matcher.group(4);
			String date = matcher.group(5);
			
			Log.d(CLASSTAG, "haslrc -- " + haslrc + " hastextzh -- " + hastextzh + " urltext -- " + urltext + " title -- " + title + " date -- " + date);
			
			Article article = new Article();
			
			article.id = -1;
			article.urltext = DataSource.HOST + (urltext.startsWith("/") ? urltext : "/" + urltext);
			article.title = title;
			article.date = Utils.formatDateString(date);
			article.type = mType;
			article.subtype = mSubtype;
			article.haslrc = (null != haslrc);
			article.hastextzh = (null != hastextzh);
				
			list.add(article);
		}
		return list;
	}
}
