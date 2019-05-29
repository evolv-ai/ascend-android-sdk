package ai.evolv;

import com.google.gson.JsonArray;

class CacheEntry {
    String key;
    JsonArray value;
    CacheEntry left;
    CacheEntry right;

    CacheEntry(String key, JsonArray value, CacheEntry left, CacheEntry right) {
        this.key = key;
        this.value = value;
        this.left = left;
        this.right = right;
    }
}
