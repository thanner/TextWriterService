package br.edu.ufrgs.inf.bpm.bpmparser.communication.textToProcess;

import br.edu.ufrgs.inf.bpm.bpmparser.communication.ProcessElement;

import java.io.File;
import java.rmi.Naming;
import java.util.stream.Stream;

public class TextToProcessClient {
    private static final String address = "127.0.0.1";
    private static final int port = 1099;
    private static final String service = "TextToProcessService";

    private static String getNaming() {
        return "rmi://" + address + ":" + port + "/" + service;
    }

    public long adicao(long a, long b) {
        try {
            TextToProcessInterface c = (TextToProcessInterface) Naming.lookup(getNaming());
            return c.add(a, b);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Stream<ProcessElement> getProcessElementStream(File file) {
        Stream<ProcessElement> stream = null;
        try {
            TextToProcessInterface textToProcessInterface = (TextToProcessInterface) Naming.lookup(getNaming());
            stream = textToProcessInterface.getProcessElementStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stream;
    }
}
