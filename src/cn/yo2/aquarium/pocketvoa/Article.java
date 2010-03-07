package cn.yo2.aquarium.pocketvoa;

public class Article {
	public static final String K_ID      = "_id";
	public static final String K_TYPE    = "type"; 
	public static final String K_SUBTYPE = "subtype";
	public static final String K_TITLE   = "title";
	public static final String K_DATE    = "date";
	public static final String K_URLTEXT = "urltext";
	public static final String K_URLMP3  = "urlmp3";
	public static final String K_TEXT    = "text";
	
	// the following three columns added from version 1.0.2
	public static final String K_URLTEXTZH = "urltextzh";
	public static final String K_TEXTZH    = "textzh"; 
	public static final String K_URLLRC    = "urllrc";
	public static final String K_HASTEXTZH = "hastextzh";
	public static final String K_HASLRC    = "haslrc";
	
	
	public long id;
	public String type;
	public String subtype;
	public String text;
	public String urlmp3;
	public String date;
	public String urltext;
	public String title;
	
	// the following five columns added from version 1.0.2
	public String textzh;
	public String urltextzh;
	public String urllrc;
	public boolean hastextzh;
	public boolean haslrc;
}
