package cn.yo2.aquarium.pocketvoa.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.yo2.aquarium.pocketvoa.App;
import cn.yo2.aquarium.pocketvoa.Article;
import cn.yo2.aquarium.pocketvoa.IllegalContentFormatException;

public class PopularAmericanPageParser extends AbstractPageParser {
	private static final String CLASSTAG = PopularAmericanPageParser.class.getSimpleName();

	public void parse(Article article, String body)
			throws IllegalContentFormatException {
		int contentStart = body.indexOf("<div id=\"content\"");
		int listadsStart = body.indexOf("<div id=\"listads\"");

		String content = body.substring(contentStart, listadsStart);

		Pattern audioPattern = Pattern.compile("Player\\(\"([^\\s]+)\"\\)",
				Pattern.CASE_INSENSITIVE);

		Matcher audioMatcher = audioPattern.matcher(content);
		
		if (audioMatcher.find()) {
			article.mp3 = App.HOST + audioMatcher.group(1);
		}

		int textStart = 0;

		Pattern bylinePattern = Pattern.compile(
				"<div\\s+id=\"?menubar\"?\\s*>", Pattern.CASE_INSENSITIVE);

		Matcher bylineMatcher = bylinePattern.matcher(content);
		if (bylineMatcher.find()) {
			textStart = bylineMatcher.start();
		}

		String text = content.substring(textStart).replaceAll(
				"src=([\"\']?)(/[^\\s\'\">]+(?:\\.jpg|\\.png|\\.bmp|\\.gif))\\1?",
				"src=\"" + App.HOST + "$2\"");

		article.text = buildHtml(article.title, text);
	}

}
