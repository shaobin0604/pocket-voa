package cn.yo2.aquarium.pocketvoa;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StandardEnglishPageParser extends AbstractPageParser {
	

	public void parse(Article article, String body) throws IllegalContentFormatException {
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
				"<span\\s+class=\"?byline\"?\\s*>", Pattern.CASE_INSENSITIVE);

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
