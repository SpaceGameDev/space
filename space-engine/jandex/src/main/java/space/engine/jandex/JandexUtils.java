package space.engine.jandex;

import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;

import java.io.IOException;
import java.io.InputStream;

public class JandexUtils {
	
	public static Index readIndex(Class<?> folder, String name) throws IOException {
		try (InputStream in = folder.getResourceAsStream(name)) {
			return new IndexReader(in).read();
		}
	}
	
	public static Index readIndexAssertExists(Class<?> folder, String name) {
			try (InputStream in = folder.getResourceAsStream(name)) {
				return new IndexReader(in).read();
		} catch (IOException e) {
			throw new AssertionError("File has to exist and be readable", e);
		}
	}
	
}
