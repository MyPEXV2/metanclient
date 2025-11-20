package relake.common.util;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.stream.Collectors;

import java.io.InputStream;

@UtilityClass
public class FileUtil {
    public String readInputStream(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream))
                .lines()
                .map(line -> line + '\n')
                .collect(Collectors.joining());
    }

    @SneakyThrows
    public InputStream createStream(String path) {
        @Cleanup InputStream inputStream = FileUtil.class.getResourceAsStream(String.format("/assets/minecraft/relake/%s", path));
        @Cleanup ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        IOUtils.copy(inputStream, buffer);

        return new ByteArrayInputStream(buffer.toByteArray());
    }
}
