package com.carpark.manager.service;

import com.carpark.manager.domain.ChargingPoint;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.file.StandardOpenOption.*;

/**
 * Writes or reads the current State. In this Case to the file system.
 */
public class FileSystemPersister implements StatePersister {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemPersister.class);
    private static final String FILE_NAME = "state.json";
    private final Path directoryPath;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructor
     *
     * @param directoryPath Path to the directory in which the state file should be stored.
     */
    @Autowired
    public FileSystemPersister(final Path directoryPath) {
        this.directoryPath = checkNotNull(directoryPath, "directoryPath must not be null");
    }

    @Override
    public Optional<Map<String, ChargingPoint>> readState() {
        final File file = directoryPath.resolve(FILE_NAME).toFile();
        if (file.exists()) {
            LOGGER.info("Found existing state file at {}", file.getAbsolutePath());
            try {
                final List<ChargingPoint> cpList = objectMapper.readValue(file, new TypeReference<List<ChargingPoint>>() {
                });
                return Optional.of(cpList.stream().collect(Collectors.toMap(ChargingPoint::getName, cp -> cp)));
            } catch (IOException e) {
                LOGGER.error("Could not read state file {}", file.getAbsolutePath(), e);
            }
        }
        LOGGER.info("No state file found at {}", file.getAbsolutePath());
        return Optional.empty();
    }

    @Override
    public void safeState(final List<ChargingPoint> currentState) {
        final Path path = directoryPath.resolve(FILE_NAME);
        LOGGER.debug("Writing state file to {}", path);
        try {
            createDirectoriesIfNecessarry(directoryPath);
            Files.write(path, objectMapper.writeValueAsBytes(currentState), WRITE, CREATE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("Failed to write current state to {}", directoryPath, e);
        }
    }

    private void createDirectoriesIfNecessarry(final Path directoryPath) throws IOException {
        if (!directoryPath.toFile().exists()) {
            LOGGER.info("Creating directories at {}", directoryPath);
            Files.createDirectories(directoryPath);
        }
    }

}
