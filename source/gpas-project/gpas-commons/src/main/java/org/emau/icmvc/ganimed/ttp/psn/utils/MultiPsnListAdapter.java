package org.emau.icmvc.ganimed.ttp.psn.utils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Serialises a list of map entries with strings as keys and values into a list of PSN pairs with the
 * original value as key element and the pseudonym as value element and back for deserialization.
 */
public class MultiPsnListAdapter extends XmlAdapter<MultiPsnListAdapter.MultiPsnList, List<StringPair>> {

	/**
	 * {@return a list of PSN pair map entries for a multimap of PSN pairs}
	 * @param pairs a multimap of PSN pairs
	 */
	public static List<StringPair> toMultiPsnList(Map<String, List<String>> pairs)
	{
		return pairs.entrySet().stream().flatMap(entry ->
				entry.getValue().stream().map(value -> new StringPair(entry.getKey(), value))).toList();
	}

	public static class MultiPsnList implements Serializable
	{
		@Serial
		private static final long serialVersionUID = 8945472251714530081L;

		@XmlElement(name = "entry")
		private List<Psn> pairs = new ArrayList<>();

		public MultiPsnList()
		{
			// for JAXB
		}

		public MultiPsnList(List<StringPair> pairs)
		{
			pairs.forEach(e -> this.pairs.add(new Psn(e)));
		}
	}

	public static class Psn implements Serializable
	{
		@Serial
		private static final long serialVersionUID = 7092996296407372543L;

		@XmlElement(name = "key", required = true)
		private String originalValue;

		@XmlElement(name = "value", required = true)
		private String pseudonym;

		public Psn()
		{
			// for JAXB
		}

		public Psn(StringPair pair)
		{
			this(pair.getFirst(), pair.getSecond());
		}

		public Psn(String originalValue, String pseudonym)
		{
			this.originalValue = originalValue;
			this.pseudonym = pseudonym;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;

			if (!(o instanceof Psn psn))
				return false;

			return new EqualsBuilder().append(originalValue, psn.originalValue).append(pseudonym, psn.pseudonym).isEquals();
		}

		@Override
		public int hashCode()
		{
			return new HashCodeBuilder(17, 37).append(originalValue).append(pseudonym).toHashCode();
		}
	}

	@Override
	public List<StringPair> unmarshal(MultiPsnList multiPsnList) {
		return multiPsnList.pairs.stream().map(e -> new StringPair(e.originalValue, e.pseudonym)).collect(Collectors.toList());
	}

	@Override
	public MultiPsnList marshal(List<StringPair> pairs) {
		return new MultiPsnList(pairs);
	}
}