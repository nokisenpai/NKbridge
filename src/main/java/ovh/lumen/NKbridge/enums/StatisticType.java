package ovh.lumen.NKbridge.enums;

import java.util.HashMap;
import java.util.Map;

public enum StatisticType
{
	CUSTOM("minecraft:custom"),
	CRAFTED("minecraft:crafted"),
	BROKEN("minecraft:broken"),
	PICKED_UP("minecraft:picked_up"),
	DROPPED("minecraft:dropped"),
	MINED("minecraft:mined"),
	KILLED_BY("minecraft:killed_by"),
	KILLED("minecraft:killed"),
	USED("minecraft:used");

	private static final Map<String, StatisticType> lookup = new HashMap<>();

	static
	{
		for(StatisticType s : StatisticType.values())
		{
			lookup.put(s.value, s);
		}
	}

	private final String value;

	StatisticType(String value)
	{
		this.value = value;
	}

	public String toString()
	{
		return String.valueOf(this.value);
	}

	public String value()
	{
		return this.value;
	}

	public static StatisticType getFromValue(String value)
	{
		return lookup.get(value);
	}
}
