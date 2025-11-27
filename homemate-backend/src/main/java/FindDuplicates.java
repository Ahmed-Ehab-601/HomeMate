import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class FindDuplicates {
    public static void main(String[] args) throws IOException {
        Map<String, List<Path>> classNames = new HashMap<>();

        Files.walk(Paths.get("src"))
            .filter(path -> path.toString().endsWith(".java"))
            .forEach(path -> {
                String fileName = path.getFileName().toString().replace(".java", "");
                classNames.computeIfAbsent(fileName, k -> new ArrayList<>()).add(path);
            });

        System.out.println("Duplicate class names:");
        classNames.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .forEach(entry -> {
                System.out.println("\n" + entry.getKey() + ":");
                entry.getValue().forEach(path -> System.out.println("  - " + path));
            });
    }
}