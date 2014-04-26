package org.rodart.minhash;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class Init {
	
	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException{
		String dirName = args[0];
		String minhash_similarity_filename = args[1];
		String similarity_by_key_filename = args[2];

		LibRusEcProcessor lib_processor = new LibRusEcProcessor();
		String[] zip_list = lib_processor.getZipList(dirName);
		BookXmlHandler handler = new BookXmlHandler();
		lib_processor.handle_zipped_xmls(zip_list, handler, minhash_similarity_filename);
		handler.printSimilarBooksTitles(similarity_by_key_filename);
	}
}
