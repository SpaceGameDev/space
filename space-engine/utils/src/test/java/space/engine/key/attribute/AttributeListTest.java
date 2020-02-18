package space.engine.key.attribute;

import org.junit.Test;
import space.engine.event.EventEntry;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;

public class AttributeListTest {
	
	AttributeListCreator<AttributeListTest> creator = new AttributeListCreator<>();
	AttributeList<AttributeListTest> list = creator.create();
	
	@Test
	public void testSequentialIdGeneration() {
		int testSize = 5;
		
		AttributeKey<?>[] keys = IntStream.range(0, testSize).mapToObj(i -> creator.createKeyNormal()).toArray(AttributeKey[]::new);
		for (int i = 0; i < testSize; i++)
			assertEquals("Key not sequential", i, keys[i].id);
		
		assertThat(creator.getKeys().stream().filter(Objects::nonNull).collect(Collectors.toList()), contains(keys));
	}
	
	@Test
	public void testDefaultValues() {
		AttributeList<AttributeListTest> list2 = creator.create();
		
		AttributeKey<Object> keyNormal = creator.createKeyNormal();
		assertNull(list.get(keyNormal));
		assertNull(list2.get(keyNormal));
		
		Object obj = new Object();
		AttributeKey<Object> keyDefault = creator.createKeyWithDefault(obj);
		assertEquals(obj, list.get(keyDefault));
		assertEquals(obj, list2.get(keyDefault));
		
		AttributeKey<Object> keyWithInitial = creator.createKeyWithInitial(Object::new);
		assertThat(list.get(keyWithInitial), instanceOf(Object.class));
		assertThat(list2.get(keyWithInitial), instanceOf(Object.class));
		assertNotEquals(list.get(keyWithInitial), list2.get(keyWithInitial));
	}
	
	@Test
	public void testModify() throws InterruptedException {
		AttributeKey<String> key = creator.createKeyWithInitial(() -> "default");
		
		AttributeListModify<AttributeListTest> modify = list.createModify();
		modify.put(key, "change");
		assertEquals(list.get(key), "default");
		assertEquals(modify.get(key), "change");
		
		modify.apply().await();
		assertEquals(list.get(key), "change");
		assertEquals(modify.get(key), "change");
	}
	
	@Test
	public void testModifyMulitpleOverriding() throws InterruptedException {
		AttributeKey<String> key1 = creator.createKeyWithInitial(() -> "default");
		AttributeKey<String> key2 = creator.createKeyWithInitial(() -> "default");
		
		AttributeListModify<AttributeListTest> modify1 = list.createModify();
		modify1.put(key1, "to1");
		modify1.put(key2, "to1");
		AttributeListModify<AttributeListTest> modify2 = list.createModify();
		modify2.put(key1, "to2");
		
		assertEquals(list.get(key1), "default");
		assertEquals(list.get(key2), "default");
		assertEquals(modify1.get(key1), "to1");
		assertEquals(modify1.get(key2), "to1");
		assertEquals(modify2.get(key1), "to2");
		assertEquals(modify2.get(key2), "default");
		
		modify1.apply().await();
		assertEquals(list.get(key1), "to1");
		assertEquals(list.get(key2), "to1");
		assertEquals(modify2.get(key1), "to2");
		assertEquals(modify2.get(key2), "to1");
		
		modify2.apply().await();
		assertEquals(list.get(key1), "to2");
		assertEquals(list.get(key2), "to1");
	}
	
	@Test
	public void testModifyEvent() throws InterruptedException {
		AttributeKey<String> keyChanged = creator.createKeyWithInitial(() -> "default");
		AttributeKey<String> keyOther = creator.createKeyWithInitial(() -> "unchanged");
		
		Runnable[] run = new Runnable[1];
		list.getChangeEvent().addHook(new EventEntry<>((attributeListModify, attributeKeys) -> {
			run[0] = () -> {
				assertThat(attributeKeys, contains(keyChanged));
				assertEquals(attributeListModify.get(keyChanged), "change");
				assertEquals(attributeListModify.get(keyOther), "unchanged");
			};
		}));
		
		AttributeListModify<AttributeListTest> modify = list.createModify();
		modify.put(keyChanged, "change");
		modify.apply().await();
		
		assertNotNull(run[0]);
		run[0].run();
	}
	
	@Test
	public void testModifyEventOverriding() throws InterruptedException {
		AttributeKey<String> key = creator.createKeyNormal();
		
		list.getChangeEvent().addHook(new EventEntry<>((modify, attributeKeys) -> {
			modify.put(key, "override");
		}));
		
		AttributeListModify<AttributeListTest> modify = list.createModify();
		modify.put(key, "change");
		modify.apply().await();
		
		assertEquals(list.get(key), "override");
	}
}
