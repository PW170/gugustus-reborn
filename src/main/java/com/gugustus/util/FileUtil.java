package com.gugustus.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {

    public static boolean ensureDirectoryExists(File dir) {
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return true;
    }

    public static String readFile(File file) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            return null;
        }
        return content.toString();
    }

    public static boolean writeFile(File file, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
