package relake.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@RequiredArgsConstructor
public abstract class Config {
    public final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final String folder;

    private String name = "default";

    public abstract boolean load();

    public abstract boolean save();

    public String getFileName() {
        return "C:\\zhuk\\" + folder + "\\" + name + "." + folder;
    }
}
