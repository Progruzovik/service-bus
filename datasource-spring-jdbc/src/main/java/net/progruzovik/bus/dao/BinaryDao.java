package net.progruzovik.bus.dao;

import org.springframework.lang.NonNull;

import java.io.IOException;
import java.nio.file.Path;

public interface BinaryDao {

    Path addBinary(@NonNull String name, @NonNull Path path) throws IOException;

    void removeBinary(@NonNull String name) throws IOException;
}
