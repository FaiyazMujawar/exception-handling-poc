package com.rheumera.poc.utils;

import java.io.IOException;
import java.nio.file.Path;

import static java.lang.System.getProperty;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.exists;
import static java.nio.file.Paths.get;

public class PathUtils {
    private static Path getDirPath(String subDir) throws IOException {
        var rootDir = getProperty("user.dir");
        var path = get(rootDir, subDir);
        if (!exists(path))
            createDirectory(path);
        return path;
    }

    public static Path getErrorDirPath() throws IOException {
        return getDirPath("error");
    }

    public static Path getStatusDir() throws IOException {
        return getDirPath("status");
    }

    public static Path getInputDirPath() throws IOException {
        return getDirPath("input");
    }
}