package com.ft.wordpressarticletransformer.util;

import com.google.common.io.Resources;

import java.io.File;

public class FileReader {
    public static String resourceFilePath(String resourceClassPathLocation) {
        File file = null;
        try {
            file = new File(Resources.getResource(resourceClassPathLocation).toURI());
            return file.getAbsolutePath();
        } catch (Exception e) {
            if (file != null) {
                throw new RuntimeException(file.toString(), e);
            }
            throw new RuntimeException(e);
        }
    }
}
