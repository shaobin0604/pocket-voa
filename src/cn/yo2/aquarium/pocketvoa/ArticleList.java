package cn.yo2.aquarium.pocketvoa;

import java.util.ArrayList;

public class ArticleList {
	public ArrayList<Article> articles;
	public int totalPage;
	
	public ArticleList(ArrayList<Article> articles, int totalPage) {
		super();
		this.articles = articles;
		this.totalPage = totalPage;
	}
	
	
}
