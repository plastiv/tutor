package ua.kharkiv.android.lingvotutor;


public class Cards implements Comparable<Cards>{
	
	private String mWord;
	private String mTranslationWord;
	private String mExample;
	
	public void setWord(String word) {
		this.mWord = word;
	}

	public String getWord() {
		return mWord;
	}
	
	public void setTranslationWord(String translationWord) {
		this.mTranslationWord = translationWord;
	}

	public String getTranslationWord() {
		return mTranslationWord;
	}

	public void setExample(String example) {
		this.mExample = example;
	}

	public String getExample() {
		return mExample;
	}

	public Cards copy(){
		Cards copy = new Cards();
		copy.mWord = mWord;
		copy.mTranslationWord = mTranslationWord;
		copy.mExample = mExample;
		return copy;
	}
	/*
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Title: ");
		sb.append(title);
		sb.append('\n');
		sb.append("Date: ");
		sb.append(this.getDate());
		sb.append('\n');
		sb.append("Link: ");
		sb.append(link);
		sb.append('\n');
		sb.append("Description: ");
		sb.append(description);
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		Cards other = (Cards) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
	*/

	public int compareTo(Cards another) {
		if (another == null) return 1;
		// sort descending, most recent first
		// TODO implement compareTo here
		// Returns a negative integer if this instance is less than another; a positive integer if this instance is greater than another; 0 if this instance has the same order as another.
		return 2;
	}
	


}
