package org.emau.icmvc.ganimed.ttp.psn.utils;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.emau.icmvc.ttp.util.Pair;

public class StringPair extends Pair<String, String> implements Serializable
{
	@Serial
	private static final long serialVersionUID = -9189450274011754505L;

	public StringPair()
	{
		// for JSON
	}

	public StringPair(String first, String second)
	{
		super(first, second);
	}

	public StringPair(Map.Entry<String, String> entry)
	{
		super(entry);
	}

	public static List<StringPair> fromMapEntriesToStringPairs(Map<String, String> pairs)
	{
		return pairs.entrySet().stream().map(StringPair::new).toList();
	}

}
