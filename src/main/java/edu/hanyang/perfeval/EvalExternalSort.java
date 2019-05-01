package edu.hanyang.perfeval;

import java.io.BufferedInputStream;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import edu.hanyang.indexer.ExternalSort;

public class EvalExternalSort {
	public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		try {
			String infile = "./test-10000000.data";
			String outfile = "./sort-10000000.data";
			String tmpdir = "./tmp";
			//		int blocksize = 4096;
			int blocksize = Integer.parseInt(args[1]);
			//		System.out.println("blocksize "+blocksize);
			//		int nblocks = 100;		
			int nblocks = Integer.parseInt(args[2]);
			//		System.out.println("nblocks "+nblocks);

			String filename = (String)args[0];
			File file = new File(filename);
			//		File file = new File("2014038304-0.0.1-SNAPSHOT.jar");

			URL url = file.toURI().toURL();
			System.out.println(url);
			URL[] urls = new URL[]{url};
			@SuppressWarnings("resource")
			ClassLoader cl = new URLClassLoader(urls);
			@SuppressWarnings("unchecked")
			Class<ExternalSort> cls = (Class<ExternalSort>) cl.loadClass("edu.hanyang.submit.TinySEExternalSort");

			//		Class<?> cls = Class.forName("edu.hanyang.submit.TinySEExternalSort");
			@SuppressWarnings("deprecation")
			ExternalSort sort = (ExternalSort) cls.newInstance();

			//TinySEExternalSort sort = new TinySEExternalSort();
			long s = System.currentTimeMillis();
			sort.sort(infile, outfile, tmpdir, blocksize, nblocks);
			long e = System.currentTimeMillis();

			String answerfile = "./result-10000000.data";
			@SuppressWarnings("resource")
			DataInputStream answerdis = new DataInputStream(new BufferedInputStream(new FileInputStream(answerfile)));
			@SuppressWarnings("resource")
			DataInputStream datadis = new DataInputStream(new BufferedInputStream(new FileInputStream(outfile)));

			for (int i = 0; i < 10000000 * 3; i++) {
				if (answerdis.readInt() != datadis.readInt()) {
					System.out.println("fail");
					System.exit(1);
				}
			}

			System.out.println("blocksize: "+blocksize+", nblocks: "+nblocks);
			System.out.println((e - s) / 1000.0);
		} catch (OutOfMemoryError e) {
			System.out.println(e.getMessage());
		}
	}
}
