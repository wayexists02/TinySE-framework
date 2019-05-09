package edu.hanyang.indexer;

import java.io.IOException;

public interface BPlusTree {
	public void open(String metafile, String filepath, int blocksize, int nblocks)  throws IOException;
	public void insert(int key, int val)  throws IOException;
	public int search(int key)  throws IOException;
	public void close() throws IOException;
}
