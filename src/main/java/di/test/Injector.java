package di.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@SuppressWarnings("unchecked")
public class Injector {
	static Class<Map> HOLDER_KEY = Map.class;

	public static <T> Mono<T> inject(Class<T> source) {
		return Mono.subscriberContext()
		           .filter(context -> context.hasKey(HOLDER_KEY))
				   .map(context -> context.get(HOLDER_KEY))
				   .map(map -> (T) map.computeIfAbsent(
                        source,
	                    key -> instantiate(source)
				   ))
				   .switchIfEmpty(Mono.error(new RuntimeException("No available context found")));
	}

	public static <T> Mono<T> withInjector(Mono<T> in) {
		return in
				.subscriberContext(context -> context.hasKey(HOLDER_KEY)
						? context
						: Context.of(HOLDER_KEY, new ConcurrentHashMap<>()));}

	public static <T> Flux<T> withInjector(Flux<T> in) {
		return in
				.subscriberContext(context -> context.hasKey(HOLDER_KEY)
						? context
						: Context.of(HOLDER_KEY, new ConcurrentHashMap<>()));
	}

	/**
	 * Setting up proxy around object to give an access to Context
	 * @param instance
	 * @param baseInterfaces
	 * @param <T>
	 * @return
	 */
	public static <T> T proxy(T instance, Class<?>... baseInterfaces) {
		scanForInjection(instance);

		return (T) Proxy.newProxyInstance(
			ClassLoader.getSystemClassLoader(),
			baseInterfaces,
			handler(instance)
		);
	}

	/**
	 * Create Invocation Handler which provide actual context to the class
	 * Note, context will be available only for methods with reactive types
	 * @param instance
	 * @return
	 */
	private static InvocationHandler handler(Object instance) {
		return (proxy, method, args) -> {
			Class<?> type = method.getReturnType();

			if(Mono.class.isAssignableFrom(type)) {
				Mono result = (Mono) method.invoke(instance, args);

				return withInjector(result);
			} else if(Flux.class.isAssignableFrom(type)) {
				Flux result = (Flux) method.invoke(instance, args);

				return withInjector(result);
			} else {
				return method.invoke(instance, args);
			}
		};
	}

	/**
	 * Inject Monos with instances from the context
	 * @param instance
	 */
	private static void scanForInjection(Object instance) {
		Flux.fromArray(instance.getClass().getDeclaredFields())
		    .filter(f -> f.isAnnotationPresent(Inject.class))
		    .filter(f -> Mono.class.isAssignableFrom(f.getType()))
		    .flatMap(f ->
			    Mono.fromCallable(() -> {
					    f.set(instance, inject((Class) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0]));
					    return instance;
				    })
			        .then()
		    )
		    .blockLast();
	}

	private static <T> T instantiate(Class<T> source) {
		try {
			T instance;
			if (source.isInterface()) {
				instance = (T) source.getDeclaredMethod("instance").invoke(source);

				return proxy(instance, source);
			} else {
				instance = source.newInstance();

				return proxy(instance, source.getInterfaces());
			}
		}
		catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
			throw new RuntimeException(e);
		}
	}
}
