package cn.yo2.aquarium.pocketvoa.parser.voa51;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import cn.yo2.aquarium.pocketvoa.Article;
import cn.yo2.aquarium.pocketvoa.IllegalContentFormatException;
import cn.yo2.aquarium.pocketvoa.parser.AbstractPageParser;

public class StandardEnglishPageParser extends AbstractPageParser {
	private static final String CLASSTAG = StandardEnglishPageParser.class.getSimpleName();
	

	public void parse(Article article, String body) throws IllegalContentFormatException {
		int menubarStart = body.indexOf("<div id=\"menubar\"");
		
		int listadsStart = body.indexOf("<div id=\"listads\"");
		if (listadsStart < 0)
			listadsStart = body.indexOf("<div id=\"Bottom_VOA\"");
		if (listadsStart < 0)
			listadsStart = body.indexOf("<div id=\"Bottom_Import\"");
		
		if (menubarStart < 0 || listadsStart < 0)
			throw new IllegalContentFormatException("Cannot find content");

		String content = body.substring(menubarStart, listadsStart);

		int textStart = 0;

		Pattern bylinePattern = Pattern.compile(
				"<span\\s+class=\"?byline\"?\\s*>", Pattern.CASE_INSENSITIVE);

		Matcher bylineMatcher = bylinePattern.matcher(content);
		if (bylineMatcher.find()) {
			textStart = bylineMatcher.start();
		} else {
			throw new IllegalContentFormatException("Cannot find match text");
		}
		
		// find mp3 url
		Pattern audioPattern = Pattern.compile("href=\"?([^\\s]+\\.mp3)\"?",
				Pattern.CASE_INSENSITIVE);

		Matcher audioMatcher = audioPattern.matcher(content);
		
		if (audioMatcher.find()) {
			article.urlmp3 = Voa51DataSource.HOST + audioMatcher.group(1);
		} else {
			throw new IllegalContentFormatException("Cannot find match mp3");
		}
		
		// find lrc url if exist
		if (article.haslrc) {
			Pattern lrcPattern = Pattern.compile("href=\"?([^\\s]+\\.lrc)\"?",
					Pattern.CASE_INSENSITIVE);

			Matcher lrcMatcher = lrcPattern.matcher(content);

			if (lrcMatcher.find()) {
				article.urllrc = Voa51DataSource.HOST + lrcMatcher.group(1);
			}
		}	
		// find translate url if exist
		if (article.hastextzh) {
			String urltext = article.urltext;
			
			article.urltextzh = urltext.substring(0, urltext.length() - 5) + "_1.html";
		}

		// transform relative url to absolute url
		String text = content.substring(textStart).replaceAll(
				"src=([\"\']?)(/[^\\s\'\">]+(?:\\.jpg|\\.png|\\.bmp|\\.gif))\\1?",
				"src=\"" + Voa51DataSource.HOST + "$2\"");

		// add necessary html tags, build full html text
		article.text = buildHtml(article.title, text);
		
		Log.d(CLASSTAG, "urltext -- " + article.urltext);
		Log.d(CLASSTAG, "urlmp3 -- " + article.urlmp3);
		Log.d(CLASSTAG, "urllrc -- " + article.urllrc);
		Log.d(CLASSTAG, "urltextzh -- " + article.urltextzh);
	}

	
}
