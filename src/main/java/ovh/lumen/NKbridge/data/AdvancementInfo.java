package ovh.lumen.NKbridge.data;

public class AdvancementInfo
{
	private String key;
	private String name;
	private String description;

	public AdvancementInfo(String key, String name, String description)
	{
		this.key = key;
		this.name = name;
		this.description = description;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}
}
