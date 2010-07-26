package cn.yo2.aquarium.pocketvoa.parser;

import java.util.ArrayList;

import cn.yo2.aquarium.pocketvoa.Article;
import cn.yo2.aquarium.pocketvoa.IllegalContentFormatException;

public interface IListParser {
	public ArrayList<Article> parse(String body) throws IllegalContentFormatException;
	public int parsePageCount(String body) throws IllegalContentFormatException;
}
