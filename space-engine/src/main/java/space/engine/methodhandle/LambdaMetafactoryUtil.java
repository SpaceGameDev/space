package space.engine.methodhandle;

import space.engine.methodhandle.capsule.PublicLookup;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class LambdaMetafactoryUtil {
	
	/**
	 * the {@link MethodHandles#publicLookup()} does not work in {@link LambdaMetafactory}, so we have a lonely class in a package to solve this
	 */
	@SuppressWarnings("unused")
	public static final Lookup PUBLIC_LOOKUP = PublicLookup.lookup();
	
	//native
	@SuppressWarnings("unused")
	public static final MethodType MTVoid = MethodType.methodType(void.class);
	@SuppressWarnings("unused")
	public static final MethodType MTByte = MethodType.methodType(byte.class);
	@SuppressWarnings("unused")
	public static final MethodType MTShort = MethodType.methodType(short.class);
	@SuppressWarnings("unused")
	public static final MethodType MTInt = MethodType.methodType(int.class);
	@SuppressWarnings("unused")
	public static final MethodType MTLong = MethodType.methodType(long.class);
	@SuppressWarnings("unused")
	public static final MethodType MTFloat = MethodType.methodType(float.class);
	@SuppressWarnings("unused")
	public static final MethodType MTDouble = MethodType.methodType(double.class);
	@SuppressWarnings("unused")
	public static final MethodType MTBoolean = MethodType.methodType(boolean.class);
	@SuppressWarnings("unused")
	public static final MethodType MTChar = MethodType.methodType(char.class);
	
	//object
	@SuppressWarnings("unused")
	public static final MethodType MTObject = MethodType.methodType(Object.class);
	
	//lambda
	@SuppressWarnings("unused")
	public static final MethodType MTRunnable = MethodType.methodType(Runnable.class);
	@SuppressWarnings("unused")
	public static final MethodType MTSupplier = MethodType.methodType(Supplier.class);
	@SuppressWarnings("unused")
	public static final MethodType MTConsumer = MethodType.methodType(Consumer.class);
	@SuppressWarnings("unused")
	public static final MethodType MTFunction = MethodType.methodType(Function.class);
	@SuppressWarnings("unused")
	public static final MethodType MTUnaryOperator = MethodType.methodType(UnaryOperator.class);
	@SuppressWarnings("unused")
	public static final MethodType MTPredicate = MethodType.methodType(Predicate.class);
	
	//constructor
	@SuppressWarnings("unchecked")
	public static <T> Supplier<T> metafactoryConstructor(Lookup caller, Class<T> clazz) throws Throwable {
		return (Supplier<T>) metafactory(caller, MTSupplier, "get", MTObject, caller.findConstructor(clazz, MTVoid), MethodType.methodType(clazz));
	}
	
	//methods
	public static Runnable metafactoryRunnableStatic(Lookup caller, Class<?> clazz, String methodName) throws Throwable {
		return metafactoryRunnable(caller, caller.findStatic(clazz, methodName, MTVoid));
	}
	
	public static Runnable metafactoryRunnableVirtual(Lookup caller, Class<?> clazz, String methodName) throws Throwable {
		return metafactoryRunnable(caller, caller.findVirtual(clazz, methodName, MTVoid));
	}
	
	public static <T> Supplier<T> metafactorySupplierStatic(Lookup caller, Class<?> clazz, String methodName, Class<T> returnType) throws Throwable {
		return metafactorySupplier(caller, caller.findStatic(clazz, methodName, MethodType.methodType(returnType)), returnType);
	}
	
	public static <T> Supplier<T> metafactorySupplierVirtual(Lookup caller, Class<?> clazz, String methodName, Class<T> returnType) throws Throwable {
		return metafactorySupplier(caller, caller.findVirtual(clazz, methodName, MethodType.methodType(returnType)), returnType);
	}
	
	public static <T> Predicate<T> metafactoryPredicateStatic(Lookup caller, Class<?> clazz, String methodName) throws Throwable {
		return metafactoryPredicate(caller, caller.findStatic(clazz, methodName, MTBoolean));
	}
	
	public static <T> Predicate<T> metafactoryPredicateVirtual(Lookup caller, Class<?> clazz, String methodName) throws Throwable {
		return metafactoryPredicate(caller, caller.findVirtual(clazz, methodName, MTBoolean));
	}
	
	//lambda wrappers
	public static Runnable metafactoryRunnable(Lookup caller, MethodHandle invokedMethod) throws Throwable {
		return (Runnable) metafactory(caller, MTRunnable, "run", MTVoid, invokedMethod, MTVoid);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Supplier<T> metafactorySupplier(Lookup caller, MethodHandle invokedMethod, Class<T> invokedTypeReturn) throws Throwable {
		return (Supplier<T>) metafactory(caller, MTSupplier, "get", MTObject, invokedMethod, MethodType.methodType(invokedTypeReturn));
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Consumer<T> metafactoryConsumer(Lookup caller, MethodHandle invokedMethod) throws Throwable {
		return (Consumer<T>) metafactory(caller, MTConsumer, "accept", MTVoid, invokedMethod, MTVoid);
	}
	
	@SuppressWarnings("unchecked")
	public static <T, R> Function<T, R> metafactoryFunction(Lookup caller, MethodHandle invokedMethod, Class<R> invokedTypeReturn) throws Throwable {
		return (Function<T, R>) metafactory(caller, MTFunction, "apply", MTObject, invokedMethod, MethodType.methodType(invokedTypeReturn));
	}
	
	@SuppressWarnings("unchecked")
	public static <T> UnaryOperator<T> metafactoryUnaryOperator(Lookup caller, MethodHandle invokedMethod, Class<T> invokedTypeReturn) throws Throwable {
		return (UnaryOperator<T>) metafactory(caller, MTUnaryOperator, "apply", MTObject, invokedMethod, MethodType.methodType(invokedTypeReturn));
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> metafactoryPredicate(Lookup caller, MethodHandle invokedMethod) throws Throwable {
		return (Predicate<T>) metafactory(caller, MTPredicate, "test", MTBoolean, invokedMethod, MTBoolean);
	}
	
	//metafactory
	
	/**
	 * A {@link LambdaMetafactory} wrapper with other and better argument names and {@link Class}-arguments instead of {@link MethodType}.
	 *
	 * @param caller            a MethodHandles.Lookup-Object (obtained from {@link Lookup#lookup()} or {@link LambdaMetafactoryUtil#PUBLIC_LOOKUP})
	 * @param lambdaType        the Type of Lambda, like Runnable or Consumer
	 * @param lambdaMethodName  the Name of the Method to be overridden, like "run" or "accept"
	 * @param lambdaRetType     the return type of the Lambda, without generics. If there is no return type, it should be void.class.
	 *                          Eg. for Supplier / Function it is Object.
	 * @param invokedMethod     the Method which should be invoked. See caller-Methods.
	 * @param invokedTypeReturn the actual return type of the Method, as defined in class. Without Generics.
	 * @return a lambda of the specified type
	 * @throws Throwable any Exception being thrown on the way
	 */
	@SuppressWarnings("unchecked")
	public static <LAMBDA> LAMBDA metafactory(Lookup caller, Class<LAMBDA> lambdaType, String lambdaMethodName, Class<?> lambdaRetType, MethodHandle invokedMethod, Class<?> invokedTypeReturn) throws Throwable {
		return (LAMBDA) metafactory(caller, MethodType.methodType(lambdaType), lambdaMethodName, MethodType.methodType(lambdaRetType), invokedMethod, MethodType.methodType(invokedTypeReturn));
	}
	
	/**
	 * A {@link LambdaMetafactory} wrapper with other and better argument names.
	 *
	 * @param caller            a MethodHandles.Lookup-Object (obtained from MethodHandles.Lookup.lookup())
	 * @param lambdaType        the Type of Lambda, like Runnable or Consumer
	 * @param lambdaMethodName  the Name of the Method to be overridden, like "run" or "accept"
	 * @param lambdaRetType     the return type of the Lambda, without generics. If there is no return type, it should be void.class.
	 *                          Eg. for Supplier / Function it is Object.
	 * @param invokedMethod     the Method which should be invoked. See caller-Methods.
	 * @param invokedTypeReturn the actual return type of the Method, as defined in class. Without Generics.
	 * @return a lambda of the specified type
	 * @throws Throwable any Exception being thrown on the way
	 */
	public static Object metafactory(Lookup caller, MethodType lambdaType, String lambdaMethodName, MethodType lambdaRetType, MethodHandle invokedMethod, MethodType invokedTypeReturn) throws Throwable {
		return LambdaMetafactory.metafactory(caller, lambdaMethodName, lambdaType, lambdaRetType, invokedMethod, invokedTypeReturn).getTarget().invoke();
	}
}
