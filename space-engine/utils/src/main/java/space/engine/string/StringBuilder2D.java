package space.engine.string;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.ArrayUtils;
import space.engine.delegate.specific.CharArrayStringIterable;
import space.engine.string.toStringHelper.ToStringHelper;

import java.util.Arrays;
import java.util.Iterator;

public class StringBuilder2D implements Iterable<String>, CharSequence2D {
	
	public static int defaultCapacity = 16;
	public static int defaultHeight = 1;
	public static int expandShiftHeight = 1;
	public static int expandShift = 1;
	
	public static char[][] BEINGEDITED = new char[][] {"Being Edited!".toCharArray()};
	
	public char[][] buffer;
	public int[] length;
	public int height;
	public int maxX;
	public char fillMissingSpaces = ' ';
	
	public int posX;
	public int posY;
	
	public boolean isBuild = false;
	/**
	 * beingEdited is a non-secure lock JUST for easier debugging, so the debugger calling toString() cannot modify the object (see the trimAll() Method inside toString())
	 */
	public int beingEdited = 0;
	
	public StringBuilder2D() {
		this(defaultHeight, defaultCapacity);
	}
	
	public StringBuilder2D(int sizeHeight, int sizeCapacity) {
		this(new char[sizeHeight][sizeCapacity], false);
	}
	
	public StringBuilder2D(char[][] buffer) {
		this(buffer, true);
	}
	
	public StringBuilder2D(char[][] buffer, boolean isFilled) {
		this(buffer, isFilled ? calcLengths(buffer) : new int[buffer.length], isFilled ? buffer.length : 0);
	}
	
	public StringBuilder2D(char[][] buffer, int[] length, int height) {
		if (buffer.length != length.length)
			throw new IllegalArgumentException();
		
		this.buffer = buffer;
		this.length = length;
		this.height = height;
	}
	
	public static int[] calcLengths(char[][] buffer) {
		int l = buffer.length;
		int[] ret = new int[l];
		for (int i = 0; i < l; i++)
			ret[i] = buffer[i].length;
		return ret;
	}
	
	//fillMissingSpaces
	public StringBuilder2D setFillMissingSpaces(char fillMissingSpaces) {
		this.fillMissingSpaces = fillMissingSpaces;
		return this;
	}
	
	public StringBuilder2D setNoFillMissingSpaces() {
		this.fillMissingSpaces = 0;
		return this;
	}
	
	//edit start end
	public void startEdit() {
		beingEdited++;
	}
	
	public void endEdit() {
		isBuild = false;
		beingEdited--;
	}
	
	//ensure height
	public boolean ensureHeightIndex(int index) {
		return ensureHeight(index + 1);
	}
	
	public boolean ensureHeight(int capa) {
		int l = buffer.length;
		if (l < capa) {
			startEdit();
			int newsize = ArrayUtils.getOptimalArraySizeExpansion(l, capa, expandShiftHeight);
			
			char[][] old = buffer;
			buffer = new char[newsize][];
			System.arraycopy(old, 0, buffer, 0, l);
			
			int[] oldl = length;
			length = new int[newsize];
			System.arraycopy(oldl, 0, length, 0, l);
			
			endEdit();
			return true;
		}
		return false;
	}
	
	//ensureCapacity
	public boolean ensureCapacityIndex(int index) {
		return ensureCapacity(posY, index + 1);
	}
	
	public boolean ensureCapacity(int capa) {
		return ensureCapacity(posY, capa);
	}
	
	public boolean ensureCapacityIndex(int height, int index) {
		return ensureCapacity(height, index + 1);
	}
	
	public boolean ensureCapacity(int height, int capa) {
		char[] buf = buffer[height];
		
		if (buf == null) {
			startEdit();
			buffer[height] = new char[ArrayUtils.getOptimalArraySizeStart(defaultCapacity, capa)];
			endEdit();
			return true;
		}
		
		int l = buf.length;
		if (l < capa) {
			startEdit();
			char[] n = new char[ArrayUtils.getOptimalArraySizeExpansion(l, capa, expandShift)];
			System.arraycopy(buf, 0, n, 0, l);
			buffer[height] = n;
			endEdit();
			return true;
		}
		
		return false;
	}
	
	//size
	@Override
	public int height() {
		return height;
	}
	
	@Override
	public int length(int h) {
		return length[h];
	}
	
	//append native
	public StringBuilder2D append(byte v) {
		return append(ToStringHelper.getDefault().toString(v));
	}
	
	public StringBuilder2D append(short v) {
		return append(ToStringHelper.getDefault().toString(v));
	}
	
	public StringBuilder2D append(int v) {
		return append(ToStringHelper.getDefault().toString(v));
	}
	
	public StringBuilder2D append(long v) {
		return append(ToStringHelper.getDefault().toString(v));
	}
	
	public StringBuilder2D append(float v) {
		return append(ToStringHelper.getDefault().toString(v));
	}
	
	public StringBuilder2D append(double v) {
		return append(ToStringHelper.getDefault().toString(v));
	}
	
	//append objects
	@NotNull
	public StringBuilder2D append(@Nullable Object obj) {
		if (obj == null)
			return append("null");
		
		Object o = ToStringHelper.getDefault().toString(obj);
		if (o instanceof ToString2D)
			return append(((ToString2D) o).toString2D());
		return append(o.toString());
	}
	
