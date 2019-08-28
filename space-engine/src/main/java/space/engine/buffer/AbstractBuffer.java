package space.engine.buffer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.barrier.Barrier;
import space.engine.baseobject.ToString;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

public abstract class AbstractBuffer extends Buffer implements FreeableWrapper, ToString {
	
	private final @NotNull Storage storage;
	
	protected AbstractBuffer(Allocator allocator, long address, @NotNull Object[] parents) {
		this.storage = new Storage(this, allocator, address, parents);
	}
	
	@Override
	public long address() {
		return storage.getAddress();
	}
	
	//storage
	@Override
	public @NotNull FreeableStorage getStorage() {
		return storage;
	}
	
	public static class Storage extends FreeableStorage {
		
		public final Allocator allocator;
		private final long address;
		
		public Storage(@Nullable Object referent, Allocator allocator, long address, @NotNull Object[] parents) {
			super(referent, parents);
			this.allocator = allocator;
			this.address = address;
		}
		
		public long getAddress() {
			throwIfFreed();
			return address;
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			allocator.free(address);
			return Barrier.DONE_BARRIER;
		}
	}
	
	//toString
	@Override
	@NotNull
	public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
		ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
		if (this.storage.isFreed()) {
			tsh.add("address", "freed");
		} else {
			tsh.add("address", this.storage.address);
			tsh.add("sizeOf", sizeOf());
		}
		return tsh.build();
	}
	
	@Override
	public String toString() {
		return toString0();
	}
}
