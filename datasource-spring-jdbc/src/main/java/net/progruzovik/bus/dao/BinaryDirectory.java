package net.progruzovik.bus.dao;

import net.progruzovik.bus.util.PathManager;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class BinaryDirectory implements BinaryDao {

    private final PathManager pathManager;

    public BinaryDirectory(PathManager pathManager) {
        this.pathManager = pathManager;
    }

    @Override
    public Path addBinary(@NonNull String name, @NonNull Path path) throws IOException {
        return Files.move(path, pathManager.getDataPath().resolve(name), StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void removeBinary(@NonNull String name) throws IOException {
        Files.deleteIfExists(pathManager.getDataPath().resolve(name));
    }
}
