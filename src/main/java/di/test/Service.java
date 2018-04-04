package di.test;

import reactor.core.publisher.Flux;

public interface Service {
	Flux<String> doStaff();
}
