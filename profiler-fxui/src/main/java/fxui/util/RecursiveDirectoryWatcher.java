package fxui.util;

import common.Util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

public class RecursiveDirectoryWatcher {
  private final WatchService watcher;
  private final Map<WatchKey, Path> keys;
  private final FileEventListener listener;
  private final List<Path> watchedDirs;

  public RecursiveDirectoryWatcher(FileEventListener listener, Path rootDirectory, Path... watchedDirs) throws IOException {
    this.watcher = FileSystems.getDefault().newWatchService();
    this.keys = new HashMap<>();
    this.listener = listener;
    this.watchedDirs = Arrays.asList(Util.prependToArray(watchedDirs, rootDirectory));
    registerAll(rootDirectory);
  }

  public void register(Path dir) throws IOException {
    WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    keys.put(key, dir);
  }

  private void registerAll(final Path root) throws IOException {
    Files.walkFileTree(root, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (watchedDirs.contains(dir)) {
          register(dir);
          return FileVisitResult.CONTINUE;
        } else {
          return FileVisitResult.SKIP_SUBTREE;
        }
      }
    });
  }

  @SuppressWarnings("unchecked")
  static <T> WatchEvent<T> cast(WatchEvent<?> event) {
    return (WatchEvent<T>) event;
  }

  public void processEvents() {
    for (; ; ) {
      WatchKey key;
      try {
        key = watcher.take();
      } catch (InterruptedException x) {
        return;
      }
      Path dir = keys.get(key);
      if (dir == null) {
        System.err.println("WatchKey not recognized!");
        continue;
      }
      for (WatchEvent<?> event : key.pollEvents()) {
        WatchEvent<Path> ev = cast(event);
        Path name = ev.context();
        Path child = dir.resolve(name);
        //System.out.format("%s: %s\n", event.kind().name(), child);
        WatchEvent.Kind<?> kind = event.kind();
        if (kind == OVERFLOW) {
          continue;
        }
        if (kind.equals(ENTRY_CREATE)) {
          if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
            try {
              registerAll(child);
            } catch (IOException ignored) {
            }
          }
          listener.onFileCreated(child);
        } else if (kind.equals(ENTRY_DELETE)) {
          listener.onFileDeleted(child);
        }
      }
      boolean valid = key.reset();
      if (!valid) {
        keys.remove(key);
        if (keys.isEmpty()) {
          break;
        }
      }
    }
  }

  public interface FileEventListener {
    void onFileCreated(@SuppressWarnings("unused") Path path);

    void onFileDeleted(@SuppressWarnings("unused") Path path);
  }
}
