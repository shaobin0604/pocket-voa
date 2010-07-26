package cn.yo2.aquarium.pocketvoa.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import cn.yo2.aquarium.pocketvoa.IllegalContentFormatException;


public abstract class AbstractListParser implements IListParser {
	
	private static final String TAG = AbstractListParser.class.getSimpleName();
	protected String mType;
	protected String mSubtype;
	
	public AbstractListParser(String type, String subtype) {
		super();
		this.mType = type;
		this.mSubtype = subtype;
	}

	@Override
	public int parsePageCount(String body) throws IllegalContentFormatException {
		Pattern pattern = Pattern.compile("页次：\\s*<b>\\s*\\d+\\s*</b>\\s*/\\s*<b>\\s*(\\d+)\\s*</b>");
		int count = 0;
		
		Matcher matcher = pattern.matcher(body);
		
		String match = null;
		if (matcher.find()) {
			match = matcher.group(1);
			Log.d(TAG, "[parsePageCount] match -- " + match);
		}
		if (match != null)
			count = Integer.parseInt(match);
		return count;
	}
	
	
}
