package cn.yo2.aquarium.pocketvoa.parser;

import java.util.ArrayList;

import cn.yo2.aquarium.pocketvoa.Article;
import cn.yo2.aquarium.pocketvoa.IllegalContentFormatException;

public interface IListParser {
	public static final int DEFAULT_MAX_COUNT = 10;
	
	public void setMaxCount(int maxCount);
	public int getMaxCount();
	public ArrayList<Article> parse(String body) throws IllegalContentFormatException;
}
