package easy.azul.api.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TimeConfig {

    @Bean
    public ZoneId appZoneId(@Value("${app.timezone:America/Sao_Paulo}") String tz) {
        return ZoneId.of(tz);
    }

    @Bean
    public Clock appClock(ZoneId zoneId) {
        return Clock.system(zoneId);
    }

    @EnableScheduling
    @Configuration
    public class SchedulingConfig {}
}