package space.engine.observable;

import space.engine.barrier.Barrier;
import space.engine.barrier.DelayTask;
import space.engine.event.EventEntry;

import static space.engine.barrier.Barrier.when;

public class GeneratingObservableReference<T> extends ObservableReference<T> {
	
	public interface Generator1Input<T, I1> {
		
		T get(I1 i1, T previous) throws NoUpdate, DelayTask;
	}
	
	public static <T, I1> GeneratingObservableReference<T> create(ObservableReference<I1> r1, Generator1Input<T, I1> generator) {
		class S implements Generator<T> {
			
			final GeneratingObservableReference<T> reference;
			
			volatile boolean updateEnabled;
			volatile I1 i1;
			
			public S() {
				reference = new GeneratingObservableReference<>();
				
				Barrier hooksAdded = when(
						r1.addHook(new EventEntry<>(o -> {
							i1 = o;
							update();
						}))
				);
				
				//only the initial value has additional barriers; usually you shouldn't use them as they block #ordering but here it's fine
				reference.ordering.nextInbetween(prev -> when(prev, hooksAdded).thenStart(() -> {
					updateEnabled = true;
					return reference.setInternalAlways(this);
				}));
			}
			
			public void update() {
				if (updateEnabled)
					reference.set(this);
			}
			
			@Override
			public T get(T previous) throws NoUpdate, DelayTask {
				return generator.get(i1, previous);
			}
		}
		return new S().reference;
	}
	
	public interface Generator2Input<T, I1, I2> {
		
		T get(I1 i1, I2 i2, T previous) throws NoUpdate, DelayTask;
	}
	
	public static <T, I1, I2> GeneratingObservableReference<T> create(ObservableReference<I1> r1, ObservableReference<I2> r2, Generator2Input<T, I1, I2> generator) {
		class S implements Generator<T> {
			
			final GeneratingObservableReference<T> reference;
			
			volatile boolean updateEnabled;
			volatile I1 i1;
			volatile I2 i2;
			
			public S() {
				reference = new GeneratingObservableReference<>();
				
				Barrier hooksAdded = when(
						r1.addHook(new EventEntry<>(o -> {
							i1 = o;
							update();
						})),
						r2.addHook(new EventEntry<>(o -> {
							i2 = o;
							update();
						}))
				);
				
				//only the initial value has additional barriers; usually you shouldn't use them as they block #ordering but here it's fine
				reference.ordering.nextInbetween(prev -> when(prev, hooksAdded).thenStart(() -> {
					updateEnabled = true;
					return reference.setInternalAlways(this);
				}));
			}
			
			public void update() {
				if (updateEnabled)
					reference.set(this);
			}
			
			@Override
			public T get(T previous) throws NoUpdate, DelayTask {
				return generator.get(i1, i2, previous);
			}
		}
		return new S().reference;
	}
	
	public interface Generator3Input<T, I1, I2, I3> {
		
		T get(I1 i1, I2 i2, I3 i3, T previous) throws NoUpdate, DelayTask;
	}
	
	public static <T, I1, I2, I3> GeneratingObservableReference<T> create(ObservableReference<I1> r1, ObservableReference<I2> r2, ObservableReference<I3> r3, Generator3Input<T, I1, I2, I3> generator) {
		class S implements Generator<T> {
			
			final GeneratingObservableReference<T> reference;
			
			volatile boolean updateEnabled;
			volatile I1 i1;
			volatile I2 i2;
			volatile I3 i3;
			
			public S() {
				reference = new GeneratingObservableReference<>();
				
				Barrier hooksAdded = when(
						r1.addHook(new EventEntry<>(o -> {
							i1 = o;
							update();
						})),
						r2.addHook(new EventEntry<>(o -> {
							i2 = o;
							update();
						})),
						r3.addHook(new EventEntry<>(o -> {
							i3 = o;
							update();
						}))
				);
				
				//only the initial value has additional barriers; usually you shouldn't use them as they block #ordering but here it's fine
				reference.ordering.nextInbetween(prev -> when(prev, hooksAdded).thenStart(() -> {
					updateEnabled = true;
					return reference.setInternalAlways(this);
				}));
			}
			
			public void update() {
				if (updateEnabled)
					reference.set(this);
			}
			
			@Override
			public T get(T previous) throws NoUpdate, DelayTask {
				return generator.get(i1, i2, i3, previous);
			}
		}
		return new S().reference;
	}
	
