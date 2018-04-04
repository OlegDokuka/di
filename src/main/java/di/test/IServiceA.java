package di.test;

import reactor.core.publisher.Flux;

public interface IServiceA {

	Flux<String> sourceA();

	static IServiceA instance() {
		return new ServiceA();
	}
}
