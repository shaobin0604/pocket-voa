package cn.yo2.aquarium.pocketvoa.lyric;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;


public class Lyric {
	private static final String TAG = Lyric.class.getSimpleName();

	private List<Sentence> list = new ArrayList<Sentence>();// 里面装的是所有的句子
	String ti;
	String ar;
	String al;
	String by;

	public void clear() {
		list.clear();
	}

	/**
	 * 调整整体的时间,比如歌词统一快多少 或者歌词统一慢多少,为正说明要快,为负说明要慢
	 * 
	 * @param time
	 *            要调的时间,单位是毫秒
	 */
	public void setTimeOffset(int time) {
		// 如果是只有一个显示的,那就说明没有什么效对的意义了,直接返回
		if (list.size() == 1) {
			return;
		}
		for (Sentence s : list) {
			s.mFromTime -= time;
			s.mToTime -= time;
		}
	}
	
	/**
	 * 最重要的一个方法，它根据读到的歌词内容 进行初始化，比如把歌词一句一句分开并计算好时间
	 * 
	 * @param is
	 *            歌词内容
	 */
	public boolean parseLyric(InputStream is) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				parseLine(line);
			}
		} catch (Exception ex) {
			Log.e(TAG, "Error in parseLyric");
			return false;
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					// Omit it
				}
		}
		// sort sentence by mBgnTime
		Collections.sort(list);

		// set mEndTime attribute of each line
		int size = list.size();
		for (int i = 0; i < size; i++) {
			Sentence next = null;
			if (i + 1 < size) {
				next = list.get(i + 1);
			}
			Sentence now = list.get(i);
			if (next != null) {
				now.mToTime = next.mFromTime - 1;
			}
		}
		Sentence last = list.get(list.size() - 1);
		last.mToTime = Integer.MAX_VALUE;

		return true;
	}

	/**
	 * 分析这一行的内容，根据这内容 以及标签的数量生成若干个Sentence对象
	 * 
	 * @param line
	 *            这一行
	 */
	private void parseLine(String line) {
		if (line.equals("")) {
			return;
		}
		Matcher m = Pattern.compile("(?<=\\[).*?(?=\\])").matcher(line);
		List<String> tags = new ArrayList<String>();
		int length = 0;
		while (m.find()) {
			String tag = m.group();
			tags.add(tag);
			length += (tag.length() + 2);
		}
		try {
			for (String tag : tags) {
				if (tag.startsWith("ti")) {
					ti = tag.substring(3);
				} else if (tag.startsWith("ar"))
					ar = tag.substring(3);
				else if (tag.startsWith("al"))
					al = tag.substring(3);
				else if (tag.startsWith("by"))
					by = tag.substring(3);
				else {
					String content = line
							.substring(length > line.length() ? line.length()
									: length);

					if (content.equals("")) {
						return;
					}
					// Time Tag
					long time = parseTime(tag);

					if (time != -1) {
						list.add(new Sentence(content, time));
					}
				}

			}
		} catch (Exception e) {
			Log.e(TAG, "Error in parseLine");
		}
	}

	/**
	 * 把如00:00.00这样的字符串转化成 毫秒数的时间，比如 01:10.34就是一分钟加上10秒再加上340毫秒 也就是返回70340毫秒
	 * 
	 * @param time
	 *            字符串的时间
	 * @return 此时间表示的毫秒
	 */
	private long parseTime(String time) {
		String[] ss = time.split("\\:|\\.");
		// 如果 是两位以后，就非法了
		if (ss.length < 2) {
			return -1;
		} else if (ss.length == 2) {// 如果正好两位，就算分秒
			try {
				int min = Integer.parseInt(ss[0]);
				int sec = Integer.parseInt(ss[1]);
				if (min < 0 || sec < 0 || sec >= 60) {
					throw new RuntimeException("数字不合法!");
				}
				return (min * 60 + sec) * 1000L;
			} catch (Exception exe) {
				return -1;
			}
		} else if (ss.length == 3) {// 如果正好三位，就算分秒，十毫秒
			try {
				int min = Integer.parseInt(ss[0]);
				int sec = Integer.parseInt(ss[1]);
				int mm = Integer.parseInt(ss[2]);
				if (min < 0 || sec < 0 || sec >= 60 || mm < 0 || mm > 99) {
					throw new RuntimeException("数字不合法!");
				}
				return (min * 60 + sec) * 1000L + mm * 10;
			} catch (Exception exe) {
				return -1;
			}
		} else {// 否则也非法
			return -1;
		}
	}

	/**
	 * 得到当前正在播放的那一句的下标 不可能找不到，因为最开头要加一句 自己的句子 ，所以加了以后就不可能找不到了
	 * 
	 * @return 句子
	 */
	public Sentence getSentenceInTime(long time) {
		for (int i = 0; i < list.size(); i++) {
			Sentence sentence = list.get(i);
			if (sentence.isInTime(time)) {
				return sentence;
			}
		}
		return null;
	}


	public Sentence getSentence(int index) {
		return list.get(index);
	}

	public int getSentenceIndexInTime(long time) {
		for (int i = 0; i < list.size(); i++) {
			Sentence sentence = list.get(i);
			if (sentence.isInTime(time)) {
				return i;
			}
		}
		return -1;
	}
	
//	public int getSentenceIndexInTimeFast(long time) {
//		int start = 0, end = list.size() - 1;
//		int mid;
//		int range;
//		Sentence sentence = null;
//		while (true) {
//			range = end - start;
//			
//			if (range == 0 && !sentence.isInTime(time))
//				return -1;
//			
//			mid = start + range/2;
//			sentence = list.get(mid);
//			if (sentence.isInTime(time)) 
//				return mid;
//			else if (time < sentence.mFromTime)
//				end = mid - 1;
//			else 
//				start = mid + 1;
//		}
//	}
	
	public int getSentenceIndexInTimeFast(long time) {
		int start = 0, end = list.size() - 1;
		int mid;
		Sentence sentence = null;
		while (start <= end) {
			
			mid = start + (end - start)/2;
			sentence = list.get(mid);
			if (time > sentence.mToTime) 
				start = mid + 1;
			else if (time < sentence.mFromTime)
				end = mid - 1;
			else 
				return mid;
		}
		return -1;
	}

	public int getSize() {
		return list.size();
	}

	@Override
	public String toString() {
		return "Lyric [list=" + list + ", title=" + ti + "]";
	}
}
