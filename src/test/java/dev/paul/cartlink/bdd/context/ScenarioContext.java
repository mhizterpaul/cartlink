package dev.paul.cartlink.bdd.context;

import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ScenarioScope
public class ScenarioContext {

    private Map<String, Object> data = new HashMap<>();

    public void set(String key, Object value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        return (T) data.get(key);
    }

    public String getString(String key) {
        return get(key, String.class);
    }

    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    public void clear() {
        data.clear();
    }
}
