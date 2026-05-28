package romario.sbpstu.loomwebflux.movie

import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers
import romario.sbpstu.loomwebflux.config.Profiles.REST_CLIENT_APACHE5

@ActiveProfiles(REST_CLIENT_APACHE5)
@Testcontainers
internal open class MovieControllerRestClientApache5IT : MovieControllerIT()
