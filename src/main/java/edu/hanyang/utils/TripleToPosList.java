package edu.hanyang.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import edu.hanyang.indexer.BPlusTree;

/**
 * 토큰화 결과로 나온 파일을 디스크에 쓴 후
 * 디스크에 쓴 것을 B+ tree로 로드하는 것이 동작하는지 테스트하는 클래스
 * 즉, B+ tree의 구현을 테스트하는 것이다.
 * 
 * 토큰화 결과로 나온 파일은 다음처럼 한 줄이 구성되어 있음
 * 
 * ==================================
 * <word id> <doc id> <position>
 * <word id> <doc id> <position>
 * ...
 * ==================================
 * 
 * 이것을 다음 형식으로 디스크에 씀
 * (보기 편하게 다음처럼 적은 것이지, 실제로 저렇게 사람이 읽을 수 있는 형태로
 * 적히지는 않음. 다만 적히는 순서가 다음처럼 된다는 의미)
 * 
 * ==================================
 * <block count>
 * <number of docs>
 * <starting doc id>
 * <ending doc id>
 * <block content>
 * ==================================
 * 
 * 여기서, <block content>에는 document의 content가 들어 있음
 * <starting doc id>는 디스크에 쓸 document들 중 첫 번째 document id,
 * <ending doc id>는 디스크에 쓸 document들 중 마지막 document id
 */
public class TripleToPosList {
	String filepath = "../all-the-news/";
	int blocksize = 52;
	
