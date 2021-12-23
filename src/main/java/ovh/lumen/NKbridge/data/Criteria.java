package ovh.lumen.NKbridge.data;

public class Criteria
{
	private final String name;
	private final boolean done;

	public Criteria(String name, boolean done)
	{
		this.name = name;
		this.done = done;
	}

	public String getName()
	{
		return name;
	}

	public boolean isDone()
	{
		return done;
	}
}
