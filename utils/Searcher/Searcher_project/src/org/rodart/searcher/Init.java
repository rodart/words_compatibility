package org.rodart.searcher;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;


public class Init {

	public static void main(String[] args) throws IOException, ParseException, InvalidTokenOffsetsException {
	    String dirName = args[0];
		String resultFilename = args[1];
		String query_str = args[2];
	    Integer max_hits_number = Integer.valueOf(args[3]);		
		
		SearchBook searchBook = new SearchBook(dirName);
		List<String> resultSentences = searchBook.search(query_str, max_hits_number);
		
		if (resultSentences.size() == 0) {
		    return;
		}
		
		try {
			PrintWriter out = new PrintWriter(new FileWriter(resultFilename));
			out.println(query_str);
	        for (String sentence : resultSentences) { 
	        	out.println(sentence);
	        }
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Finish search, " + resultSentences.size() + " sentences was found.");
	}
}
