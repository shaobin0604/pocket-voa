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
	
	public Long id;
	public String type;
	public String subtype;
	public String text;
	public String mp3;
	public String date;
	public String url;
	public String title;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((mp3 == null) ? 0 : mp3.hashCode());
		result = prime * result + ((subtype == null) ? 0 : subtype.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Article other = (Article) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (mp3 == null) {
			if (other.mp3 != null)
				return false;
		} else if (!mp3.equals(other.mp3))
			return false;
		if (subtype == null) {
			if (other.subtype != null)
				return false;
		} else if (!subtype.equals(other.subtype))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Article [date=" + date + ", id=" + id + ", mp3=" + mp3
				+ ", subtype=" + subtype + ", text=" + text + ", title="
				+ title + ", type=" + type + ", url=" + url + "]";
	}	
	
	
}
