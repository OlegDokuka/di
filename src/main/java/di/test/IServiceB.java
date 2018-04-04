package di.test;

import reactor.core.publisher.Flux;

public interface IServiceB {

	Flux<String> sourceB();

	static IServiceB instance() {
		return new ServiceB();
	}
}
