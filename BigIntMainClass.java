import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Main Class that runs command line interpreter.
 */
public class BigIntMainClass {

	private final static String INTRO_MSG = "This is BigInt Calculator. The BigInt class implemens big integer(BigInt)\n"
			+ "using Double-Linked List and Binary Arithemetics.\n";
	private final static String HELP_MSG = "There are four arithmetic operation available: addition(+)\n"
			+ "subtraction(-), multiplication(*), and factorial(!).\n"
			+ "Other than the factorial, all the operations take 2 terms.\n"
			+ "Factorial takes 1 term before the operator.\n"
			+ "Factorial of zero or negative integers are explicitly defined as 1.\n" + "Spaces are ignored.\n";

	private final static String VALID_CHARS = "0123456789+-!* \t\n";
	private final static String SIGNS = "+-";
	private final static String OPERS = "+-*!";
	private final static String PROMPT = "ENTER COMMAND> ";

	private static Scanner scan = new Scanner(System.in);

	private final static boolean isOperation(String term) {
		return term.length() == 1 && OPERS.contains(term);
	}

	public final static void ShowHelp() {
		System.out.println(HELP_MSG);
	}

	public final static void ShowIntro() {
		System.out.println(INTRO_MSG);
	}

	public final static void printResult() {
		System.out.println(HELP_MSG);
	}

	public final static String getUserInput() {
		System.out.print(PROMPT);
		return scan.nextLine().trim();
	}

	// parseCommand takes a line of String and parse the terms and operation as
	// a List of Strings which then is used by executeCommand
	public final static List<String> parseCommand(String line) throws IllegalArgumentException {
		List<String> cmd = new ArrayList<String>();
		String term = "";
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (VALID_CHARS.indexOf(c) == -1)
				throw new IllegalArgumentException("Command Contains invalid Characters.");
			if (Character.isDigit(c))
				term += c;
			else {
				// if it is the first letter of a term and is not a digit.
				if (!term.isEmpty()) {
					cmd.add(term); // add and flush it
					term = "";
					i--; // one step backward
					continue;
				} else if (SIGNS.indexOf(c) != -1) {
					term += c;
				} else if (OPERS.indexOf(c) != -1) {
					cmd.add(c + "");
				}
			}
		}
		if (!term.isEmpty())
			cmd.add(term); // if there is a term left over, add it
		if (cmd.size() > 3)
			throw new IllegalArgumentException("There are too many terms in command: " + term.length());
		return cmd;
	}

	// executeCommand executes the given list of terms by initializing BigInt
	// instances and perform appropriate operation with them.
	// When the given command does not meet the requirements, throws
	// IllegalArgumentException. It also passes the BigIntFormatException to
	// caller when the initialization was unsuccessful.
	public final static BigInt executeCommand(List<String> cmd) throws BigIntFormatException, IllegalArgumentException {
		if (isOperation(cmd.get(0)))
			throw new IllegalArgumentException("Invalid order of command: The first term should not be an operation.");
		if (cmd.size() == 2 && !isOperation(cmd.get(1)))
			cmd.add(1, "+"); // In case of two terms and no operation, it is regarded as addition.
		BigInt result = new BigInt(cmd.get(0));
		if (isOperation(cmd.get(1))) {
			String op = cmd.get(1);
			if (!op.equals("!") && cmd.size() != 3)
				throw new IllegalArgumentException("Invalid number of command: This operation takes another term.");
			switch (op) {
			case "!":
				result = result.factorial();
				break;
			case "+":
				result = result.add(new BigInt(cmd.get(2)));
				break;
			case "-":
				result = result.subtract(new BigInt(cmd.get(2)));
				break;
			case "*":
				result = result.multiply(new BigInt(cmd.get(2)));
				break;
			}
		}
		return result;
	}

	public static void main(String[] args) {
		ShowIntro();
		// main loop starts
		while (true) {
			try {

				String input = getUserInput();
				if (input.length() == 1) {
					if (input.toLowerCase().equals("q"))
						System.exit(0);
					else if (input.toLowerCase().equals("h"))
						ShowHelp();
				} else {
					BigInt result = executeCommand(parseCommand(input));
					System.out.println(result);
				}
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage());
			} catch (BigIntFormatException e) {
				System.out.println("Cannot initialize BigInt with given terms");
				System.out.println(e.getMessage());
			}
		}
	}
}