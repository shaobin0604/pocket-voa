package cn.yo2.aquarium.pocketvoa.parser.voa51;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import cn.yo2.aquarium.pocketvoa.Article;
import cn.yo2.aquarium.pocketvoa.IllegalContentFormatException;
import cn.yo2.aquarium.pocketvoa.parser.AbstractPageParser;

public class PopularAmericanPageParser extends AbstractPageParser {
	private static final String CLASSTAG = PopularAmericanPageParser.class.getSimpleName();

	public void parse(Article article, String body)
			throws IllegalContentFormatException {
		int contentStart = body.indexOf("<div id=\"content\"");
		int listadsStart = body.indexOf("<div id=\"listads\"");
		if (listadsStart < 0)
			listadsStart = body.indexOf("<div id=\"Bottom_Import\"");
		if (listadsStart < 0)
			listadsStart = body.indexOf("<div id=\"Bottom_VOA\"");
		

		if (contentStart < 0 || listadsStart < 0)
			throw new IllegalContentFormatException("Cannot find content");
		
		String content = body.substring(contentStart, listadsStart);

//		Pattern audioPattern = Pattern.compile("Player\\(\"([^\\s]+)\"\\)",
//				Pattern.CASE_INSENSITIVE);
		
		Pattern audioPattern = Pattern.compile("href=\"?([^\\s]+\\.mp3)\"?",
				Pattern.CASE_INSENSITIVE);

		Matcher audioMatcher = audioPattern.matcher(content);
		
		if (audioMatcher.find()) {
			String urlmp3 = audioMatcher.group(1);
			if (urlmp3.startsWith("http://")) {
				article.urlmp3 = urlmp3;
			} else {
				article.urlmp3 = Voa51DataSource.HOST + urlmp3;
			}
		} else {
			throw new IllegalContentFormatException("Cannot find match mp3");
		}
		
		Log.d(CLASSTAG, "parse() article.urlmp3 = " + article.urlmp3);

		int textStart = 0;

		Pattern bylinePattern = Pattern.compile(
				"<div\\s+id=\"?menubar\"?\\s*>", Pattern.CASE_INSENSITIVE);

		Matcher bylineMatcher = bylinePattern.matcher(content);
		if (bylineMatcher.find()) {
			textStart = bylineMatcher.start();
		} else {
			throw new IllegalContentFormatException("Cannot find match text");
		}

		String text = content.substring(textStart).replaceAll(
				"src=([\"\']?)(/[^\\s\'\">]+(?:\\.jpg|\\.png|\\.bmp|\\.gif))\\1?",
				"src=\"" + Voa51DataSource.HOST + "$2\"");

		article.text = buildHtml(article.title, text);
	}

}
