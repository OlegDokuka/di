package di.test;

import org.junit.Test;
import reactor.test.StepVerifier;

public class ServiceImplTest {

	@Test
	public void doStaff() {
		Service proxy = Injector.proxy(new ServiceImpl(), Service.class);

		StepVerifier.create(proxy.doStaff())
					.expectSubscription()
					.expectAccessibleContext().hasKey(Injector.HOLDER_KEY).then()
					.expectNextCount(15)
					.verifyComplete();
	}
}