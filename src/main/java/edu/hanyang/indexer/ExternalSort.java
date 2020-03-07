package edu.hanyang.indexer;

import java.io.IOException;

/**
 * 학생들이 구현할 ExternalSort 인터페이스
 * 학생들은 이 인터페이스를 implements하여 external sort용 class를 생성하고 구현해야 한다.
 */
public interface ExternalSort {

	/**
	 * 파일의 내용물을 정렬한다.
	 * @param infile 정렬할 내용물이 적혀 있는 파일
	 * @param outfile 정렬된 내용물이 적힐 파일
	 * @param tmpdir 정렬할때, 중간 결과물을 저장할 디렉토리
	 * @param blocksize 정렬할때 사용할 메모리 블록 하나의 크기
	 * @param nblocks 정렬할때 사용할 메모리 블록 개수
	 * @throws IOException
	 */
	public void sort(String infile, String outfile, String tmpdir, int blocksize, int nblocks) throws IOException;
}
