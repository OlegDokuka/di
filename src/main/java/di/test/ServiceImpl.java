package di.test;

import javax.inject.Inject;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ServiceImpl implements Service {

	@Inject
	Mono<ServiceA> serviceA;

	@Inject
	Mono<IServiceB> serviceB;

	@Override
	public Flux<String> doStaff() {
		return serviceA
				.cast(IServiceA.class)
				.flatMapMany(IServiceA::sourceA)
				.concatWith(serviceB.flatMapMany(IServiceB::sourceB));
	}
}
