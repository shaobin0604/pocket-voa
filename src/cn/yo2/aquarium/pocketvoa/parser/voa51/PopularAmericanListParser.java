package cn.yo2.aquarium.pocketvoa.parser.voa51;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;
import android.util.Log;
import cn.yo2.aquarium.pocketvoa.Article;
import cn.yo2.aquarium.pocketvoa.IllegalContentFormatException;
import cn.yo2.aquarium.pocketvoa.parser.AbstractListParser;

public class PopularAmericanListParser extends AbstractListParser {
	private static final String CLASSTAG = PopularAmericanListParser.class.getSimpleName();


	public PopularAmericanListParser(String type, String subtype) {
		super(type, subtype);
	}
	
	
	
	public PopularAmericanListParser(String type, String subtype, int maxCount) {
		super(type, subtype, maxCount);
	}



	public ArrayList<Article> parse(String body)
			throws IllegalContentFormatException {
		if (TextUtils.isEmpty(body))
			throw new IllegalContentFormatException("The respone body is empty.");
		
		ArrayList<Article> list = new ArrayList<Article>();
		
		// int ulStart = body.indexOf("id=\"list\"");
		
		Pattern pattern = Pattern
				.compile("<a href=\"([^\\s]+)\" target=_blank>([^<]+)</a>");
		Matcher matcher = pattern.matcher(body);
		
		int count = 0;
		while (matcher.find() && count < this.mMaxCount) {
			count++;
			
			String url = matcher.group(1);
			String title = matcher.group(2);
			
			Log.d(CLASSTAG, "url -- " + url + " title -- " + title);
			
			Article article = new Article();
			
			article.id = -1;
			article.urltext = DataSource.HOST + (url.startsWith("/") ? url : "/" + url);
			article.title = title;
			article.type = this.mType;
			article.subtype = this.mSubtype;
			
			list.add(article);
		}
		return list;
	}
	

}
