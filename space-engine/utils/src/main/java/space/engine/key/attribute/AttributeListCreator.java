package space.engine.key.attribute;

import org.jetbrains.annotations.NotNull;
import space.engine.key.KeyGenerator;
import space.engine.key.attribute.AttributeKey.AttributeKeyNormal;
import space.engine.key.attribute.AttributeKey.AttributeKeyWithDefaultValue;
import space.engine.key.attribute.AttributeKey.AttributeKeyWithInitialValue;

import java.util.function.Supplier;

public class AttributeListCreator<TYPE> extends KeyGenerator<AttributeKey<?>> {
	
	public static final Object DEFAULT = new Object() {
		@Override
		public String toString() {
			return "DEF";
		}
	};
	public static final Object UNCHANGED = new Object() {
		@Override
		public String toString() {
			return "UNCH";
		}
	};
	
	public AttributeListCreator() {
		super(AttributeKey.class);
	}
	
	//create lists
	public @NotNull AttributeList<TYPE> create() {
		return new AttributeList<>(this);
	}
	
	public @NotNull AttributeListModify<TYPE> createModify() {
		return new AttributeListModify<>(new AttributeList<>(this));
	}
	
	//create keys
	public <V> AttributeKey<V> createKeyNormal() {
		return new AttributeKeyNormal<>(this);
	}
	
	public <V> AttributeKey<V> createKeyWithDefault(V defaultValue) {
		return new AttributeKeyWithDefaultValue<>(this, defaultValue);
	}
	
	public <V> AttributeKey<V> createKeyWithInitial(Supplier<V> initialValue) {
		return new AttributeKeyWithInitialValue<>(this, initialValue);
	}
}
