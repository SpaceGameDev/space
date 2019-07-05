package space.engine.string;

import org.jetbrains.annotations.NotNull;

public class String2D implements CharSequence2D {
	
	public static final String2D EMPTY = new String2D(new char[0][], 0);
	
	public final char[][] buffer;
	protected int maxLength;
	
	//2D
	public String2D(char[][] buffer) {
		this(buffer, -1);
	}
	
	public String2D(char[][] buffer, int maxLength) {
		this.buffer = buffer;
		this.maxLength = maxLength;
	}
	
	public String2D(CharSequence2D squ) {
		this(squ.getChars(), squ.maxLength());
	}
	
	//1D
	public String2D(char c) {
		this(new char[][] {new char[] {c}}, 1);
	}
	
	public String2D(char[] squ) {
		this(new char[][] {squ}, squ.length);
	}
	
	public String2D(CharSequence squ) {
		this(squ.toString().toCharArray());
	}
	
	//1D array
	public String2D(CharSequence[] squs) {
		this(charSequenceArrayTo2DCharBuffer(squs));
	}
	
	protected static char[][] charSequenceArrayTo2DCharBuffer(CharSequence[] squs) {
		int l = squs.length;
		char[][] buffer = new char[l][];
		for (int i = 0; i < l; i++)
			buffer[i] = squs[i].toString().toCharArray();
		return buffer;
	}
	
	public static String2D parse(String str) {
		return new String2D(str.split(StringUtil.nextLine));
	}
	
	//methods
	@Override
	public int height() {
		return buffer.length;
	}
	
	@Override
	public int length(int h) {
		return buffer[h].length;
	}
	
	@Override
	public int maxLength() {
		if (maxLength != -1)
			return maxLength;
		
		int l = 0;
		for (char[] buf : buffer)
			if (buf.length > l)
				l = buf.length;
		
		maxLength = l;
		return l;
	}
	
	@NotNull
	@Override
	public char[][] getChars() {
		return buffer;
	}
	
	@Override
	public String toString() {
		return toString0();
	}
	
	//modification
	public String2D concat(String2D append) {
		char[][] b = new char[this.buffer.length + append.buffer.length][];
		System.arraycopy(this.buffer, 0, b, 0, this.buffer.length);
		System.arraycopy(append.buffer, 0, b, this.buffer.length, append.buffer.length);
		return new String2D(b);
	}
}
