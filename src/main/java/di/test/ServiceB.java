package di.test;

import reactor.core.publisher.Flux;

public class ServiceB implements IServiceB {

	@Override
	public Flux<String> sourceB() {
		return Flux.range(5, 5)
				.map(i -> "B_" + i);
	}
}
