package cn.yo2.aquarium.pocketvoa.parser;

import cn.yo2.aquarium.pocketvoa.Article;
import cn.yo2.aquarium.pocketvoa.IllegalContentFormatException;

public interface IPageParser {
	public void parse(Article article, String body) throws IllegalContentFormatException;
}
