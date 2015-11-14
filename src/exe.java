
public class exe {

	public static void main(String[] args)
	{
		String regexExpression = "([0-9]{4})-([0-9]{2})-([0-9]{2})";
		String input = "1990-09-21";
		String input2 = "1991-02-31";
		System.out.println(input.compareTo(input2));
		if (input.matches(regexExpression))
		{
			System.out.println("it matches");
		}
		else
		{
			System.out.println("erros");

		}
	}
}