	//append strings
	@NotNull
	public StringBuilder2D append(char v) {
		int start = posX;
		posX = posX + 1;
		notifyYX();
		buffer[posY][start] = v;
		return this;
	}
	
	@NotNull
	public StringBuilder2D append(@NotNull char[] str) {
		startEdit();
		int l = str.length;
		int start = posX;
		posX += l;
		notifyYX();
		System.arraycopy(str, 0, buffer[posY], start, l);
		endEdit();
		return this;
	}
	
	@NotNull
	public StringBuilder2D append(@NotNull CharSequence2D b) {
		return append(b.getChars());
	}
	
	@NotNull
	public StringBuilder2D append(@NotNull String str) {
		startEdit();
		int l = str.length();
		int start = posX;
		posX = posX + l;
		notifyYX();
		str.getChars(0, l, buffer[posY], start);
		endEdit();
		return this;
	}
	
	//append string2Ds
	@NotNull
	public StringBuilder2D append(@NotNull char[][] chars) {
		startEdit();
		int absStartX = posX;
		int absCurrY = posY;
		
		posY = posY + height - 1;
		notifyY();
		
		for (int i = 0; i < chars.length; ) {
			char[] from = chars[i];
			int froml = from.length;
			
			posY = absCurrY;
			posX = absStartX + froml;
			notifyYX();
			char[] to = buffer[absCurrY];
			
			System.arraycopy(from, 0, to, absStartX, froml);
			
			i++;
			absCurrY++;
		}
		
		endEdit();
		return this;
	}
	
	public StringBuilder2D append(@NotNull CharSequence2D b, int untily, int untilx, char fillup) {
		return append(b.getChars(), untily, untilx, fillup);
	}
	
	@NotNull
	public StringBuilder2D append(@NotNull char[][] chars, int untily, int untilx, char fillup) {
		startEdit();
		int absCurrY = posY;
		int absStartX = posX;
		
		posY = untily - 1;
		notifyY();
		
		for (int i = 0; i < chars.length; ) {
			char[] from = chars[i];
			int froml = Math.min(from.length, untilx - absStartX);
			
			posY = absCurrY;
			posX = untilx;
			notifyYX();
			char[] to = buffer[absCurrY];
			
			System.arraycopy(from, 0, to, absStartX, froml);
			if (froml < untilx)
				Arrays.fill(to, absStartX + froml, untilx, fillup);
			
			i++;
			absCurrY++;
		}
		
		if (absCurrY < untily) {
			for (int i = absCurrY; i < untily; i++) {
				posY = i;
				posX = untilx;
				notifyYX();
				
				Arrays.fill(buffer[i], absStartX, untilx, fillup);
			}
		}
		
		endEdit();
		return this;
	}
	
	//pos
	public int getX() {
		return posX;
	}
	
	@NotNull
	public StringBuilder2D setX(int pos) {
		posX = pos;
		return this;
	}
	
	public void notifyX() {
		startEdit();
		ensureCapacity(posX);
		if (posX > length[posY]) {
			if (fillMissingSpaces != 0)
				Arrays.fill(buffer[posY], length[posY], posX, fillMissingSpaces);
			length[posY] = posX;
		}
		if (posX > maxX)
			maxX = posX;
		endEdit();
	}
	
	public int getY() {
		return posY;
	}
	
	@NotNull
	public StringBuilder2D setY(int pos) {
		posY = pos;
		return this;
	}
	
	public void notifyY() {
		startEdit();
		ensureHeightIndex(posY);
		if (posY + 1 > height)
			height = posY + 1;
		endEdit();
	}
	
	public void notifyYX() {
		startEdit();
		notifyY();
		notifyX();
		endEdit();
	}
	
	//pos max
	@Override
	public int maxLength() {
		return maxX;
	}
	
	//fill
	@NotNull
	public StringBuilder2D fill(int l, char c) {
		startEdit();
		int start = posX;
		posX = posX + l - 1;
		notifyYX();
		Arrays.fill(buffer[posY], start, posX, c);
		endEdit();
		return this;
	}
	
	//nextLine
	@NotNull
	public StringBuilder2D nextLine() {
		startEdit();
		posY = posY + 1;
		posX = 0;
		endEdit();
		return this;
	}
	
	//getters
	@NotNull
	@Override
	public char[][] getChars() {
		if (beingEdited != 0)
			return BEINGEDITED;
		if (!isBuild) {
			trimAll();
			isBuild = true;
		}
		return buffer;
	}
	
	@NotNull
	@Override
	public Iterator<String> iterator() {
		return new CharArrayStringIterable(getChars()).iterator();
	}
	
	public void trimAll() {
		trim();
		for (int i = 0; i < height; i++)
			if (!ensureCapacity(i, 0))
				trim(i);
	}
	
	public void trim() {
		if (buffer.length == height)
			return;
		
		char[][] old = buffer;
		buffer = new char[height][];
		System.arraycopy(old, 0, buffer, 0, height);
		
		int[] oldl = length;
		length = new int[height];
		System.arraycopy(oldl, 0, length, 0, height);
	}
	
	public void trim(int h) {
		char[] old = buffer[h];
		int l = length[h];
		
		if (old.length == l)
			return;
		
		char[] n = new char[l];
		System.arraycopy(old, 0, n, 0, l);
		buffer[h] = n;
	}
	
	@NotNull
	public String2D toString2D() {
		return new String2D(getChars(), maxX);
	}
	
	@Override
	public String toString() {
		return toString0();
	}
}
