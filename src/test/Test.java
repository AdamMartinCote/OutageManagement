package test;

import utils.Operations;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

public class Test {
    private int result;
    private LinkedList<String> operationQueue;
    private String operationFileName;
    private final String PELL = "pell";
    private final String PRIME = "prime";

    public Test() {
        this.operationQueue = new LinkedList<String>();
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        Test test = new Test();
        test.operationFileName = args[0];
        test.parseOperationFile();
        Iterator<String> it = test.operationQueue.iterator();
        while(it.hasNext()) {
            String computation = it.next();
            test.result += test.compute(computation);
            test.result = test.result % 4000;
        }
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        System.out.println("Test result: " + test.result);
        System.out.println("Computation took: " + totalTime);
    }

    public int compute(String command) {
        String[] args = command.split("\\s+");
        if (args[0].equals(PELL)) {
            return Operations.pell(Integer.parseInt(args[1]));
        } else if(args[0].equals(PRIME)) {
            return Operations.prime(Integer.parseInt(args[1]));
        } else {
            // TODO : handle error
            return -1;
        }
    }

    public void parseOperationFile() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(this.operationFileName));
            String line = reader.readLine();
            while (line != null) {
                this.operationQueue.add(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
