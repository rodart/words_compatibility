package org.rodart.minhash;

import java.util.ArrayList;
import java.util.Collections;

public class MinHash {
	int numHashFunctions;
	int[] seeds;
	
	public MinHash(double max_error){
		numHashFunctions = (int) Math.round(1 / (max_error * max_error));
		seeds = new int[numHashFunctions];
		for (int i = 0; i < numHashFunctions; ++i) {
			seeds[i] = (int)(Math.random() * numHashFunctions) + 32;
		}
    }
	
    public ArrayList<Integer> getMinHashValues(ArrayList<String> shiglesList){       
    	ArrayList<Integer> minHashValues = new ArrayList<Integer>(Collections.nCopies(numHashFunctions, Integer.MAX_VALUE));
		for (int i = 0; i < numHashFunctions; ++i) {
			for(String shingle : shiglesList) {
				int hashValue = MurmurHash.hash32(shingle, seeds[i]);
				if (hashValue < minHashValues.get(i)) {
					minHashValues.add(i, hashValue);
				}
			}
		}
        return minHashValues;
    }
    
    public int getNumHashFunctions() {
    	return numHashFunctions;
    }
}