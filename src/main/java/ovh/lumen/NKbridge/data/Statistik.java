package ovh.lumen.NKbridge.data;

public class Statistik
{
	private final String name;
	private final int amount;

	public Statistik(String name, int amount)
	{
		this.name = name;
		this.amount = amount;
	}

	public String getName()
	{
		return name;
	}

	public int getAmount()
	{
		return amount;
	}
}
