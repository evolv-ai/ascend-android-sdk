package ai.evolv;

import com.google.gson.JsonArray;
import java.util.HashMap;

class LruCache {

    private HashMap<String, CacheEntry> cache;
    private CacheEntry start;
    private CacheEntry end;
    private int cacheSize;

    LruCache(int cacheSize) {
        this.cache = new HashMap<>();
        this.cacheSize = cacheSize;
    }

    JsonArray getEntry(String key) {
        if (cache.containsKey(key)) {
            CacheEntry entry = cache.get(key);
            removeEntry(entry);
            addAtTop(entry);
            return entry.value;
        }
        return new JsonArray();
    }

    void putEntry(String key, JsonArray value) {
        if (cache.containsKey(key)) {
            CacheEntry entry = cache.get(key);
            entry.value = value;
            removeEntry(entry);
            addAtTop(entry);
        } else {
            CacheEntry newEntry = new CacheEntry(key, value, null, null);
            if (cache.size() >= cacheSize) {
                cache.remove(end.key);
                removeEntry(end);
                addAtTop(newEntry);
            } else {
                addAtTop(newEntry);
            }
            cache.put(key, newEntry);
        }
    }

    private void addAtTop(CacheEntry entry) {
        entry.right = start;
        entry.left = null;
        if (start != null) {
            start.left = entry;
        }
        start = entry;
        if (end == null) {
            end = start;
        }
    }

    private void removeEntry(CacheEntry entry) {
        if (entry.left != null) {
            entry.left.right = entry.right;
        } else {
            start = entry.right;
        }

        if (entry.right != null) {
            entry.right.left = entry.left;
        } else {
            end = entry.left;
        }
    }


}
