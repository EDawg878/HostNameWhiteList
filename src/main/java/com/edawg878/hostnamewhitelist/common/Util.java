package com.edawg878.hostnamewhitelist.common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author EDawg878 <EDawg878@gmail.com>
 */
public class Util {

    public static Set<String> getHostNames(List<String> hosts, boolean ignoreCase) {
        Set<String> result = new HashSet<>();
        if (hosts != null) {
            for (String host : hosts) {
                if (ignoreCase) {
                    host = host.toLowerCase();
                }
                result.add(host);
            }
        }
        return result;
    }

}
