/**
 * Implementation of BigInt
 */
public class BigInt implements Comparable<BigInt> {

	// digits of the binary representation are stored in nodes
	private class Node {
		public int data;
		public Node prev;
		public Node next;

		public Node(int data) {
			if (data != 0 && data != 1) {
				throw new IllegalArgumentException("data = " + data);
			}
			this.data = data;
		}

		public Node(int data, Node prev) {
			this(data);
			this.prev = prev;
		}

		public Node(int data, Node prev, Node next) {
			this(data, prev);
			this.next = next;
		}
	}

	/* Private instance fields */
	private Node head, tail;
	private int size;

	/* Private helper functions */
	// negate this
	private void negate() {
		boolean toPositive = !isPositive();
		// 1's complement
		Node cur = tail;
		while (cur != null) {
			cur.data = 1 - cur.data;
			cur = cur.prev;
		}
		// 2's complement: just add 1
		int carry = 1;
		cur = head;
		while (cur != null) {
			cur.data += carry;
			carry = cur.data / 2;
			cur.data %= 2;
			cur = cur.next;
		}
		if (toPositive) {
			tail.next = new Node(0, tail);
			tail = tail.next;
		}
		removeLeading0sOr1s();
	}

	private boolean isPositive() {
		return tail.data == 0;
	}

	// Shift this by one Digit adding 0 after LSB and dropping the MSB.
	// Size is held constant.
	// Used by multiply().
	private void shiftOneDigit() {
		head.prev = new Node(0, null, head);
		head = head.prev;
		tail = tail.prev;
	}

	// Pad this extending size to n.
	// if the size is already larger than n, do nothing.
	// Used by multiply().
	private void pad(long n) {
		while (size < n) {
			tail.next = new Node(tail.data, tail);
			tail = tail.next;
			size++;
		}
	}

	// return new BigInt that contains n digit of this starting from LSB
	// Used by multiply().
	private BigInt subBigInt(long n) {
		BigInt result = null;
		try {
			result = new BigInt();
		} catch (BigIntFormatException e) {
			/*
			 * This would never happen but it is written this way to use
			 * constructor while letting constructors to throw exceptions which
			 * will be dealt by the command line program
			 */
			return null;
		}
		Node cur = head;
		while (cur != null && result.size != n) {
			if (result.size == 0) {
				result.tail.data = cur.data;
			} else {
				result.tail.next = new Node(cur.data, result.tail);
				result.tail = result.tail.next;
			}
			cur = cur.next;
			result.size++;
		}
		return result;
	}

	private void removeLeading0sOr1s() {
		int data = tail.data;
		Node cur = tail.prev;
		while (cur != null && cur.data == data) {
			tail = cur;
			tail.next = null;
			size--;
			cur = cur.prev;
		}
	}

	// String input divided by 2
	private static String div2(String s) {
		String result = "";
		int carry = 0;
		for (int i = 0; i < s.length(); i++) {
			int d = s.charAt(i) - '0';
			result += (d + 10 * carry) / 2;
			carry = d % 2;
		}

		if (result.charAt(0) == '0' && result.length() > 1) {
			int i = 1;
			while (i < result.length() && result.charAt(i) == '0') {
				i++;
			}
			result = result.substring(i);
			if (result.length() == 0) {
				result = "0";
			}
		}

		return result;
	}

	// String input mod by 2
	private static int mod2(String s) {
		int d = s.charAt(s.length() - 1) - '0';
		return d % 2;
	}

	// Add two string representations of decimal numbers.
	// Used by toString to perform binary to decimal conversion.
	private static String addDecStr(String a, String b) {
		String result = "";
		int d = 0;
		for (int i = 1; i <= Math.max(a.length(), b.length()) || d != 0; i++) {
			d += (i <= a.length() ? Integer.parseInt(a.charAt(a.length() - i) + "") : 0)
					+ (i <= b.length() ? Integer.parseInt(b.charAt(b.length() - i) + "") : 0);
			result = (d % 10) + result;
			d = d / 10;
		}
		return result;
	}

	/* Constructors */
	// Private constructor for internal use
	private BigInt() throws BigIntFormatException {
		this(0);
		size = 0;
	}

	// BigInt(String val) Construct a BigInt object and initialize it with the
	// integer represented by the String. Throw an appropriate exception
	// (BigIntFormatException) if the string does not represent a signed integer
	// (i.e. contains illegal characters)
	public BigInt(String s) throws BigIntFormatException {
		s = s.trim();
		String[] parts = s.split("\\s+");
		String number = "";
		int sign = +1;

		if (parts.length == 0 || parts.length > 2 || parts[0].length() == 0) {
			throw new BigIntFormatException();
		}
		// if 2 pieces, the first piece must be + or -
		if (parts.length == 2 && !parts[0].equals("-") && !parts[0].equals("+")) {
			throw new BigIntFormatException();
		}

		number = parts[0] + ((parts.length > 1) ? parts[1] : "");
		if (number.charAt(0) == '-') {
			sign = -1;
		}
		if (number.charAt(0) == '+' || number.charAt(0) == '-') {
			number = number.substring(1); // drop the sign
		}

		// only digits in number?
		for (int i = 0; i < number.length(); i++) {
			char c = number.charAt(i);
			if (!Character.isDigit(c)) {
				throw new BigIntFormatException();
			}
		}
		if (number.equals("0")) {
			head = tail = new Node(0);
			size = 1;
		}

		// construct the linked list
		while (!number.equals("0")) {
			int d = mod2(number);
			if (head == null) {
				head = tail = new Node(d);
			} else {
				tail.next = new Node(d, tail);
				tail = tail.next;
			}
			size++;
			number = div2(number);
		}
		// add a 0 for the sign (we will take the 2's complement
		// later if negative)
		Node n = new Node(0, tail);
		if (tail != null) {
			tail.next = n;
		} else {
			head = n;
		}
		tail = n;
		size++;

		if (sign == -1) {
			negate();
		}

		// remove any leading 0's and 1's (except the last 0 or 1)
		removeLeading0sOr1s();
	}

