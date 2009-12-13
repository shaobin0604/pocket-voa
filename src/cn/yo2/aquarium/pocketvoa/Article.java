package cn.yo2.aquarium.pocketvoa;

public class Article {
	public static final String K_ID      = "_id";
	public static final String K_TYPE    = "type"; 
	public static final String K_SUBTYPE = "subtype";
	public static final String K_TITLE   = "title";
	public static final String K_DATE    = "date";
	public static final String K_URL     = "url";
	public static final String K_MP3     = "mp3";
	public static final String K_TEXT    = "text";
	
	public long id;
	public String type;
	public String subtype;
	public String text;
	public String mp3;
	public String date;
	public String url;
	public String title;
	@Override
	public String toString() {
		return "Article [date=" + date + ", id=" + id + ", mp3=" + mp3
				+ ", subtype=" + subtype + ", text=" + text + ", title="
				+ title + ", type=" + type + ", url=" + url + "]";
	}
}
