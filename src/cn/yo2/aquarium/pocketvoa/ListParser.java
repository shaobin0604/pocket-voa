package cn.yo2.aquarium.pocketvoa;

import java.util.ArrayList;

public interface ListParser {
	public ArrayList<Article> parse(String body) throws IllegalContentFormatException;
}
