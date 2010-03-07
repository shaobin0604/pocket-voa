package cn.yo2.aquarium.pocketvoa.lyric;

public class Sentence implements Comparable<Sentence> {
	// from time in millis
	long mFromTime;
	// to time in millis
	long mToTime;
	// the content
	String mContent;

	public Sentence(String content, long fromTime, long toTime) {
		this.mContent = content;
		this.mFromTime = fromTime;
		this.mToTime = toTime;
	}

	public Sentence(String content, long bgnTime) {
		this(content, bgnTime, 0);
	}

	public Sentence(String content) {
		this(content, 0, 0);
	}

	public boolean isInTime(long time) {
		return time >= mFromTime && time <= mToTime;
	}

	public long getDuration() {
		return mToTime - mFromTime;
	}

	@Override
	public String toString() {
		return "Sentence [fromtime=" + mFromTime + ", totime=" + mToTime
				+ ", content=" + mContent + "]";
	}

	public int compareTo(Sentence another) {
		return (int) (this.mFromTime - another.mFromTime);
	}
}
