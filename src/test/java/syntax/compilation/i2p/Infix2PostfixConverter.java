package syntax.compilation.i2p;

import java.util.Stack;

public class Infix2PostfixConverter {

	private static boolean isOperator(char c)
	{
		return c == '+' || c == '-' || c == '*' || c == '/' || c == '^'
				|| c == '(' || c == ')';
	}

	private static boolean isLowerPrecedence(char op1, char op2)
	{
		switch (op1)
		{
		case '+':
		case '-':
			return !(op2 == '+' || op2 == '-');

		case '*':
		case '/':
			return op2 == '^' || op2 == '(';

		case '^':
			return op2 == '(';

		case '(':
			return true;

		default:
			return false;
		}
	}

	public static String convertToPostfix(String infix)
	{
		Stack<Character> stack = new Stack<Character>();
		StringBuffer postfix = new StringBuffer(infix.length());
		char c;

		for (int i = 0; i < infix.length(); i++)
		{
			c = infix.charAt(i);

			if (Character.isDigit(c)){
				while (Character.isDigit(c)){   
					postfix.append(c);
					i++;
					if(i == infix.length()) break;
					c = infix.charAt(i);
				}
				postfix.append(" ");
			}
			if(i == infix.length()) break;
			c = infix.charAt(i);
			if(isOperator(c))
			{
				if (c == ')')
				{

					while (!stack.isEmpty() && stack.peek() != '(')
					{
						postfix.append(stack.pop());
						postfix.append(" ");
					}
					if (!stack.isEmpty())
					{
						stack.pop();
					}
				}

				else
				{
					if (!stack.isEmpty() && !isLowerPrecedence(c, stack.peek()))
					{
						stack.push(c);
					}
					else
					{
						while (!stack.isEmpty() && isLowerPrecedence(c, stack.peek()))
						{
							Character pop = stack.pop();
							if (pop != '(')
									{
								postfix.append(pop+" ");
								
									} else {
										c = pop;
									}
						}
						stack.push(c);
					}

				}
			}
		}
		while (!stack.isEmpty()) {
			postfix.append(stack.pop()+" ");
		}
		return postfix.toString().trim();
	}
}

