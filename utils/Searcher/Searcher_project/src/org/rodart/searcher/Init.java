package org.rodart.searcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;


public class Init {

    private static String getQueryWord(String fileName) {
        File file = new File(fileName);
        String query = new String();
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream (file), "UTF8");
            BufferedReader bufferedReader = new BufferedReader (inputStreamReader);
            query = bufferedReader.readLine();
            bufferedReader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return query;
    }
    
	public static void main(String[] args) throws IOException, ParseException, InvalidTokenOffsetsException {
	    String dirName = args[0];
		String resultFilename = args[1];
		String queryFileName = args[2];
	    Integer max_hits_number = Integer.valueOf(args[3]);		
		
	    String query_str = getQueryWord(queryFileName);
	    System.out.println("Try to process query " + query_str);
	    
		SearchBook searchBook = new SearchBook(dirName);
		List<String> resultSentences = searchBook.search(query_str, max_hits_number);
		
		if (resultSentences.size() == 0) {
		    return;
		}
		
		try {
		    File file = new File(resultFilename);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            
            bufferedWriter.write(query_str);
            bufferedWriter.newLine();
            bufferedWriter.flush();
	        
            for (String sentence : resultSentences) { 
                bufferedWriter.write(sentence);
                bufferedWriter.newLine();
                bufferedWriter.flush();
	        }
	        bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Finish search, " + resultSentences.size() + " sentences was found.");
	}
}
