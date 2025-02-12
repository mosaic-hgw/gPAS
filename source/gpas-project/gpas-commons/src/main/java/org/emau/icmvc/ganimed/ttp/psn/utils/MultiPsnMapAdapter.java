package org.emau.icmvc.ganimed.ttp.psn.utils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Serialises a multimap with a list of PSNs per value into a list of MPSN entries
 * with the value as key attribute and the PSNs as list and back for deserialization.
 */
public class MultiPsnMapAdapter extends XmlAdapter<MultiPsnMapAdapter.MultiPsnMap, Map<String, List<String>>> {

	/**
	 * {@return a multimap of PSN pairs for a list of PSN pair map entries}
	 * @param pairs a list of PSN pair map entries
	 */
	public static Map<String, List<String>> toMultiPsnMap(List<Map.Entry<String, String>> pairs)
	{
		return pairs.stream().collect(Collectors.groupingBy(
				Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
	}

	public static class MultiPsnMap implements Serializable
	{
		@Serial
		private static final long serialVersionUID = -8811183203331996359L;

		@XmlElement(name = "multiPsn")
		private List<MultiPsn> multiPsns = new ArrayList<>();

		public MultiPsnMap()
		{
			// for JAXB
		}

		public MultiPsnMap(Map<String, List<String>> map) {
			map.forEach((key, value) -> this.multiPsns.add(new MultiPsn(key, value)));
		}
	}

	public static class MultiPsn implements Serializable
	{
		@Serial
		private static final long serialVersionUID = 3496581446353820551L;

		@XmlAttribute(name = "value", required = true)
		private String value;

		@XmlElement(name = "psn", required = true)
		private List<String> psns;

		public MultiPsn()
		{
			// for JAXB
		}

		public MultiPsn(String value, List<String> psns)
		{
			this.value = value;
			this.psns = psns;
		}
	}

	@Override
	public Map<String, List<String>> unmarshal(MultiPsnMap multiPsnMap) {
		return multiPsnMap.multiPsns.stream().collect(Collectors.toMap(multiPsn -> multiPsn.value, multiPsn -> multiPsn.psns, (a, b) -> b));
	}

	@Override
	public MultiPsnMap marshal(Map<String, List<String>> map) {
		return new MultiPsnMap(map);
	}
}