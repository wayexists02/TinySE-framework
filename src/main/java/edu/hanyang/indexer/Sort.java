package edu.hanyang.indexer;

import java.io.IOException;

/**
 * 학생들이 구현한 external sort class를 테스트하기 위한 코드
 * 테스트용 파일의 내용물을 정렬하는 역할을 수행한다.
 */
public class Sort {
	
	private ExternalSort extsort = null;
	
	/**
	 * 생성자.
	 * 학생들이 구현한 external sort 클래스를 불러오고 객체화한다.
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public Sort () throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		
		// external code binding
		Class<?> cls = Class.forName("edu.hanyang.submit.TinySEExternalSort");
		extsort = (ExternalSort) cls.newInstance();
	}
	
	/**
	 * 주어진 파일의 내용물의 정렬을 수행한다.
	 * @param infile 정렬할 내용물이 들어 있는 파일
	 * @param outfile 정렬된 내용물이 저장될 파일
	 * @param tmpdir 정렬 과정에서 중간 저장물이 저장될 디렉토리
	 * @param blocksize 정렬 과정에서 이용될 메모리 블록 하나의 크기
	 * @param nblocks 정렬 과정에서 이용될 메모리 블록의 개수
	 * @throws IOException
	 */
	public void run (String infile, String outfile, String tmpdir, int blocksize, int nblocks) throws IOException {
		extsort.sort(infile, outfile, tmpdir, blocksize, nblocks);
	}

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		// 인자 개수 체크
		if (args.length != 5) {
			System.err.print("usage: <inputfile> <outputfile> <temporary directory> <block size> <number of available blocks>");
			System.exit(1);
		}
		
		// 블록 하나의 크기와 블록 개수
		int blocksize = Integer.parseInt(args[3]);
		int nblocks = Integer.parseInt(args[4]);
		
		Sort sort = new Sort ();
		sort.run(args[0], args[1], args[2], blocksize, nblocks);
	}

}
