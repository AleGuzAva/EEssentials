package EEssentials.config;

import com.google.common.base.Charsets;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A class used to create a {@link Configuration} using snakeyaml, based off of BungeeCord's configuration implementation.
 * To read a file into a {@link Configuration}, use {@link YamlConfiguration#loadConfiguration(File)}.
 */
public final class YamlConfiguration {
    private static final ThreadLocal<Yaml> yaml = ThreadLocal.withInitial(() -> {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Representer representer = new Representer(options) {{
            representers.put(Configuration.class, data -> represent(((Configuration) data).self));
        }};
        return new Yaml(new Constructor(new LoaderOptions()), representer, options);
    });
    
    public static void save(Configuration config, File file) throws IOException {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
            save(config, writer);
        }
    }
    
    public static void save(Configuration config, Writer writer) {
        yaml.get().dump(config.self, writer);
    }
    
    public static Configuration loadConfiguration(File file) throws IOException {
        return loadConfiguration(file, null);
    }
    
    public static Configuration loadConfiguration(File file, Configuration defaults) throws IOException {
        try (FileInputStream is = new FileInputStream(file)) {
            return loadConfiguration(is, defaults);
        }
    }
    
    public static Configuration loadConfiguration(Reader reader) {
        return loadConfiguration(reader, null);
    }
    
    @SuppressWarnings("unchecked")
    public static Configuration loadConfiguration(Reader reader, Configuration defaults) {
        Map<String, Object> map = yaml.get().loadAs(reader, LinkedHashMap.class);
        if (map == null) {
            map = new LinkedHashMap<>();
        }
        return new Configuration(map, defaults);
    }
    
    public static Configuration loadConfiguration(InputStream is) {
        return loadConfiguration(is, null);
    }

    @SuppressWarnings("unchecked")
    public static Configuration loadConfiguration(InputStream is, Configuration defaults) {
        Map<String, Object> map = yaml.get().loadAs(is, LinkedHashMap.class);
        if (map == null) {
            map = new LinkedHashMap<>();
        }
        return new Configuration(map, defaults);
    }
    
    public static Configuration loadConfiguration(String string) {
        return loadConfiguration(string, null);
    }
    
    @SuppressWarnings("unchecked")
    public static Configuration loadConfiguration(String string, Configuration defaults) {
        Map<String, Object> map = yaml.get().loadAs(string, LinkedHashMap.class);
        if (map == null) {
            map = new LinkedHashMap<>();
        }
        return new Configuration(map, defaults);
    }
}