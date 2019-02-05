package com.carpark.manager.service;

import com.carpark.manager.domain.ChargingPoint;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


public class FileSystemPersisterTest {


    @Test
    public void shouldReadStateFromFileSystem() throws URISyntaxException {
        // GIVEN
        URL state_dir = Resources.getResource("state_dir");
        FileSystemPersister persister = new FileSystemPersister(Paths.get(state_dir.toURI()));

        // WHEN
        Optional<Map<String, ChargingPoint>> stateOptional = persister.readState();

        // THEN
        assertThat(stateOptional.isPresent()).isTrue();
        assertThat(stateOptional.get().get("CP02").getPlugInTimestamp()).isEqualTo(1549321017325L);
        assertThat(stateOptional.get().get("CP02").getCurrent()).isEqualTo(10);
        assertThat(stateOptional.get().get("CP02").getName()).isEqualTo("CP02");
        assertThat(stateOptional.get().get("CP02").isPlugged()).isTrue();
    }


    @Test
    public void shouldReturnEmptyOptional() {
        // GIVEN
        FileSystemPersister persister = new FileSystemPersister(Paths.get("target/nowhere/to/be/found"));

        // WHEN
        Optional<Map<String, ChargingPoint>> stateOptional = persister.readState();

        // THEN no Exception, but just empty Optional
        assertThat(stateOptional.isPresent()).isFalse();
    }


    @Test
    public void shouldCreateDirectoriesAndWriteAFile() throws IOException {
        // GIVEN
        Path directoryPath = Paths.get("target/new/dirs");
        Files.deleteIfExists(directoryPath.resolve("state.json"));
        Files.deleteIfExists(directoryPath);
        Files.deleteIfExists(directoryPath.getParent());

        assertThat(directoryPath.resolve("state.json").toFile().exists()).isFalse();
        FileSystemPersister persister = new FileSystemPersister(directoryPath);

        // WHEN
        persister.safeState(Collections.emptyList());

        // THEN directories should be created
        assertThat(directoryPath.resolve("state.json").toFile().exists()).isTrue();
    }
}