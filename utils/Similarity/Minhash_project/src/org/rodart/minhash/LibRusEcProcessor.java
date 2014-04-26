package org.rodart.minhash;

import org.xml.sax.SAXException;

import com.ontos.core.miner.util.Utils;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LibRusEcProcessor {

    protected void handle_zipped_xmls(String[] zip_list, BookXmlHandler handler, 
                                      String minhash_similarity_filename) throws IOException, SAXException, ParserConfigurationException {
        int procecced_files_number = 0, processed_with_errors = 0;

        int total_mb = 0;
        for (int i = 0; i < zip_list.length; ++i) {
            total_mb += new File(zip_list[i]).length() / (1 << 20);
        }
        int processed_mb = 0;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();

        MinHash minHash = new MinHash(0.1);
		ArrayList<ArrayList<Integer>> minHashValues = new ArrayList<ArrayList<Integer>>();
		ArrayList<String> documentList = new ArrayList<String>();
		
        for_zips:
        for (int i = 0; i < zip_list.length; ++i) {
            ZipFile zip = new ZipFile(zip_list[i]);

            System.out.println(processed_mb + "MB / " + total_mb + "MB (compressed) processed; processing " + zip.getName() + " ...");
            processed_mb += new File(zip_list[i]).length() / (1 << 20);

            Enumeration entries = zip.entries();
            LinkedList<ZipEntry> zfiles = new LinkedList<ZipEntry>();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                zfiles.add(entry);
            }

            for_entries:
            for (ZipEntry entry : zfiles) {
                InputStream in = zip.getInputStream(entry);
                try {
                    if (procecced_files_number % 1000 == 0) {
                    	System.out.println(procecced_files_number + " files was procecced (" + processed_with_errors + " unsuccessfully) " + Utils.memoryState());
                    }
                    
                    handler.startParsingFile(zip, entry);
                    parser.parse(in, handler);
                    Book book = handler.getBook();
                    if (book.getTextLength() > 0) {
                    	computeMinHash(book.getText(), minHash, minHashValues);
                    	documentList.add(book.getPath());
                    }
                    handler.finishParsingFile(zip, entry, true);
                } catch (Exception e) {
                    if(e.getCause() instanceof SkipCurrentBookException){
                        // do nothing, just process next book
                        handler.finishParsingFile(zip, entry, true);
                        SkipCurrentBookException.SkipType type = ((SkipCurrentBookException) e.getCause()).getType();
                        if(type==SkipCurrentBookException.SkipType.FINISH_PROCESSING)
                            break for_zips;
                        else if(type== SkipCurrentBookException.SkipType.PROCESS_NEXT_ZIP)
                            continue for_zips;
                        else
                            continue for_entries;
                    } else {
                        processed_with_errors++;
                        handler.finishParsingFile(zip, entry, false);
                    }
                } finally {
                    procecced_files_number++;
                    in.close();
                }
            }
            zip.close();
        }
        System.out.println("All articles is procecced");
        int numHashFunctions = minHash.getNumHashFunctions();

       // printMinHashValues(minHashValues, numHashFunctions, documentList, "D:\\minhash.txt", "D:\\docnames.txt");
		computeSimilarityFromSignatures(minHashValues, numHashFunctions, documentList, minhash_similarity_filename);
    }
    
    // функция, которая выводит в файл все посчитанные minHash'ы и соответсвующие названия документов. 
    // Может понадобится для того, что бы сравнить все эти дохреналион минхэшовых векторов например на С++, а не ждать 1000 лет джаву
    /*
    private static void printMinHashValues(ArrayList<ArrayList<Integer>> minHashValues, int numHashFunctions, 
                                           ArrayList<String> documentList,
                                           String minhashes_filename, String docnames_filename) 
    { 
        try {
            PrintWriter minhash_out = new PrintWriter(new FileWriter(minhashes_filename));
            PrintWriter docnames_out = new PrintWriter(new FileWriter(docnames_filename));
            
            int filesNumber = documentList.size();
            for (int i = 0; i < filesNumber; ++i) {
                docnames_out.println(documentList.get(i));
                for (int j = 0; j < numHashFunctions; ++j) {
                    minhash_out.print(minHashValues.get(i).get(j) + " ");
                }
                minhash_out.println();
            }

            minhash_out.close();
            docnames_out.close();
            
            System.out.println("Finish similar documents computing\n");
        } catch (IOException e) {
            e.printStackTrace();
        } 
        
    }
    */
    
    private void computeMinHash(String doc, MinHash minHash, ArrayList<ArrayList<Integer>> minHashValues) {
    	int numTerms = 5000;
		//int numTerms = -1; - если -1, то обрабатывать весь, документ, иначе - только первые numTerms слов
		Shingle shingles = new Shingle(doc.toString(), 4);
		ArrayList<String> shiglesList = shingles.getShinglesList(numTerms);
		minHashValues.add(minHash.getMinHashValues(shiglesList));
	}
    
	private static void computeSimilarityFromSignatures(ArrayList<ArrayList<Integer>> minHashValues, 
														int numHashFunctions, 
														ArrayList<String> documentList,
														String minhash_similarity_filename) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(minhash_similarity_filename));
			int filesNumber = documentList.size();
			int similar_minhash_doc_num = 0;
			for (int i = 0; i < filesNumber - 1; ++i) {
				if (i % 1000 == 0) {
					System.out.println("SIMILARITY: computing " +  i + " files\n");
				}
				for (int j = i + 1; j < filesNumber; ++j) {
					int identicalMinHashes = 0;
					for (int k = 0; k < numHashFunctions; ++k) {
						int a = minHashValues.get(i).get(k);
						int b = minHashValues.get(j).get(k);
						if (a == b) {
							identicalMinHashes++;
						}
					}
					double prob = (1.0 * identicalMinHashes) / numHashFunctions;
					if (prob >= 0.85) {
						similar_minhash_doc_num++;
						out.println(documentList.get(i) + " <--> " + documentList.get(j) + " with P = " + prob + "\n");
					}
				}
			}
			if (similar_minhash_doc_num == 0) {
				out.println("No similar by minhash documents!");
			}
			out.close();
			System.out.println("Finish similar documents computing\n");
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	protected String[] getZipList(String path_str) {
        File temp_file;
        String[] dir_list = null;
        while (true) {
            if (path_str.charAt(path_str.length() - 1) != '/') {
                path_str += "/";
            }

            File file_in_path = new File(path_str);
            if (!file_in_path.exists()) {
                System.out.println("\nNot found: " + path_str);
                continue;
            }

            if (!file_in_path.isDirectory()) {
                System.out.println("\nNot directory: " + path_str);
                continue;
            }

            temp_file = new File(path_str);
            dir_list = temp_file.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".zip");
                }
            });
            break;
        }

        String[] result = new String[dir_list.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = path_str + dir_list[i];
        }
        return result;
    }
}

