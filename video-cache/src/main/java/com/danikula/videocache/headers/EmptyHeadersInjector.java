package com.danikula.videocache.headers;

import java.util.HashMap;
import java.util.Map;

/**
 * Empty {@link HeaderInjector} implementation.
 *
 * @author Lucas Nelaupe (https://github.com/lucas34).
 */
public class EmptyHeadersInjector implements HeaderInjector {

    @Override
    public Map<String, String> addHeaders(String url) {
        Map<String, String> map = new HashMap<>();
        map.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0");
        map.put("Referer", url);
        map.put("Accept", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        return map;
    }

}
