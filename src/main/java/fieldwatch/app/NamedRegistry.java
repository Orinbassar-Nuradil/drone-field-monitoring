package fieldwatch.app;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/** Case-insensitive name to provider lookup. The extension point for both Bridge axes. */
public final class NamedRegistry<T> {

    private final String kind;
    private final Map<String, T> entries = new TreeMap<>();

    public NamedRegistry(String kind) {
        this.kind = kind;
    }

    public NamedRegistry<T> register(String key, T entry) {
        entries.put(key.toLowerCase(Locale.ROOT), entry);
        return this;
    }

    public T lookup(String key) {
        T entry = entries.get(key.toLowerCase(Locale.ROOT));
        if (entry == null) {
            throw new UnknownSelectionException(
                    "Unknown " + kind + " '" + key + "'. Known: " + entries.keySet());
        }
        return entry;
    }
}
