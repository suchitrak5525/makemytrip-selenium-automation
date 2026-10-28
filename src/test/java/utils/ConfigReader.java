
package utils;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigReader.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new RuntimeException(
                    "config.properties not found. " +
                    "Place it in src/test/resources/"
                );
            }

            properties.load(input);

        } catch (IOException e) {
            throw new RuntimeException(
                "Unable to load config.properties", e
            );
        }
    }

    public static String getProperty(String key) {
        String value = properties.getProperty(key);

        if (value == null) {
            throw new RuntimeException(
                "Property not found: " + key
            );
        }

        return value.trim();
    }
}
