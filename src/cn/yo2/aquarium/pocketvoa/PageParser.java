package cn.yo2.aquarium.pocketvoa;

public interface PageParser {
	public void parse(Article article, String body) throws IllegalContentFormatException;
}
