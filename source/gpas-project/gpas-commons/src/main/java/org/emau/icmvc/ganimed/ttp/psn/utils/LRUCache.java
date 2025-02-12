package org.emau.icmvc.ganimed.ttp.psn.utils;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> extends LinkedHashMap<K, V>
{
	@Serial
	private static final long serialVersionUID = -7252965024217200035L;

	private final int maxEntries;

	public LRUCache(int maxEntries)
	{
		super(maxEntries, 0.75f, true);
		this.maxEntries = maxEntries;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
	{
		return size() > maxEntries;
	}
}
