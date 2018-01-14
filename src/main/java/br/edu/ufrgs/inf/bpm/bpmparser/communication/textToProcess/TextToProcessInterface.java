package br.edu.ufrgs.inf.bpm.bpmparser.communication.textToProcess;

import br.edu.ufrgs.inf.bpm.bpmparser.communication.ProcessElement;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.stream.Stream;

public interface TextToProcessInterface extends Remote {
    long add(long a, long b) throws RemoteException;

    Stream<ProcessElement> getProcessElementStream(File file);
}