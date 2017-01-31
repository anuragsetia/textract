package org.textract;

public class FieldSource {
	
	public static final int SEARCH_TYPE_SIMPLE_REGEX = 1;
	public static final int SEARCH_TYPE_OFFSET_LENGTH = 2;
	public static final int SEARCH_TYPE_OFFSET_TERMINATION = 3;	
	public static final int SEARCH_TYPE_REGEX_OFFSET_LENGTH = 4;
	public static final int SEARCH_TYPE_REGEX_OFFSET_TERMINATION = 5;	
	
	private int searchType;
	private int offset;
	private int length;
	private String fieldName;
	private String regex;
	private String terminatingChar;
	

	public int getSearchType() {
		return searchType;
	}

	public void setSearchType(int searchType) {
		this.searchType = searchType;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getTerminatingChar() {
		return terminatingChar;
	}

	public void setTerminatingChar(String terminatingChar) {
		this.terminatingChar = terminatingChar;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	

}
