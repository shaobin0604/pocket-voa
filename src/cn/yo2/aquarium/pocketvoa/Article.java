package cn.yo2.aquarium.pocketvoa;

import android.os.Parcel;
import android.os.Parcelable;

public class Article implements Parcelable {
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
	
	public Article() {
		super();
	}
	
	public Article(long id, String type, String subtype, String text,
			String urlmp3, String date, String urltext, String title,
			String textzh, String urltextzh, String urllrc, boolean hastextzh,
			boolean haslrc) {
		super();
		this.id = id;
		this.type = type;
		this.subtype = subtype;
		this.text = text;
		this.urlmp3 = urlmp3;
		this.date = date;
		this.urltext = urltext;
		this.title = title;
		this.textzh = textzh;
		this.urltextzh = urltextzh;
		this.urllrc = urllrc;
		this.hastextzh = hastextzh;
		this.haslrc = haslrc;
	}

	public Article(String type, String subtype, String text, String urlmp3,
			String date, String urltext, String title, String textzh,
			String urltextzh, String urllrc, boolean hastextzh, boolean haslrc) {
		super();
		this.type = type;
		this.subtype = subtype;
		this.text = text;
		this.urlmp3 = urlmp3;
		this.date = date;
		this.urltext = urltext;
		this.title = title;
		this.textzh = textzh;
		this.urltextzh = urltextzh;
		this.urllrc = urllrc;
		this.hastextzh = hastextzh;
		this.haslrc = haslrc;
	}
	
	private Article(Parcel in) {
		this.id = in.readLong();
		this.type = in.readString();
		this.subtype = in.readString();
		this.text = in.readString();
		this.urlmp3 = in.readString();
		this.date = in.readString();
		this.urltext = in.readString();
		this.title = in.readString();
		this.textzh = in.readString();
		this.urltextzh = in.readString();
		this.urllrc = in.readString();
		this.hastextzh = in.readByte() == 1;
		this.haslrc = in.readByte() == 1;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.id);
		dest.writeString(this.type);
		dest.writeString(this.subtype);
		dest.writeString(this.text);
		dest.writeString(this.urlmp3);
		dest.writeString(this.date);
		dest.writeString(this.urltext);
		dest.writeString(this.title);
		dest.writeString(this.textzh);
		dest.writeString(this.urltextzh);
		dest.writeString(this.urllrc);
		dest.writeByte(this.hastextzh ? (byte) 1 : (byte) 0);
		dest.writeByte(this.haslrc ? (byte) 1 : (byte) 0);
	}
	
	public static final Parcelable.Creator<Article> CREATOR = new Parcelable.Creator<Article>(){  
	       public Article createFromParcel(Parcel in) {  
	           return new Article(in);  
	       } 
	       
	       public Article[] newArray(int size) {  
	           return new Article[size];  
	      }  
	};

	@Override
	public String toString() {
		return "Article [date=" + date + ", haslrc=" + haslrc + ", hastextzh="
				+ hastextzh + ", id=" + id + ", subtype=" + subtype + ", text="
				+ text + ", textzh=" + textzh + ", title=" + title + ", type="
				+ type + ", urllrc=" + urllrc + ", urlmp3=" + urlmp3
				+ ", urltext=" + urltext + ", urltextzh=" + urltextzh + "]";
	}

	
	
}
