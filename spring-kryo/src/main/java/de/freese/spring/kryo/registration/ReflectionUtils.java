// Created: 27.02.2026
package de.freese.spring.kryo.registration;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Thomas Freese
 */
public final class ReflectionUtils {
    /**
     * Liefert alle Klassen im Package und Sub-Packages.<br>
     * Funktioniert nicht bei Runtime-Packages.
     */
    public static Set<Class<?>> getClasses(final String packageName) throws ClassNotFoundException, IOException {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        final String packagePath = packageName.replace('.', '/');
        final Enumeration<URL> resources = classLoader.getResources(packagePath);

        final List<String> fileNames = new ArrayList<>();

        while (resources.hasMoreElements()) {
            final URL resource = resources.nextElement();
            fileNames.add(resource.getFile());
        }

        final Set<Class<?>> classes = new HashSet<>();

        for (String fileName : fileNames) {
            if (fileName.contains(".jar!")) {
                final URI jarFileURI = URI.create(fileName.substring(0, fileName.indexOf(".jar!") + 4));

                classes.addAll(findClassesInJar(Path.of(jarFileURI), packagePath, classLoader));
            }
            else {
                classes.addAll(findClassesInFolder(new File(fileName), packageName));
            }
        }

        return classes;
    }

    /**
     * Suche in Verzeichnissen bei Ausführung in der IDE.<br>
     * Funktioniert nicht bei Runtime-Packages.
     */
    private static Set<Class<?>> findClassesInFolder(final File directory, final String packageName) throws ClassNotFoundException {
        final Set<Class<?>> classes = new HashSet<>();

        if (!directory.exists()) {
            return classes;
        }

        final File[] files = directory.listFiles();

        if (files == null) {
            return classes;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClassesInFolder(file, packageName + "." + file.getName()));
            }
            else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }

        return classes;
    }

    /**
     * Suche in Jars bei Ausführung in Deploy-Umgebung.<br>
     * Funktioniert nicht bei Runtime-Packages.
     */
    private static Set<Class<?>> findClassesInJar(final Path path, final String packagePath, final ClassLoader classLoader) throws ClassNotFoundException, IOException {
        final Set<String> files = new HashSet<>();

        try (FileSystem fileSystem = FileSystems.newFileSystem(path, classLoader)) {
            try (Stream<Path> paths = Files.walk(fileSystem.getPath(packagePath))) {
                paths.map(Path::toString).filter(p -> p.endsWith(".class")).map(p -> p.replace("/", ".")).forEach(files::add);
            }
        }

        final Set<Class<?>> classes = new HashSet<>();

        for (String file : files) {
            classes.add(Class.forName(file.substring(0, file.length() - 6)));
        }

        return classes;
    }

    private ReflectionUtils() {
        super();
    }
}
