package space.engine.string;

import java.util.Arrays;

@SuppressWarnings("SameParameterValue")
public class StringUtil {
	
	public static final String nextLine = String.format("%n");
	public static final char[] nextLineChars = nextLine.toCharArray();
	public static final int nextLineLength = nextLine.length();
	
	public static StringBuilder2D hrLittle(int length) {
		char[] c = new char[length];
		Arrays.fill(c, '-');
		String m = new String(c);
		
		StringBuilder2D b = new StringBuilder2D();
		b.nextLine();
		b.append(m).nextLine();
		b.append("");
		
		return b;
	}
	
	public static StringBuilder2D hrBig(int length) {
		char[] c = new char[length];
		Arrays.fill(c, '-');
		String m = new String(c);
		
		StringBuilder2D b = new StringBuilder2D();
		b.nextLine();
		b.append(m).nextLine();
		b.append(m).nextLine();
		b.append(m).nextLine();
		b.append("");
		
		return b;
	}
}
