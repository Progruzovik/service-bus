package net.progruzovik.bus.util;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathResolver implements PathManager {

    private final Path currentPath;
    private final Path dataPath;

    public PathResolver() throws IOException {
        String locationUrl = PathResolver.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (System.getProperty("os.name").contains("indow")) {
            locationUrl = locationUrl.substring(1);
        }
        currentPath = Paths.get(URLDecoder.decode(locationUrl, "UTF-8"));
        dataPath = currentPath.resolveSibling("data");
        if (!Files.exists(dataPath)) {
            Files.createDirectory(dataPath);
        }
    }

    @Override
    public Path getCurrentPath() {
        return currentPath;
    }

    @Override
    public Path getDataPath() {
        return dataPath;
    }
}
