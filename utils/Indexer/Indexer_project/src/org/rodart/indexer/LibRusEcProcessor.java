package org.rodart.indexer;

import com.ontos.core.miner.util.Utils;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LibRusEcProcessor {

    protected void handle_zipped_xmls(String[] zip_list, BookXmlHandler handler, Set<String> duplicates) throws IOException, SAXException, ParserConfigurationException {
        int procecced_files_number = 0, processed_with_errors = 0;

        int total_mb = 0;
        for (int i = 0; i < zip_list.length; ++i) {
            total_mb += new File(zip_list[i]).length() / (1 << 20);
        }
        int processed_mb = 0;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();

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
                    
                    String book_path = handler.getBookPath(zip, entry);
                    if (duplicates.contains(book_path)) {
                        continue for_entries;
                    }
                    
                    handler.startParsingFile(zip, entry);
                    parser.parse(in, handler);
                    handler.finishParsingFile(zip, entry, true);
                } catch (Exception e) {
                    if(e.getCause() instanceof SkipCurrentBookException){
                        //do nothing, just process next book
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
                        //System.out.println("Cannot process file " + entry.getName() + " from " + zip.getName() + ": " + e.getMessage());
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
