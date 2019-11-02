package com.beirtipol.dates;

public class ConverterKey {
	private final Class<?>	from;
	private final Class<?>	to;

	public ConverterKey(Class<?> from, Class<?> to) {
		this.from = from;
		this.to = to;
	}

	public Class<?> getFrom() {
		return from;
	}

	public Class<?> getTo() {
		return to;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		ConverterKey other = (ConverterKey) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[from=" + from + ", to=" + to + "]";
	}

}