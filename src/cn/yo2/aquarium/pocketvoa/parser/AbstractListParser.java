package cn.yo2.aquarium.pocketvoa.parser;


public abstract class AbstractListParser implements IListParser {
	
	protected String mType;
	protected String mSubtype;
	
	public AbstractListParser(String type, String subtype) {
		super();
		this.mType = type;
		this.mSubtype = subtype;
	}
	
}
