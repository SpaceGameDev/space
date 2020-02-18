package space.engine.key.attribute;

import org.jetbrains.annotations.Nullable;
import space.engine.key.Key;

import java.util.function.Supplier;

import static space.engine.key.attribute.AttributeListCreator.DEFAULT;

public abstract class AttributeKey<V> extends Key<V> {
	
	protected AttributeKey(AttributeListCreator<?> gen) {
		super(gen);
	}
	
	public abstract V attributeListGet(AbstractAttributeList<?> attributeList);
	
	public abstract void attributeListPut(AbstractAttributeList<?> attributeList, @Nullable V value);
	
	public abstract V attributeListGetAndPut(AbstractAttributeList<?> attributeList, @Nullable V value);
	
	public static abstract class AbstractAttributeKey<V> extends AttributeKey<V> {
		
		public AbstractAttributeKey(AttributeListCreator<?> gen) {
			super(gen);
		}
		
		public V attributeListGet(AbstractAttributeList<?> attributeList) {
			return correctDefault(attributeList.getDirect(this));
		}
		
		public void attributeListPut(AbstractAttributeList<?> attributeList, @Nullable V value) {
			attributeList.putDirect(this, value);
		}
		
		public V attributeListGetAndPut(AbstractAttributeList<?> attributeList, @Nullable V value) {
			return correctDefault(attributeList.putDirect(this, value));
		}
		
		protected abstract V correctDefault(@Nullable Object direct);
	}
	
	public static class AttributeKeyNormal<V> extends AbstractAttributeKey<V> {
		
		public AttributeKeyNormal(AttributeListCreator<?> gen) {
			super(gen);
		}
		
		@SuppressWarnings({"ConstantConditions", "unchecked"})
		protected V correctDefault(@Nullable Object direct) {
			return direct == DEFAULT ? null : (V) direct;
		}
	}
	
	/**
	 * If the direct value is {@link AttributeListCreator#DEFAULT}, the {@link #defaultValue} is returned.
	 */
	public static class AttributeKeyWithDefaultValue<V> extends AbstractAttributeKey<V> {
		
		public V defaultValue;
		
		public AttributeKeyWithDefaultValue(AttributeListCreator<?> gen, V defaultValue) {
			super(gen);
			this.defaultValue = defaultValue;
		}
		
		@Override
		@SuppressWarnings({"ConstantConditions", "unchecked"})
		protected V correctDefault(@Nullable Object direct) {
			return direct == DEFAULT ? defaultValue : (V) direct;
		}
	}
	
	public static class AttributeKeyWithInitialValue<V> extends AttributeKey<V> {
		
		public Supplier<V> initialValue;
		
		public AttributeKeyWithInitialValue(AttributeListCreator<?> gen, Supplier<V> initialValue) {
			super(gen);
			this.initialValue = initialValue;
		}
		
		@Override
		public V attributeListGet(AbstractAttributeList<?> attributeList) {
			return correctDefault(attributeList, attributeList.getDirect(this));
		}
		
		@Override
		public void attributeListPut(AbstractAttributeList<?> attributeList, @Nullable V value) {
			attributeList.putDirect(this, value);
		}
		
		@Override
		public V attributeListGetAndPut(AbstractAttributeList<?> attributeList, @Nullable V value) {
			return correctDefault(attributeList, attributeList.putDirect(this, value));
		}
		
		private V correctDefault(AbstractAttributeList<?> attributeList, @Nullable Object direct) {
			if (direct != DEFAULT) {
				//noinspection ConstantConditions,unchecked
				return (V) direct;
			}
			
			V initial = initialValue.get();
			attributeList.putDirect(this, initial);
			return initial;
		}
	}
	
}
