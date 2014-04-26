package org.rodart.searcher;

import com.ontos.core.miner.util.Utils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermVectorMapper;
import org.apache.lucene.index.TermVectorOffsetInfo;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.morphology.russian.RussianAnalyzer;

import java.io.*;
import java.util.*;


public class SearchBook {
    private RussianAnalyzer analyzer;
    private IndexSearcher searcher;
    private IndexReader reader;

    public SearchBook(String dir_name) throws IOException {
        FSDirectory dir = FSDirectory.open(new File(dir_name));
        reader = IndexReader.open(dir);
        searcher = new IndexSearcher(reader);
        analyzer = new RussianAnalyzer();
    }

    private static boolean isBigRussianLetter(char letter) {
        return letter >= 'À' && letter <= 'ß' || letter == '¨';
    }

    private static boolean isSentenceDelim(char c) {
        return (c == '.' || c == '!' || c == '?' || c == '…');
    }

    private static boolean isStartSentence(String s, int pos) {
        return isBigRussianLetter(s.charAt(pos)) &&
                ( pos == 0 
                  || pos > 0 && s.charAt(pos-1) == '\n'
                  || pos > 1 && s.charAt(pos-1) == ' ' && isSentenceDelim(s.charAt(pos - 2))
                );
    }

    private String GetSentence(String input_hit, int offset, Set<Integer> start_offsets) {
        int start_sentence_pos, end_sentence_pos;
        start_sentence_pos = end_sentence_pos = offset;

        //get start position of sencence
        while (start_sentence_pos > 0 && !isStartSentence(input_hit, start_sentence_pos)) {
            start_sentence_pos--;
        }

        if (start_offsets.contains(start_sentence_pos)) {
            return null;
        }
        start_offsets.add(start_sentence_pos);
        

        //get end position of sencence
        while ( end_sentence_pos < input_hit.length() && !isSentenceDelim(input_hit.charAt(end_sentence_pos)) ) {
            end_sentence_pos++;
        }

        if (end_sentence_pos != input_hit.length()) {
            end_sentence_pos++;
        }

        char[] chars = input_hit.substring(start_sentence_pos, end_sentence_pos).toCharArray();
        return new String(chars);
    }


    private static boolean IsGoodSentence(String sentence) {
        return isBigRussianLetter(sentence.charAt(0)) && isSentenceDelim(sentence.charAt(sentence.length()-1));
    }

    @Override
    protected void finalize() throws Throwable {
        searcher.close();
    }

    public List<String> search(final String query_str, Integer max_hits_number) throws ParseException, IOException, InvalidTokenOffsetsException
    {
        if (max_hits_number == -1 || max_hits_number > 1000000) {
            max_hits_number = 1000000;
        }
        System.out.println("Query: " + query_str);
        System.out.println("Input max_hits_number: " + max_hits_number);     
        
        QueryParser qParser = new QueryParser(Version.LUCENE_36, BookConstants.FIELD_TEXT, analyzer);
        Query query = qParser.parse(query_str);
        
        TopDocs topDocs = searcher.search(query, max_hits_number);
        
        if (topDocs.scoreDocs.length == 0) {
        	System.out.println("No documents found for query: " + query_str);
            return new ArrayList<String>();
        }
        
        System.out.println("Documents found: " + topDocs.scoreDocs.length);
        System.out.println("Processing results... " + Utils.memoryState());
        
        List<String> result = new ArrayList<String>();
        int procecced_hits = 0;
        int hits = 0;
        Set<Integer> start_offsets = new HashSet<Integer>();
        
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document d = searcher.doc(scoreDoc.doc);
            String text = d.get(BookConstants.FIELD_TEXT);

            final TermVectorOffsetInfo[][] matched_indexes = new TermVectorOffsetInfo[1][];
            
            reader.getTermFreqVector(scoreDoc.doc, BookConstants.FIELD_TEXT, new TermVectorMapper() {
                String lc_query_str = query_str.toLowerCase();
                
                @Override
                public void setExpectations(String s, int i, boolean b, boolean b1) {
                   //do nothing
                }

                @Override
                public void map(String s, int i, TermVectorOffsetInfo[] termVectorOffsetInfos, int[] ints) {
                    if (lc_query_str.equals(s)) {
                        matched_indexes[0] = termVectorOffsetInfos;
                    }
                }
            });
               
            if (matched_indexes[0] == null) {
                continue;
            }
            
            start_offsets.clear();
            
            for (TermVectorOffsetInfo index : matched_indexes[0]) {
                String sentence = GetSentence(text, index.getStartOffset(), start_offsets);
                if (sentence != null && IsGoodSentence(sentence) && sentence.length() < 1000) {
                    result.add(sentence);
                    procecced_hits++;
                }
                hits++;
                
                if (procecced_hits == max_hits_number) break;
            }
            if (procecced_hits == max_hits_number) break;
        }

        if (procecced_hits == 0) {
        	System.out.println("No good sentence found for query: " + query_str);
            return new ArrayList<String>();
        }
        
        System.out.println("procecced_hits = " + procecced_hits + "; total hits = " + hits + " " + Utils.memoryState());
        return result;
    }

}