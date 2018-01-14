package br.edu.ufrgs.inf.bpm.bpmparser.communication.textToProcess;

public class AppTextToProcessCommunication {

    public static void main(String[] args) {
        TextToProcessClient textToProcessClient = new TextToProcessClient();

        System.out.println(textToProcessClient.adicao(5000, 1000));

        // Stream<ProcessElement> processElementStream = textToProcessClient.getProcessElementStream(new File(""));
        //processElementStream.forEach(System.out::println);
    }
}
