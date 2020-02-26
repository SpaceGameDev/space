package space.engine.plugin.modengine;

public class ClassnameUtils {
	
	public static String toValidJavaClassIdentifier(String input) {
		StringBuilder b = new StringBuilder(input);
		
		//start
		for (char first = b.charAt(0); !Character.isJavaIdentifierStart(first); first = b.charAt(0))
			b.delete(0, 1);
		b.replace(0, 1, String.valueOf(Character.toUpperCase(b.charAt(0))));
		
		//content
		for (int i = 1; i < b.length(); i++) {
			char c = b.charAt(i);
			
			if (!Character.isLetterOrDigit(c)) {
				b.replace(i, i + 2, String.valueOf(Character.toUpperCase(b.charAt(i + 1))));
				i--;
			} else if (!Character.isJavaIdentifierPart(c)) {
				b.delete(i, i + 1);
				i--;
			}
		}
		return b.toString();
	}
	
	@SuppressWarnings("StatementWithEmptyBody")
	public static String toValidJavaPackageIdentifier(String input) {
		StringBuilder b = new StringBuilder(input);
		
		//start
		for (char first = b.charAt(0); !Character.isJavaIdentifierStart(first); first = b.charAt(0))
			b.delete(0, 1);
		b.replace(0, 1, String.valueOf(Character.toLowerCase(b.charAt(0))));
		
		//content
		for (int i = 1; i < b.length(); i++) {
			char c = b.charAt(i);
			
			if (c == '.') {
				//skip
			} else if (c == '/' || c == '\\') {
				b.replace(i, i + 1, ".");
			} else if (!Character.isLetterOrDigit(c)) {
				b.replace(i, i + 1, ".");
				i--;
			} else if (!Character.isJavaIdentifierPart(c)) {
				b.delete(i, i + 1);
				i--;
			}
		}
		return b.toString();
	}
}
