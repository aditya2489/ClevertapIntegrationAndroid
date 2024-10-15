package com.ge.clevertapanalytics;
import android.os.Bundle;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
public class BundleToMap {

    public static Map<String, Object> bundleToMap(Bundle bundle) {
        Map<String, Object> map = new HashMap<>();

        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            map.put(key, bundle.get(key));
        }

        return map;
}}
