package com.danikula.videocache;

import java.io.File;

public class AdFilterUtils {

    private static final class AdFilterUtilsHolder {
        static final AdFilterUtils adFilterUtils = new AdFilterUtils();
    }

    public static AdFilterUtils getInstance() {
        return AdFilterUtilsHolder.adFilterUtils;
    }

}
