package cn.yo2.aquarium.pocketvoa.parser.iciba;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import cn.yo2.aquarium.pocketvoa.Article;
import cn.yo2.aquarium.pocketvoa.IllegalContentFormatException;
import cn.yo2.aquarium.pocketvoa.parser.AbstractPageParser;

public class StandardEnglishPageParser extends AbstractPageParser {
	private static final String CLASSTAG = StandardEnglishPageParser.class.getSimpleName();

	public void parse(Article article, String body) throws IllegalContentFormatException {
		int contentStart = body.indexOf("<div class=\"content\"");
		int listadsStart = body.indexOf("<div style=\"width:152px;margin:0 auto;clear:both;\">");
		Log.d(CLASSTAG, "contentStart -- " + contentStart);
		Log.d(CLASSTAG, "listadsStart -- " + listadsStart);
		String content = body.substring(contentStart, listadsStart);

		Pattern audioPattern = Pattern.compile("<a href=\"([^\\s]+\\.mp3)\">",
				Pattern.CASE_INSENSITIVE);

		Matcher audioMatcher = audioPattern.matcher(content);
		int textStart = 0;
		if (audioMatcher.find()) {
			textStart = audioMatcher.start();
			article.urlmp3 = audioMatcher.group(1);
		} else {
			throw new IllegalContentFormatException("Cannot find match");
		}

		String text = "<p>" + content.substring(textStart) + "</div>";

		article.text = buildHtml(article.title, text);
	}

	
}
