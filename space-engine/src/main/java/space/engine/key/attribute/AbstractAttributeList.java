package space.engine.key.attribute;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.baseobject.ToString;
import space.engine.indexmap.IndexMap;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

public abstract class AbstractAttributeList<TYPE> implements ToString {
	
	protected IndexMap<@Nullable Object> indexMap;
	
	protected AbstractAttributeList(IndexMap<@Nullable Object> indexMap) {
		this.indexMap = indexMap;
	}
	
	//abstract
	public abstract @NotNull AttributeListCreator<TYPE> creator();
	
	//direct access
	public <V> @Nullable Object getDirect(@NotNull AttributeKey<V> key) {
		verifyKey(key);
		return indexMap.get(key.id);
	}
	
	public <V> @Nullable Object putDirect(@NotNull AttributeKey<V> key, @Nullable Object value) {
		verifyKey(key);
		return indexMap.put(key.id, value);
	}
	
	//get
	public <V> V get(@NotNull AttributeKey<V> key) {
		verifyKey(key);
		return key.attributeListGet(this);
	}
	
	//other
	public void verifyKey(AttributeKey<?> key) {
		creator().assertKeyOf(key);
	}
	
	public int size() {
		return indexMap.size();
	}
	
	//toString
	@NotNull
	@Override
	public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
		ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
		tsh.add("indexMap", indexMap);
		tsh.add("creator", creator());
		return tsh.build();
	}
	
	@Override
	public String toString() {
		return toString0();
	}
	
}
