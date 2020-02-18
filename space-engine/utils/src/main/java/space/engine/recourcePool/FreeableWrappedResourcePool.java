package space.engine.recourcePool;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.barrier.Barrier;
import space.engine.freeable.Cleaner;
import space.engine.freeable.Freeable;

import java.util.function.BiFunction;
import java.util.function.Consumer;

import static space.engine.barrier.Barrier.DONE_BARRIER;

/**
 * {@link FreeableWrappedResourcePool} uses a ResourcePool and wraps allocated resources in another {@link space.engine.freeable.Freeable} for easy releasing by calling {@link Freeable#free()}
 */
public abstract class FreeableWrappedResourcePool<I, O extends Freeable> implements FreeableResourcePool<O> {
	
	//static
	public static <I, O extends Freeable> FreeableWrappedResourcePool<I, O> withLamdba(@NotNull ResourcePool<I> resourcePool, @NotNull WrapFunction<I, O> wrap, Consumer<I> reset) {
		return new FreeableWrappedResourcePool<>(resourcePool) {
			@NotNull
			@Override
			public O wrap(@NotNull I inner, @NotNull BiFunction<? super Object, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
				return wrap.wrap(inner, storageCreator, parents);
			}
			
			@Override
			public void reset(@NotNull I inner) {
				reset.accept(inner);
			}
		};
	}
	
	@FunctionalInterface
	public interface WrapFunction<I, O extends Freeable> {
		
		@NotNull O wrap(@NotNull I inner, @NotNull BiFunction<? super Object, Object[], Freeable> storageCreator, @NotNull Object[] parents);
	}
	
	//object
	private final ResourcePool<I> resourcePool;
	
	public FreeableWrappedResourcePool(ResourcePool<I> resourcePool) {
		this.resourcePool = resourcePool;
	}
	
	public abstract @NotNull O wrap(@NotNull I inner, @NotNull BiFunction<? super Object, Object[], Freeable> storageCreator, @NotNull Object[] parents);
	
	public abstract void reset(@NotNull I inner);
	
	@Override
	public O allocateParents(Object[] parents) {
		I inner = resourcePool.allocate();
		return wrap(inner, storageCreator(inner), parents);
	}
	
	@Override
	public O[] allocateParents(O[] os, Object[] parents) {
		//noinspection unchecked
		I[] inners = resourcePool.allocate((I[]) new Object[os.length]);
		for (int i = 0; i < inners.length; i++) {
			I inner = inners[i];
			os[i] = wrap(inner, storageCreator(inner), parents);
		}
		return os;
	}
	
	private @NotNull BiFunction<? super Object, Object[], Freeable> storageCreator(@NotNull I inner) {
		return (outer, objects) -> new OuterStorage(outer, inner, objects);
	}
	
	private class OuterStorage extends Cleaner {
		
		private final @NotNull I inner;
		
		public OuterStorage(@Nullable Object outer, @NotNull I inner, @NotNull Object[] parents) {
			super(outer, parents);
			this.inner = inner;
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			reset(inner);
			resourcePool.release(inner);
			return DONE_BARRIER;
		}
	}
}
