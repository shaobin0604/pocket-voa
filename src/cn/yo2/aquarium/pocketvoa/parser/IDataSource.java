package cn.yo2.aquarium.pocketvoa.parser;

import java.util.HashMap;

public interface IDataSource {
	public void init(int maxCount);
	
	public HashMap<String, String> getListUrls();
	public HashMap<String, IListParser> getListParsers();
	public HashMap<String, IPageParser> getPageParsers();
	public HashMap<String, IPageParser> getPageZhParsers();
	
	public String getName();
}
