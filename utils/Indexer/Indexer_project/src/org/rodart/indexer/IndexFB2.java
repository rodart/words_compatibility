package org.rodart.indexer;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.morphology.russian.RussianAnalyzer;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


public class IndexFB2 extends LibRusEcProcessor {

    private Set<String> getDuplicatesDocumentsNames(String duplicates_path) {
        Set<String> duplicates = new HashSet<String>();
        File file = new File(duplicates_path);
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                duplicates.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return duplicates;
    }
    
    public void index(String zip_path_str, String index_path_str, String duplicates_path) throws Exception {
        String[] zip_list = getZipList(zip_path_str);
        RussianAnalyzer analyzer = new RussianAnalyzer();
        FSDirectory dir = FSDirectory.open(new File(index_path_str));
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
        IndexWriter writer = new IndexWriter(dir, config);
        BookIndexer indexer = new BookIndexer(writer);
        BookXmlHandler handler = new IndexerBookXmlHandler(indexer, true);
        
        Set<String> duplicates = getDuplicatesDocumentsNames(duplicates_path);        
        
        handle_zipped_xmls(zip_list, handler, duplicates);
        writer.close();
    }

    public static void main(String[] args) throws Exception {
        String zip_path = args[0];
        String index_path = args[1];
        String duplicates_path = args[2];
    
        System.out.println("Start indexing");
        IndexFB2 indexer = new IndexFB2();
        indexer.index(zip_path, index_path, duplicates_path);
        System.out.println("Finish indexing");
    }
}
