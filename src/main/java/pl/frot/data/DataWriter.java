package pl.frot.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public class DataWriter {
    private static final Logger logger = Logger.getLogger(DataWriter.class.getName());

    private static final String PATH = "src/main/resources/summaries.txt";

    private DataWriter() {}

    public static void saveToFile(List<String> strings) {
        try {
            Files.write(Path.of(PATH), strings);
            logger.info("Data saved to file: " + PATH);
        } catch (IOException e) {
            logger.warning("Failed to write to file: " + e.getMessage());
        }

    }
}
