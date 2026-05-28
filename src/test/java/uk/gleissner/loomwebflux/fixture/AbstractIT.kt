package romario.sbpstu.loomwebflux.fixture

import org.junitpioneer.jupiter.cartesian.ArgumentSets
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import romario.sbpstu.loomwebflux.common.Approaches.LOOM_NETTY
import romario.sbpstu.loomwebflux.common.Approaches.LOOM_TOMCAT
import romario.sbpstu.loomwebflux.common.Approaches.PLATFORM_TOMCAT
import romario.sbpstu.loomwebflux.common.Approaches.WEBFLUX_NETTY


@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
abstract class AbstractIT {

    companion object {

        @JvmStatic
        fun approaches(): List<String> = listOf(PLATFORM_TOMCAT, LOOM_TOMCAT, LOOM_NETTY, WEBFLUX_NETTY)

        @JvmStatic
        fun approachesAndDelayCallDepths(): ArgumentSets {
            return ArgumentSets
                .argumentsForFirstParameter(approaches())
                .argumentsForNextParameter(0, 1, 2)
        }

    }
}
