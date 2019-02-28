package com.mflechl.mfchess;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ReadOpenings {

    /**
     * Open and read a file, and return the lines in the file as a list
     * of Strings.
     * (Demonstrates Java FileReader, BufferedReader, and Java5.)
     */

    List<String> openings = new ArrayList<>(); //if static, can access it without running constructor

    ReadOpenings() {
        this("openings.txt");
    }

    ReadOpenings(String filename) {
        try {
            InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(filename);

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                openings.add(line);
            }
            reader.close();
        } catch (Exception e) {
            System.err.format("Exception occurred trying to read '%s'.", filename);
            e.printStackTrace();
        }
    }


}
