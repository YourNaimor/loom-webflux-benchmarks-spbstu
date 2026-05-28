package romario.sbpstu.loomwebflux.common.proxy.reactive;

import reactor.core.publisher.Mono;
import romario.sbpstu.loomwebflux.common.proxy.ServiceProxy;

public interface ReactiveServiceProxy extends ServiceProxy<Mono<Long>> {
}
