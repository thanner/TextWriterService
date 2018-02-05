package br.edu.ufrgs.inf.bpm.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ResourceLoader {

    public static InputStream getResource(String resourcePath) throws IOException {
        URL url = ResourceLoader.class.getResource(resourcePath);
        if (url == null) {
            return new FileInputStream(new File(Paths.LocalResourcePath + resourcePath));
        } else {
            return url.openStream();
        }
    }

    public static File getResourceFile(String resourcePath) throws  IOException {
        InputStream inputStream = getResource(resourcePath);
        File tempFile = File.createTempFile("file",".txt");
        FileUtils.copyInputStreamToFile(inputStream, tempFile);
        return tempFile;
    }

}