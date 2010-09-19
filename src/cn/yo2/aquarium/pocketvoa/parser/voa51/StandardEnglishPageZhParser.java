package cn.yo2.aquarium.pocketvoa.parser.voa51;

import cn.yo2.aquarium.pocketvoa.Article;
import cn.yo2.aquarium.pocketvoa.IllegalContentFormatException;
import cn.yo2.aquarium.pocketvoa.parser.AbstractPageParser;

public class StandardEnglishPageZhParser extends AbstractPageParser {
	public void parse(Article article, String body) throws IllegalContentFormatException {
		int menubarStart = body.indexOf("<div id=\"menubar\"");
		
		if (menubarStart < 0)
			throw new IllegalContentFormatException("Cannot find content");
		
		int menubarEnd = body.indexOf("</div>", menubarStart);
		
		int listadsStart = body.indexOf("<div id=\"listads\"", menubarStart);
		if (listadsStart < 0)
			listadsStart = body.indexOf("<div id=\"Bottom_Import\"");
		if (listadsStart < 0)
			listadsStart = body.indexOf("<div id=\"Bottom_VOA\"");
		
		
		if (menubarEnd < 0 || listadsStart < 0)
			throw new IllegalContentFormatException("Cannot find content");

		String content = body.substring(menubarEnd + 6, listadsStart);

		String text = content.replaceAll(
				"src=([\"\']?)(/[^\\s\'\">]+(?:\\.jpg|\\.png|\\.bmp|\\.gif))\\1?",
				"src=\"" + Voa51DataSource.HOST + "$2\"");

		article.textzh = buildHtml(article.title, text);
	}
}
