package br.edu.ufrgs.inf.bpm.util;

import java.io.*;
import java.net.URL;

public class ResourceLoader {

    public static InputStream getResource(String artifactPath, String localPath) throws IOException {
        URL url = ResourceLoader.class.getResource(artifactPath);
        if (url == null) {
            return new FileInputStream(new File(localPath));
        } else {
            return url.openStream();
        }
    }

}
