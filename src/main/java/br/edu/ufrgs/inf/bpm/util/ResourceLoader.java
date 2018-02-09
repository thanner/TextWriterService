package br.edu.ufrgs.inf.bpm.util;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;

public class ResourceLoader {

    public static String getResourcePath(String resourcePath) throws UnsupportedEncodingException {
        return URLDecoder.decode(ResourceLoader.class.getResource(resourcePath).getPath(), "UTF-8");
    }

    public static InputStream getResource(String resourcePath) throws IOException {
        URL url = ResourceLoader.class.getResource(resourcePath);
        if (url == null) {
            return new FileInputStream(new File(Paths.LocalResourcePath + resourcePath));
        } else {
            return url.openStream();
        }
    }

    public static File getResourceFile(String resourcePath, String sufix) throws IOException {
        InputStream inputStream = getResource(resourcePath);
        File tempFile = File.createTempFile("file", sufix);
        FileUtils.copyInputStreamToFile(inputStream, tempFile);
        return tempFile;
    }

}