	public interface Generator4Input<T, I1, I2, I3, I4> {
		
		T get(I1 i1, I2 i2, I3 i3, I4 i4, T previous) throws NoUpdate, DelayTask;
	}
	
	public static <T, I1, I2, I3, I4> GeneratingObservableReference<T> create(ObservableReference<I1> r1, ObservableReference<I2> r2, ObservableReference<I3> r3, ObservableReference<I4> r4, Generator4Input<T, I1, I2, I3, I4> generator) {
		class S implements Generator<T> {
			
			final GeneratingObservableReference<T> reference;
			
			volatile boolean updateEnabled;
			volatile I1 i1;
			volatile I2 i2;
			volatile I3 i3;
			volatile I4 i4;
			
			public S() {
				reference = new GeneratingObservableReference<>();
				
				Barrier hooksAdded = when(
						r1.addHook(new EventEntry<>(o -> {
							i1 = o;
							update();
						})),
						r2.addHook(new EventEntry<>(o -> {
							i2 = o;
							update();
						})),
						r3.addHook(new EventEntry<>(o -> {
							i3 = o;
							update();
						})),
						r4.addHook(new EventEntry<>(o -> {
							i4 = o;
							update();
						}))
				);
				
				//only the initial value has additional barriers; usually you shouldn't use them as they block #ordering but here it's fine
				reference.ordering.nextInbetween(prev -> when(prev, hooksAdded).thenStart(() -> {
					updateEnabled = true;
					return reference.setInternalAlways(this);
				}));
			}
			
			public void update() {
				if (updateEnabled)
					reference.set(this);
			}
			
			@Override
			public T get(T previous) throws NoUpdate, DelayTask {
				return generator.get(i1, i2, i3, i4, previous);
			}
		}
		return new S().reference;
	}
	
	public interface Generator5Input<T, I1, I2, I3, I4, I5> {
		
		T get(I1 i1, I2 i2, I3 i3, I4 i4, I5 i5, T previous) throws NoUpdate, DelayTask;
	}
	
	public static <T, I1, I2, I3, I4, I5> GeneratingObservableReference<T> create(ObservableReference<I1> r1, ObservableReference<I2> r2, ObservableReference<I3> r3, ObservableReference<I4> r4, ObservableReference<I5> r5, Generator5Input<T, I1, I2, I3, I4, I5> generator) {
		class S implements Generator<T> {
			
			final GeneratingObservableReference<T> reference;
			
			volatile boolean updateEnabled;
			volatile I1 i1;
			volatile I2 i2;
			volatile I3 i3;
			volatile I4 i4;
			volatile I5 i5;
			
			public S() {
				reference = new GeneratingObservableReference<>();
				
				Barrier hooksAdded = when(
						r1.addHook(new EventEntry<>(o -> {
							i1 = o;
							update();
						})),
						r2.addHook(new EventEntry<>(o -> {
							i2 = o;
							update();
						})),
						r3.addHook(new EventEntry<>(o -> {
							i3 = o;
							update();
						})),
						r4.addHook(new EventEntry<>(o -> {
							i4 = o;
							update();
						})),
						r5.addHook(new EventEntry<>(o -> {
							i5 = o;
							update();
						}))
				);
				
				//only the initial value has additional barriers; usually you shouldn't use them as they block #ordering but here it's fine
				reference.ordering.nextInbetween(prev -> when(prev, hooksAdded).thenStart(() -> {
					updateEnabled = true;
					return reference.setInternalAlways(this);
				}));
			}
			
			public void update() {
				if (updateEnabled)
					reference.set(this);
			}
			
			@Override
			public T get(T previous) throws NoUpdate, DelayTask {
				return generator.get(i1, i2, i3, i4, i5, previous);
			}
		}
		return new S().reference;
	}
	
	//object
	protected GeneratingObservableReference() {
	}
	
	protected Barrier set(Generator<T> supplier) {
		return ordering.next(prev -> prev.thenStartCancelable(
				canceledCheck -> setInternalMayCancel(supplier, canceledCheck)
		));
	}
}
