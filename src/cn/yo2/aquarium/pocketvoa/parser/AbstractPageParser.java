package cn.yo2.aquarium.pocketvoa.parser;

public abstract class AbstractPageParser implements IPageParser {
	private static final String HTML_DEC = "<!DOCTYPE html PUBliC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\" >"
		+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" >"
		+ "<head><title>";
	
	protected String buildHtml(String title, String text) {
		StringBuilder s = new StringBuilder(HTML_DEC);
		s.append(title);
		s.append("</title></head><body><h1>");
		s.append(title);
		s.append("</h1><div id=\"content\">");
		s.append(text);
		s.append("</body></html>");

		return s.toString();
	}
}