	// BigInt(BigInt val) This is the copy constructor. It should make a deep
	// copy of val. Making a deep copy is not strictly necessary since as
	// designed a BigInt is immutable, but it is good practice.
	public BigInt(BigInt val) throws BigIntFormatException {
		this();
		Node cur = val.head;
		while (cur != null) {
			if (size == 0) {
				tail.data = cur.data;
			} else {
				tail.next = new Node(cur.data, tail);
				tail = tail.next;
			}
			cur = cur.next;
			size++;
		}
	}

	// BigInt(long val)
	// Construct a BigInt object and intitialize it wth the value stored in val
	public BigInt(long val) throws BigIntFormatException {
		this(val + "");
	}

	/* Arithmetic methods: add, multiply, subtract, factorial */
	// BigInt add(BigInt val) Returns a BigInt whose value is (this + val)
	public BigInt add(BigInt val) {
		BigInt b = null;
		try {
			b = new BigInt();
		} catch (BigIntFormatException e) {
			// this would never happen ...
			return null;
		}

		int length = Math.max(val.size, size) + 2;
		Node vcur = val.head;
		Node tcur = head;
		int carry = 0;

		while (b.size != length) {
			int data = carry + (vcur != null ? vcur.data : val.isPositive() ? 0 : 1)
					+ (tcur != null ? tcur.data : isPositive() ? 0 : 1);
			if (b.size == 0) {
				b.tail.data = data % 2;
			} else {
				b.tail.next = new Node(data % 2, b.tail);
				b.tail = b.tail.next;
			}
			b.size++;
			carry = data / 2;
			vcur = vcur != null ? vcur.next : null;
			tcur = tcur != null ? tcur.next : null;
		}
		b.removeLeading0sOr1s();
		return b;
	}

	// BigInt multiply(BigInt val) Returns a BigInt whose value is (this * val)
	public BigInt multiply(BigInt val) {
		long length = Math.max(val.size, size) * 2;

		BigInt a = null, b = null, result = null;
		try {
			a = new BigInt(this);
			b = new BigInt(val);
			result = new BigInt();
			a.pad(length);
			b.pad(length);
		} catch (BigIntFormatException e) {
			// this would never happen ...
			return null;
		}
		while (a.head != null) {
			if (a.head.data == 1)
				result = result.add(b);
			b.shiftOneDigit();
			a.head = a.head.next;
		}
		result = result.subBigInt(length);
		result.removeLeading0sOr1s();
		return result;
	}

	// BigInt subtract(BigInt val) Returns a BigInt whose value is (this - val)
	public BigInt subtract(BigInt val) {
		BigInt b = null;
		try {
			b = new BigInt(val);
			b.negate();
		} catch (BigIntFormatException e) {
			// this would never happen ...
			return null;
		}
		return add(b);
	}

	// BigInt factorial() Returns a BigInt whose value is this!
	public BigInt factorial() {
		try {
			return compareTo(new BigInt(0)) <= 0 ? new BigInt(1) : multiply(subtract(new BigInt(1)).factorial());
		} catch (BigIntFormatException e) {
			// this would never happen ...
			return null;
		}
	}

	/* Comparison related methods */
	// int compareTo(BigInt) Have the BigInt class implement the Comparable
	// interface.
	@Override
	public int compareTo(BigInt b) {
		return equals(b) ? 0 : (subtract(b).isPositive() ? 1 : -1);
	}

	// boolean equals(Object)
	// Override the equals() method from Object.
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof BigInt) {
			BigInt b = (BigInt) obj;
			if (size != b.size)
				return false;
			Node p = tail;
			Node q = b.tail;
			while (p != null) {
				if (p.data != q.data)
					return false;
				p = p.prev;
				q = q.prev;
			}
			return true;
		} else
			return false;
	}

	// String toString() Returns the decimal representation of this BigInt as a
	// String
	public String toString() {
		int sign = +1;
		BigInt b = null;
		try {
			b = new BigInt(this);
		} catch (BigIntFormatException e) {
			// this would never happen...
			return null;
		}
		if (!b.isPositive()) {
			b.negate();
			sign = -1;
		}
		Node cur = b.tail;
		String result = "";
		while (cur != null) {
			if (!result.isEmpty())
				result = BigInt.addDecStr(result, result);
			if (cur.data == 1)
				result = BigInt.addDecStr(result, "1");
			cur = cur.prev;
		}
		if (result.isEmpty())
			result = "0";
		return (sign == -1 ? "-" : "") + result;
	}

	// String toString2s() Returns the 2's complement representation of this
	// BigInt as a String using the minimum number of digits necessary (e.g. 0
	// is "0", -1 is "1", 2 is "010", -2 is "10", etc).
	public String toString2s() {
		String result = tail.data + "";
		Node cur = tail.prev;
		while (cur != null) {
			result += cur.data;
			cur = cur.prev;
		}
		return result;
	}
}