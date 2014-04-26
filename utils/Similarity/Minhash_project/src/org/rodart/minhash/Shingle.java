package org.rodart.minhash;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class Shingle {

	String doc;
	int shingleLength;
	
	public Shingle(String doc, int shingleLength){
		this.shingleLength = shingleLength;
		this.doc = doc;
    }
	
	private ArrayList<String> getListOfTerms(int numTerms) {
		ArrayList<String> words = new ArrayList<String>();
		if (numTerms == -1) {
			String[] tmp_words = doc.split("\\s+");
			for (int i = 0; i < tmp_words.length; ++i) {
				words.add(tmp_words[i]);
			}
		} else {
			int i = 0;
			StringTokenizer st = new StringTokenizer(doc, " "); 
			while(st.hasMoreTokens() && i < numTerms) {
				i++;
				words.add(st.nextToken());
			}
		}
		
		for (int i = 0; i < words.size() && i < numTerms; i++) {
		    words.set(i, words.get(i).replaceAll("[^\\à-ÿÀ-ßA-Za-z]", ""));
		    words.set(i, words.get(i).toLowerCase());
		}
		
		ArrayList<String> stringList = new ArrayList<String>();
		for (int i = 0; i < words.size(); i++) {
			if (words.get(i).length() != 0) {
				stringList.add(words.get(i));
			}
		}
		
		return stringList;
	}
	
	public ArrayList<String> getShinglesList(int numTerms) {
		ArrayList<String> terms = getListOfTerms(numTerms);
		ArrayList<String> nGrams = new ArrayList<String>();
		for (int i = 0; i < terms.size() - shingleLength; i++) {
			String nGramItem = "";
			for (int j = 0; j < shingleLength; j++) {
				nGramItem += terms.get(i + j) + " ";
			}
			nGrams.add(nGramItem);
		}
		return nGrams;
	}

}
