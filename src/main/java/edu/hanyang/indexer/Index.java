package edu.hanyang.indexer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.tuple.MutableTriple;

public class Index {
	
	private BPlusTree tree = null;
	
	public Index () throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		// external code binding
		Class<?> cls = Class.forName("edu.hanyang.submit.TinySEBPlusTree");
		tree = (BPlusTree) cls.newInstance();
	}
	
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		// argument 수 검사 (사용법 준수)
		if (args.length != 6) {
			System.err.print("usage: <inputfile> <directory metafile> <directory nodefile> <posting file> <block size> <number of available blocks>");
			System.exit(1);
		}
		
		// 한번에 이용할 메모리 블록 크기와 블록 개수
		int blocksize = Integer.parseInt(args[4]);
		int nblocks = Integer.parseInt(args[5]);
		
		Index index = new Index();
		index.run(args[0], args[1], args[2], args[3], blocksize, nblocks);
	}

	private void run(String inputfile, String dirmetafile, String dirnodefile, String postingfile, int blocksize, int nblocks) throws IOException {
		DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(inputfile)));
		
		int[] tuple = new int[3];
		
		int cnt = 0;				// # of distinct words (terms)
		int prev_word = -1;
		
		// initialize inverted list
		// NaiveInvertedList invlist = new NaiveInvertedList ();
		// invlist.init();
		
		// for each tuple,
		while (read_tuple(input, tuple)) {
			if (prev_word > 0 || prev_word < tuple[0]) {
				// write inverted list on the inverted file -> get position (block number)
				// int poistion = invlist.writeToDisk();
				
				// add (prev_word, position) into b+ tree
				
				prev_word = tuple[0];
				cnt++;
				
				// initialize inverted list
				// invlist.init();
			}
			else if (prev_word < 0) {
				prev_word = tuple[0];
			}
				
			// append to the current inverted list
			// invlist.add(tuple[1], tuple[2]);
		}
		
		//if (invlist.size() > 0) {
			//int poistion = invlist.writeToDisk();
			// add (prev_word, position) into b+ tree
			// cnt++;
		//}
	}

	private boolean read_tuple (DataInputStream in, int[] tuple) throws IOException {
		try {
			tuple[0] = in.readInt();
		}
		catch (EOFException e) {
			return false;
		}
		
		try {
			tuple[1] = in.readInt();
			tuple[2] = in.readInt();
		}
		catch (EOFException e) {
			throw new IOException("invalid input posting file; wrong alignment");
		}
		
		return true;
	}
}
