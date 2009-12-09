package cn.yo2.aquarium.pocketvoa.parser;


public abstract class AbstractListParser implements IListParser {
	
	protected String mType;
	protected String mSubtype;
	
	protected int mMaxCount = DEFAULT_MAX_COUNT;
	
	public int getMaxCount() {
		return mMaxCount;
	}



	public void setMaxCount(int maxCount) {
		this.mMaxCount = maxCount;
	}



	public AbstractListParser(String type, String subtype) {
		super();
		this.mType = type;
		this.mSubtype = subtype;
	}
		
	public AbstractListParser(String type, String subtype, int maxCount) {
		this(type, subtype);
		this.mMaxCount = maxCount;
	}
	
	
}
