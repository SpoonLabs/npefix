package fr.inria.spirals.npefix.resi.context;

import org.json.JSONObject;

import java.io.Serializable;

public class Location implements Comparable<Location>, Serializable {

	private static final long serialVersionUID = 1L;

	private String className;
	private int line;
	private int sourceStart;
	private int sourceEnd;

	public Location(String className, int line, int sourceStart, int sourceEnd) {
		this.className = className;
		this.line = line;
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public int getLine() {
		return line;
	}

	public int getSourceStart() {
		return sourceStart;
	}

	public void setSourceStart(int sourceStart) {
		this.sourceStart = sourceStart;
	}

	public int getSourceEnd() {
		return sourceEnd;
	}

	public void setSourceEnd(int sourceEnd) {
		this.sourceEnd = sourceEnd;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Location location = (Location) o;

		if (line != location.line)
			return false;
		if (sourceStart != location.sourceStart)
			return false;
		if (sourceEnd != location.sourceEnd)
			return false;
		if (className != null ?
				!className.equals(location.className) :
				location.className != null)
			return false;

		return true;
	}

	@Override
	public int compareTo(Location location) {
		if(this.equals(location)) {
			return 0;
		}
		if(this.className.equals(location.className)) {
			if(this.line == (location.line)) {
				return this.sourceStart - location.sourceStart;
			} else {
				return this.line - (location.line);
			}
		} else {
			return this.className.compareTo(location.className);
		}
	}

	@Override
	public int hashCode() {
		int result = className != null ? className.hashCode() : 0;
		result = 31 * result + line;
		result = 31 * result + sourceStart;
		result = 31 * result + sourceEnd;
		return result;
	}

	@Override
	public String toString() {
		return className.replace("org.apache.commons.", "") + ":" + line;
	}

	public JSONObject toJSON() {
		JSONObject locationJSON = new JSONObject();
		locationJSON.put("class", this.getClassName());
		locationJSON.put("line", this.getLine());
		locationJSON.put("sourceStart", this.getSourceStart());
		locationJSON.put("sourceEnd", this.getSourceEnd());
		return locationJSON;
	}

}
