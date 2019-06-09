package edu.hanyang.perfeval;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.net.URL;
import java.net.URLClassLoader;

//import edu.hanyang.submit.TinySEBPlusTree;
import edu.hanyang.indexer.BPlusTree;


// This code is for checking with small dataset before final evaluation.
// It is working on eclipse.
// To test your result, please refer the "testing on" line 36.

public class EvalBPlusTreeInsert {
	public static void main(String[] args) throws Exception {
//		if (args.length < 7) {
//			System.out.println("<metafile path> <file path> <block size> <# of blocks> <studnet id> <input file> <student's jar file>");
//			System.exit(1);
//		}

//		String metafile = args[0];
//		String filepath = args[1];
//		int blocksize = Integer.valueOf(args[2]);
//		int nblocks = Integer.valueOf(args[3]);
//		String studentID = args[4];
//		studentID = studentID.split("_")[0];
//		String inputfile = args[5];
//		String filename = (String)args[6];
		
//		// testing on
		String metafile = "./metafile.meta";
		String filepath = "./tree.tree";
		int blocksize = 8192;
		int nblocks = 1000;
		String studentID = "2014";
		String inputfile = "./treetest-15000000.data";
		String filename = "9366020182-0.0.1-SNAPSHOT.jar";

//		BufferedWriter bw = new BufferedWriter(new FileWriter(studentID + "_insert", true));

		FileInputStream fis = new FileInputStream(inputfile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		DataInputStream dis = new DataInputStream(bis);

//		TinySEBPlusTree tree = new TinySEBPlusTree();
		// dynamic loading from student's submission
		File file = new File(filename);
		URL url = file.toURI().toURL();
		URL[] urls = new URL[] {url};
		ClassLoader cl = new URLClassLoader(urls);
		Class<BPlusTree> cls = (Class<BPlusTree>) cl.loadClass("edu.hanyang.submit.TinySEBPlusTree");
		BPlusTree tree = (BPlusTree) cls.newInstance();
		
		try {
			tree.open(metafile, filepath, blocksize, nblocks);
		} catch (Exception e) {
//			bw.write(e.getMessage());
			System.err.println(e.getMessage());
			System.exit(1);
		}

		long start = System.currentTimeMillis();
//		for (int i = 0; i < 15000000; i++) {
		for (int i = 0; i < 10000; i++) {
			if (i%1000 == 0)
				System.out.println(i+"-th insertion");
			int key = dis.readInt();
			int val = dis.readInt();
			try {
				tree.insert(key, val);
			} catch (Exception e) {
				System.out.println("");
				System.out.println("Insert Error!");
				System.out.println("");
//				bw.write(e.getMessage());
				System.err.println(e.getMessage());
				tree.close();
				System.exit(1);
			}
		}
		tree.close();
		long end = System.currentTimeMillis();

		System.out.println((end - start) / 1000.0 + " sec");
//		bw.write(nblocks + " " + ((end - start) / 1000.0));
//		bw.newLine();
		fis.close();
		bis.close();
		dis.close();

//		bw.close();
	}
}
