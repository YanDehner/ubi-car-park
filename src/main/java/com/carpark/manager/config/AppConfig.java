package com.carpark.manager.config;

import com.carpark.manager.service.FileSystemPersister;
import com.carpark.manager.service.RequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableConfigurationProperties
public class AppConfig {

    @Autowired
    private ChargingPointsConfig config;

    @Value("${max-current}")
    private int maxCurrent;

    @Value("${high-current}")
    private int highCurrent;

    @Value("${low-current}")
    private int lowCurrent;

    @Value("${state-file-directory.path}")
    private String stateFilePath;


    @Bean
    RequestHandler pluggingService(final FileSystemPersister statePersister) {
        return new RequestHandler(config.getNames(), maxCurrent, highCurrent, lowCurrent, statePersister);
    }

    @Bean
    FileSystemPersister statePersister() {
        return new FileSystemPersister(Paths.get(stateFilePath));
    }

    @Configuration
    @ConfigurationProperties(prefix = "charging-points")
    public class ChargingPointsConfig {

        private final List<String> names = new ArrayList<>();

        public List<String> getNames() {
            return names;
        }

    }

}
