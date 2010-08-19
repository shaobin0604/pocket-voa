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

public class PopularAmericanListParser extends AbstractListParser {
	private static final String CLASSTAG = PopularAmericanListParser.class.getSimpleName();


	public PopularAmericanListParser(String type, String subtype) {
		super(type, subtype);
	}

	public ArrayList<Article> parse(String body)
			throws IllegalContentFormatException {
		if (TextUtils.isEmpty(body))
			throw new IllegalContentFormatException("The respone body is empty.");
		
		ArrayList<Article> list = new ArrayList<Article>();
		
		// int ulStart = body.indexOf("id=\"list\"");
		
		Pattern pattern = Pattern
				.compile("<li>\\s*<a href=\"([^\\s]+)\" target=_blank>([^<]+)</a>\\s*(?:\\((\\d+-\\d+-\\d+)\\)){0,1}\\s*</li>");
		Matcher matcher = pattern.matcher(body);
		
		while (matcher.find()) {
			
			String url = matcher.group(1);
			String title = matcher.group(2);
			String date = matcher.group(3);
			
			Log.d(CLASSTAG, "url -- " + url + " title -- " + title + " date -- " + date);
			
			Article article = new Article();
			
			article.id = -1;
			article.urltext = Voa51DataSource.HOST + (url.startsWith("/") ? url : "/" + url);
			article.title = title;
			article.type = mType;
			article.subtype = mSubtype;
			article.haslrc = false;
			article.hastextzh = false;
			
			if (date != null)
				article.date = Utils.formatDateString(date);
			
			list.add(article);
		}
		return list;
	}
	

}
