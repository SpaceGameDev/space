package space.engine;

import org.junit.After;
import org.junit.Before;

/**
 * @deprecated doesn't work with tests having timeout set because Junit spawns a separate Thread for executing these tests
 */
@Deprecated
public class SingleThreadPoolTest {
	
	@Before
	public void before() {
		Side.overrideThreadlocalPool(Runnable::run);
	}
	
	@After
	public void after() {
		Side.overrideThreadlocalPool(null);
	}
}
