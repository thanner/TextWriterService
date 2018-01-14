package br.edu.ufrgs.inf.bpm.bpmparser.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CsvReader {

    public List<List<String>> readCsv(String path) {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        List<List<String>> elements = new ArrayList<>();

        try {

            br = new BufferedReader(new FileReader(path));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                List<String> element = Arrays.asList(line.split(cvsSplitBy));
                elements.add(new ArrayList<String>(element));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return elements;
    }
}
