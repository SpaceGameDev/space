package space.engine.key.attribute;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.barrier.Barrier;
import space.engine.barrier.lock.SyncLock;
import space.engine.indexmap.IndexMap;
import space.engine.indexmap.IndexMapArray;

import java.util.List;
import java.util.stream.Collectors;

import static space.engine.barrier.Barrier.*;
import static space.engine.key.attribute.AttributeListCreator.UNCHANGED;

public class AttributeListModify<TYPE> extends AbstractAttributeList<TYPE> {
	
	private AttributeList<TYPE> list;
	
	protected AttributeListModify(AttributeList<TYPE> list) {
		this(list, new IndexMapArray<>(UNCHANGED));
	}
	
	protected AttributeListModify(AttributeList<TYPE> list, IndexMap<@Nullable Object> indexMap) {
		super(indexMap);
		this.list = list;
	}
	
	//getter
	@Override
	public @NotNull AttributeListCreator<TYPE> creator() {
		return list.creator;
	}
	
	public AttributeList<TYPE> list() {
		return list;
	}
	
	//direct
	@Override
	public <V> @Nullable Object getDirect(@NotNull AttributeKey<V> key) {
		Object direct = super.getDirect(key);
		return direct == UNCHANGED ? list.getDirect(key) : direct;
	}
	
	@Override
	public <V> @Nullable Object putDirect(@NotNull AttributeKey<V> key, @Nullable Object value) {
		Object direct = super.putDirect(key, value);
		return direct == UNCHANGED ? list.getDirect(key) : direct;
	}
	
	//get
	public <V> V getOld(@NotNull AttributeKey<V> key) {
		return list.get(key);
	}
	
	public <V> @Nullable Object getOldDirect(@NotNull AttributeKey<V> key) {
		return list.getDirect(key);
	}
	
	public <V> @Nullable Object getModDirect(@NotNull AttributeKey<V> key) {
		verifyKey(key);
		return indexMap.get(key.id);
	}
	
	public <V> boolean isUnchanged(@NotNull AttributeKey<V> key) {
		verifyKey(key);
		return indexMap.get(key.id) == UNCHANGED;
	}
	
	public <V> boolean hasChanged(@NotNull AttributeKey<V> key) {
		verifyKey(key);
		return indexMap.get(key.id) != UNCHANGED;
	}
	
	//put
	public <V> void put(@NotNull AttributeKey<V> key, @Nullable V value) {
		verifyKey(key);
		if (value == UNCHANGED)
			indexMap.put(key.id, UNCHANGED);
		else
			key.attributeListPut(this, value);
	}
	
	public <V> V getAndPut(@NotNull AttributeKey<V> key, @Nullable V value) {
		verifyKey(key);
		if (value == UNCHANGED) {
			putDirect(key, UNCHANGED);
			return key.attributeListGet(this);
		} else {
			return key.attributeListGetAndPut(this, value);
		}
	}
	
	public void putAll(AbstractAttributeList list, @NotNull AttributeKey<?>... keys) {
		for (AttributeKey<?> key : keys) {
			verifyKey(key);
			indexMap.put(key.id, list.getDirect(key));
		}
	}
	
	public <V> void reset(@NotNull AttributeKey<V> key) {
		verifyKey(key);
		indexMap.put(key.id, UNCHANGED);
	}
	
	public void resetAll() {
		indexMap.clear();
	}
	
	//apply
	public Barrier apply() {
		return nowLock(new SyncLock[] {list}, () -> {
			List<AttributeKey<?>> changes =
					list.creator.getKeys()
								.stream()
								.limit(indexMap.size()) //limit to the max index which was set
								.filter(this::hasChanged)
								.collect(Collectors.toList());
			Barrier changeEventTask = list.changeEvent.submit(callback -> callback.onChange(this, changes));
			return changeEventTask.thenStart(() -> {
				indexMap.entrySet()
						.stream()
						.filter(entry -> entry.getValue() != UNCHANGED)
						.forEach(entry -> list.indexMap.put(entry.getIndex(), entry.getValue()));
				return done();
			});
		});
	}
	
	public @NotNull AttributeList<TYPE> createNewAttributeList() {
		AttributeList<TYPE> list = new AttributeList<>(this.list.creator);
		for (AttributeKey<?> key : this.list.creator.getKeys()) {
			list.putDirect(key, getDirect(key));
		}
		return list;
	}
	
	public @NotNull AttributeListModify<TYPE> copyModify() {
		return rebaseOnto(list);
	}
	
	public @NotNull AttributeListModify<TYPE> rebaseOnto(AttributeList<TYPE> list) {
		return new AttributeListModify<>(list, new IndexMapArray<>(UNCHANGED, indexMap));
	}
}
