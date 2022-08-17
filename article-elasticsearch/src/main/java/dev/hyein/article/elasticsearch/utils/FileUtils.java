package dev.hyein.article.elasticsearch.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;

public class FileUtils {

    /**
     * 파일에 line 이 없을 경우 쓰기
     * @param file
     * @param line
     * @return 파일에 썼으면 true, 안 썼으면 false
     * @throws IOException
     */
    public static boolean writeLineIfUnique(Path file, String line) throws IOException {
        if(isNotExist(file, line)) {
            Files.write(file, line.concat(System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            return true;
        }
        return false;
    }

    /**
     * line 존재 여부
     * @param file
     * @param line
     * @return 존재하지 않으면 true, 존재하면 false
     */
    private static boolean isNotExist(Path file, String line) throws IOException {
        return !new HashSet<>(Files.readAllLines(file)).contains(line);
    }
}
