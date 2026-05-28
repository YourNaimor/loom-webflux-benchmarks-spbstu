package romario.sbpstu.loomwebflux.movie

import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers
import romario.sbpstu.loomwebflux.config.Profiles.REST_CLIENT_REACTOR_NETTY

@ActiveProfiles(REST_CLIENT_REACTOR_NETTY)
@Testcontainers
internal open class MovieControllerRestClientReactorNettyIT : MovieControllerIT()
