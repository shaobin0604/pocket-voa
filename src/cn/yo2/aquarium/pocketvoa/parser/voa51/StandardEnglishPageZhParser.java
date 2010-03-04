package cn.yo2.aquarium.pocketvoa.parser.voa51;

import cn.yo2.aquarium.pocketvoa.Article;
import cn.yo2.aquarium.pocketvoa.IllegalContentFormatException;
import cn.yo2.aquarium.pocketvoa.parser.AbstractPageParser;

public class StandardEnglishPageZhParser extends AbstractPageParser {
	public void parse(Article article, String body) throws IllegalContentFormatException {
		int menubarStart = body.indexOf("<div id=\"menubar\"");
		int menubarEnd = body.indexOf("</div>", menubarStart);
		
		int listadsStart = body.indexOf("<div id=\"listads\"", menubarStart);

		String content = body.substring(menubarEnd + 6, listadsStart);

		String text = content.replaceAll(
				"src=([\"\']?)(/[^\\s\'\">]+(?:\\.jpg|\\.png|\\.bmp|\\.gif))\\1?",
				"src=\"" + DataSource.HOST + "$2\"");

		article.textzh = buildHtml(article.title, text);
	}
}