	/**
	 * byteBufferWrite() 로 적힌 파일이 제대로 적혔는지 확인하는 메소드
	 * 다시 그 파일을 읽어와서 내용물을 찍어본다.
	 * 모두 찍어보는게 아니라, targetWord 에 들어온 번호의 덩어리를 찍어봄
	 * 
	 * byteBufferWrite()를 한번 호출하면 document가 여러개 쓰여지는데,
	 * 이때, byteBufferWrite()를 한번 호출했을 때, 적히는 양을 덩어리라고 정의해본다;
	 * 덩어리는 몇 개의 블록으로 구성되어 있고, 각 블록에 여러 document가 나뉘어 저장되어 있다.
	 * 
	 * @param filename 디스크에 적은 파일명
	 * @param targetWord 출력해보고 싶은 덩어리 번호
	 */
	public void introduce(String filename, int targetWord) {
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(filepath+filename, "r");
			long offset = 0;
			int numOfBlock = raf.readInt();

			for(int i = 0; i < targetWord; i++) {
				offset += 16 + numOfBlock * blocksize;
				raf.seek(offset);
				numOfBlock = raf.readInt();
			}
			
			byte[] buf = new byte[blocksize];
			int cnt = 0;
			int numOfPos = 0;
			boolean newDoc = true;
		
			// 이 덩어리의 헤더 정보 출력
			System.out.println("<HEADER>");
			System.out.println("Blocks: "+numOfBlock);
			System.out.println("Documents: "+raf.readInt());
			System.out.println("Min Doc: "+raf.readInt());
			System.out.println("Max Doc: "+raf.readInt());
			System.out.println("<CONTENT>");
			
			// 덩어리의 내용을 모두 출력
			while(cnt < numOfBlock) {
				raf.readFully(buf);
				DataInputStream pkt = new DataInputStream(new ByteArrayInputStream(buf));
				int capacity = 0;
				
				CheckIsItFull : while(capacity < blocksize) {
					if(newDoc) {
						System.out.print("["+pkt.readInt()+"] ");
						capacity = capacity + 4;
						newDoc = false;
						continue CheckIsItFull;
					}
					else if(numOfPos == 0) {
						numOfPos = pkt.readShort();
						System.out.print("("+numOfPos+")\t");
						capacity = capacity + 2;
						continue CheckIsItFull;
					}
					else if(numOfPos > 1){
						System.out.print(pkt.readShort()+"\t");
						capacity = capacity + 2;
						numOfPos--;
						continue CheckIsItFull;
					}
					else {
						System.out.print(pkt.readShort()+"\t");
						capacity = capacity + 2;
						numOfPos--;
						newDoc = true;
						if (capacity == blocksize-2) { break CheckIsItFull; }
						continue CheckIsItFull;
					}
				}
				System.out.println();
				cnt++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Make posting list from sorted inverted list data
	//	<Header> : # of Blocks, # of Docs, Min Doc, Max Doc (all int)
	//	<Content> : Doc_id (int), # of pos, pos1, pos2, ... (short)
	/**
	 * 토큰화된 파일을 디스크에 쓴다.
	 * 참고로, 파일 1개는 여러 document의 내용이 쓰여진 파일임
	 * @param filename 토큰화된 파일 이름
	 */
	public void readDataFile(String filename) {
		DataInputStream dis = null;
		try {
			dis = new DataInputStream(new BufferedInputStream(new FileInputStream(filepath+filename)));
			int currentWordID = 0;
			int currentDocID = -1;
			List<Short> content = new ArrayList<>();
			// -1, 0, # of pos, pos1, pos2, ..., -1, 1, # of pos, pos1, pos2, ... --> List<Short> content
			// <--------- Doc 0 ------------> <---------- Doc 1 ----------->
			List<Integer> docID = new ArrayList<>();
			int cnt = 0;
			int numOfDoc = 0;
			int numOfPos = 0;
			int currentVal = 0;

			// <WordID, DocID, Position>
			// 디스크에 쓰기 좋도록 토큰화된 파일 내에 적힌 형태를 하나의 리스트로 파싱
			while((currentVal = dis.readInt()) != -1) {
				if (cnt%3 == 2) { // Here comes a new position
					content.add((short) currentVal);
					numOfPos++;
				}
				else if (cnt%3 == 0 && currentWordID != currentVal) { // Here comes a new word
					content.set(content.size()-numOfPos-1, (short) numOfPos);
					byteBufferWrite(numOfDoc, docID, content);
					currentWordID = currentVal;
					currentDocID = -1;
					numOfDoc = 0;
					docID.clear();
					content.clear();
				}
				else if (cnt%3 == 1 && currentDocID != currentVal) { // Here comes a new document
					if (!content.isEmpty()) { content.set(content.size()-numOfPos-1, (short) numOfPos); }
					content.add((short) -1); // -1 means starting of new document
					content.add((short) 0); // make a room for # of positions
					docID.add(currentVal);
					numOfPos = 0;
					currentDocID = currentVal;
					numOfDoc++;
				}
				cnt++;
			}
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 학생들이 구현한 B+ tree 구현 클래스를 로드하고 객체화
	 * @return B+ tree 객체
	 */
	public BPlusTree loadclass () {
		// external code binding
		Class<?> cls;
		BPlusTree tree = null;
		
		try {
			cls = Class.forName("edu.hanyang.submit.TinySEBPlusTree");
			tree = (BPlusTree) cls.newInstance();
			
		} catch (ClassNotFoundException e) {
			System.err.println("[error] cannot find class edu.hanyang.submit.TinySEBPlusTree");
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return tree;
	}
	
	// Make B+Tree Posting List
	/**
	 * 여러번의 byteBufferWrite() 호출의 결과로 디스크에 저장된 파일을 읽어와서
	 * 각 덩어리(한 번의 byteBufferWrite() 호출로 디스크에 쓰인 단위, document 여러개)들의 헤더를 이용해서
	 * B+ tree를 만들어봄
	 * @param filename 디스크
	 */
	public void readDataFileToTree(String filename) {
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(filepath+filename, "r");
			long offset = 0;
			int currentWordID = 0;
			int numOfBlock = 0;
			
			// B+ tree 객체 생성
			BPlusTree tbt = loadclass();
			tbt.open("metapath", filepath+"bplustree.tree", blocksize, 10);
			tbt.insert(currentWordID, numOfBlock);
	
			// 디스크에 저장된 덩어리들의 첫 부분만 읽어온다.
			// 여기서, 덩어리란, byteBufferWrite() 한 번의 호출로 저장된 블록들이다.
			// 덩어리들의 첫 부분은 블록 개수이므로, 각 덩어리들의 블록 크기를 읽어옴
			while((numOfBlock = raf.readInt()) != -1) {
				currentWordID++;						// B+ tree에서, 각 덩어리들에 할당된 key값
				offset += 16 + numOfBlock * blocksize;	// 이 덩어리의 크기 계산
				tbt.insert(currentWordID, (int)offset);	// 덩어리의 크기를 tree에 쓴다.

				// 다음 덩어리 위치로 점프
				raf.seek(offset);
			}

			// 트리와 디스크 모두 닫음
			tbt.close();
			raf.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * content 배열 -> 바이트 버퍼 -> 디스크
	 * 이 순서로 쓴다.
	 * content 배열에서 버퍼크기만큼 버퍼에 담고 버퍼가 가득 차면 디스크로 쓴다.
	 * 버퍼 = 블록 이라고 이해하면 됨
	 * @param numOfDoc document 총 개수
	 * @param docID document ID가 있는 배열. id는 중복되지 않음
	 * @param content word id들이 있는 배열. 즉, document들이 content들이 있는 배열
	 * @throws IOException
	 */
	public void byteBufferWrite(int numOfDoc, List<Integer> docID, List<Short> content) throws IOException {
		int blockcnt = 1;
		int docIDCursor = 0;
		byte[] buf = new byte[blocksize];
		ByteBuffer bf = ByteBuffer.wrap(buf);
		RandomAccessFile raf = new RandomAccessFile(filepath+"PostingList.data", "rw");
		raf.seek(raf.length());
	
		// 지금 쓰고 있는 블록의 정보를 디스크에 기록
		raf.writeInt(blockcnt); // Room for Header value 1			// 몇 개의 블록이 필요한지(이건 나중에 업데이트됨. 일단 1로 쓴다)
		raf.writeInt(numOfDoc); // Header value 2					// document가 몇개나 디스크에 써졌는지,

		// 첫 번째와 마지막 document가 무엇인지 디스크에 기록
		raf.writeInt(docID.get(0)); // Header value 3				// 첫 document의 id
		raf.writeInt(docID.get(docID.size()-1)); // Header value 4	// 마지막 document의 id
		
		// 모든 content 내용을 기록한다.
		for(int i = 0; i < content.size(); i++) {
			if (bf.position() == bf.capacity() || (bf.position() == bf.capacity()-2 && content.get(i) == -1)) {
				// ByteBuffer is full <OR> No room for DocID value (Integer)
				// 1. 버퍼(블록)가 가득 차거나
				// 2. 한 document를 다 써서, 새 document로 넘어가야 하는데, document id를 쓸 공간이 버퍼에 없거나

				raf.write(buf);	// 버퍼를 디스크에 씀
				bf.clear();		// 버퍼를 비움
				blockcnt++;		// 버퍼를 하나 다 썼다는 것은 블록 하나를 다 썼다는 것이므로, 블록개수 +1
			}
			if(content.get(i) == -1) {
				// document 하나를 다 써서(그 content들을 다 씀)
				// 다음 document로 넘어가야 한다면

				bf.putInt(docID.get(docIDCursor));	// 먼저, document id를 써줌. 다음 루프부터는 이 document의 content가 들어감
				docIDCursor++;						// document 포인터를 한칸 뒤로
			}
			else {
				// 버퍼도 아직 남았고, 한 document를 쓰는 도중일때,
				// 그 document의 content word 하나를 버퍼에 쓴다.
				// 즉, content를 버퍼에 쓰는 과정임

				bf.putShort(content.get(i));		// i번째 word를 버퍼에 쓴다.
			}
		}

		raf.write(buf);									// 버퍼가 가득 채워지지 않았더라도, 남은 내용이 없으므로, 디스크에 마저 씀
		bf.clear();										// 버퍼를 비움
		raf.seek(raf.length()-blockcnt*blocksize-16);	// 디스크를 쓰기 시작한 첫 부분으로 이동(처음에 block count를 쓴 위치)
		raf.writeInt(blockcnt); // Header value 1		// 블록 개수 업데이트

		raf.close();									// 디스크 IO 반환
	}

	public static void main(String[] args) throws FileNotFoundException {
		TripleToPosList ttp = new TripleToPosList();
		ttp.readDataFile("SortedInvertedTripleList.data");
//		ttp.readDataFileToTree("PostingList.data");
//		ttp.introduce("PostingList.data", 1);
	}
}
