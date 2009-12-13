package cn.yo2.aquarium.pocketvoa.parser.voa51;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.yo2.aquarium.pocketvoa.Article;
import cn.yo2.aquarium.pocketvoa.IllegalContentFormatException;
import cn.yo2.aquarium.pocketvoa.parser.AbstractPageParser;

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
			article.mp3 = Constant.HOST + audioMatcher.group(1);
		} else {
			throw new IllegalContentFormatException("Cannot find match mp3");
		}

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
				"src=\"" + Constant.HOST + "$2\"");

		article.text = buildHtml(article.title, text);
	}

}
