package edu.hanyang.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import edu.hanyang.indexer.Query;

/**
 * 테스트용 query를 생성하는 클래스
 * Query 1개는 termid(단어 id)로 구성된 배열 하나로, 그 배열 내의
 * 단어들을 순차적으로 검색하라는 명령이다.
 */
public class MakeTestData {
	final int QUERY_MIN_SIZE = 4;
	final int QUERY_MAX_SIZE = 10;
	final double QUERY_GENERATION_RATIO = 0.0005;
	final double QUERY_ADDITION_RATIO = 0.2;
	String filepath = "../all-the-news/";
	String filename = "InvertedTripleList.data";
	
	DataInputStream dis = null;
	DataOutputStream dos = null;
	
	// make test query (termid) without quotation marks(")
	/**
	 * Quotation mark가 없는, 즉, 단어만 검색하는 명령만으로 구성된 query를 number개 만든다.
	 * @param number 만들 query의 개수
	 */
	public void makeQuery(int number) {
		int numOfQuery = 0;		// 현재 만든 query의 개수
		int wordID = 0;			// 지금 읽은 단어 id
		int docID = 0;			// 지금 읽고있는 document id
		int pos = 0;			// 안쓰임
		boolean making = false;	// 지금 query 1개를 제작중이라는 의미. 
								// false면 1개가 모두 완성된 이후거나, 새로운 query를 만들어야 됨을 의미.

		Query query = new Query(-1, 0); // initialization is meaningless but for <Errors: May not have been initialized>
		
		try {
			// dis: tokenizer로 토큰화된 문서들이 적힌 postings 파일
			// dos: 만들어진 query들이 기록될 파일
			dis = new DataInputStream(new BufferedInputStream(new FileInputStream(filepath+filename)));
			dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filepath+"TestQuery.data")));

			while(numOfQuery < number) {
				// 원하는 개수의 query가 만들어질 때 까지

				// postings 파일은 한줄한줄이 <word id> <doc id> <position> 로 기록되어 있으므로, 한줄을 읽어옴
				wordID = dis.readInt();
				docID = dis.readInt();
				pos = dis.readInt();
				
				if(!making && Math.random() < QUERY_GENERATION_RATIO) {
					// 모든 document에 대해 query를 만드는게 아니고, 확률적으로 만듦. 즉, 어떤 document를 검색하는 query는 없을수도,
					// 여러개일수도 있음
					// 이 query는 두 번째 인자로 들어간 수의 개수만큼의 단어를 검색하는 query임.
					// query는 두번째 인자로 들어간 수의 개수만큼의 길이의 배열을 생성
					query = new Query(docID, (int) (Math.random() * (QUERY_MAX_SIZE - QUERY_MIN_SIZE + 1)) + QUERY_MIN_SIZE);
					making = true; // query가 제작중임 으로 표시. 제작중에는 아예 새로운 query가 만들어지면 안되니까
				}
				else if(making) {
					// query를 제작중이면 여기를 실행한다.

					if(docID != query.docID) {
						// out of document
						// 만약, query 제작중에 지금 document가 끝나버리면,
						// 제작중인 query는 버린다.
						query = null;
						making = false;
					}
					else if(Math.random() < QUERY_ADDITION_RATIO) {
						// 한 document의 모든 단어에 대해 query를 구성하는 것이 아니라
						// 확률적으로 구성한다. 즉, 어떤 단어에 대한 검색 명령은 query에
						// 없을 수도 있다.

						if(query.isEmpty() && !query.hasValue(wordID)) {
							// query.isEmpty() 가 아니라 !query.isFull()로 하는게 맞을듯.
							// 즉, query에 빈자리 하나라도 있으면 여기 코드를 실행
							// 이미 query에 같은 단어가 있으면 실행안함

							// 현재 읽고 있는 단어를 query에 넣는데, 빈자리중 랜덤 위치로 넣음.
							query.put_random(wordID);
							
							// query가 가득찼으면 dos파일에 쓴다.
							if(!query.isEmpty()) {
								for(int value : query.query) {
									dos.writeInt(value);
								}

								// query의 끝을 나타내는 문자인듯.
								dos.writeInt(-1);

								// 만든 query 개수 +1
								numOfQuery++;
							}
						}
					}
					else {
						continue;
					}
				}
			}
			dis.close();
			dos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// make test query (termid) with quotation marks(")
	/**
	 * 문장을 검색하는 명령도 포함된 query를 생성
	 * @param number 만들 query개수
	 */
	public void makeQueryWithQuotation(int number) {
		int numOfQuery = 0;					// 현재 만들어진 query 개수
		int wordID = 0;						// 지금 읽은 word id
		int docID = 0;						// 지금 읽고있는 document id
		int pos = 0;						// 안쓰임
		boolean making = false;				// true이면, query 1개를 만드는 중임을 의미.
		boolean quoteQueryMaking = false;	// true이면, 문장 검색 명령을 만드는 중임을 의미.
		Query query = new Query(-1, 0); // initialization is meaningless but for <Errors: May not have been initialized>
		
		int[] quoteQuery = null;
		int quoteQueryPos = 0;
		
		try {
			// dis: tokenizer로 토큰화된 문서들이 적힌 postings 파일
			// dos: 만들어진 query들이 기록될 파일
			dis = new DataInputStream(new BufferedInputStream(new FileInputStream(filepath+filename)));
			dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filepath+"TestQuery.data", true)));
			
			while(numOfQuery < number) {
				// 원하는 개수의 query가 나올 때까지 실행

				// postings 파일은 한줄한줄이 <word id> <doc id> <position> 로 기록되어 있으므로, 한줄을 읽어옴
				wordID = dis.readInt();
				docID = dis.readInt();
				pos = dis.readInt();
				
				if(!making && Math.random() < QUERY_GENERATION_RATIO && !quoteQueryMaking) {
					// 모든 document에 대해 query를 만드는게 아니고, 확률적으로 만듦. 즉, 어떤 document를 검색하는 query는 없을수도,
					// 여러개일수도 있음
					// 이 query는 두 번째 인자로 들어간 수의 개수만큼의 단어를 검색하는 query임.
					// query는 두번째 인자로 들어간 수의 개수만큼의 길이의 배열을 생성
					query = new Query(docID, (int) (Math.random() * (QUERY_MAX_SIZE - QUERY_MIN_SIZE + 1)) + QUERY_MIN_SIZE + 2);
					making = true;

					// 문장이 들어갈 배열(원소 하나하나는 단어 id에 해당함)
					quoteQuery = new int[(int) (Math.random() * (query.query.length - 3)) + 2];

					// 문장이 다 안만들어졌으면 true
					quoteQueryMaking = true;
				}
				else if(making) {
					// query를 만드는 중이면 실행

					if(docID != query.docID) {
						// out of document
						// Document가 끝나고 다음 document로 넘어가면,
						// 만들고 있던 query는 버림

						query = null;
						making = false;
						quoteQueryPos = 0;
						quoteQueryMaking = false;
					}
					else if(quoteQueryMaking) {
						// 문장 검색 명령을 만드는 중이면 실행

						if(quoteQueryPos < quoteQuery.length) {
							// 문장이 아직 다 안만들어졌으면(길이만큼 안찼으면)

							// 이때는 word를 읽은 순서대로 차례대로 넣음.
							// 즉, document 에 있는 단어 순서대로 넣음(그래야 문장이니까)
							quoteQuery[quoteQueryPos] = wordID;
							quoteQueryPos++;
						}
						else {
							// 문장이 길이만큼 만들어졌으면 query의 제일 앞부분에 삽입
							query.put_quotation(quoteQuery);

							// 이번 query에서는 이제 더 이상 문장 검색 명령을 만들지 않음.
							// 단, 한 document에 query는 여러개일 수 있으므로, document당 문장은 여러개일수도 있음
							quoteQuery = new int[(int) (Math.random() * (QUERY_MAX_SIZE - QUERY_MIN_SIZE + 1))];
							quoteQueryMaking = false;
						}
					}
					else if(Math.random() < QUERY_ADDITION_RATIO) {
						// 문장도 다 만들었으나, query 배열이 다 차지 않은 경우, 현 document의 현재 단어를 확률적으로 query에 넣음

						if(query.isEmpty() && !query.hasValue(wordID)) {
							// query가 가득 차지 않고, 현재 단어도 query에 없다면,

							// query의 아무 순서위치에 현재 단어 검색을 넣음
							query.put_random(wordID);
							
							if(!query.isEmpty()) {
								// 가득찼으면 dos에 이 query 저장.
								for(int value : query.query) {
									dos.writeInt(value);
								}
								dos.writeInt(-1);
								numOfQuery++;
							}
						}
					}
					else {
						continue;
					}
				}
			}
			dis.close();
			dos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		MakeTestData mtd = new MakeTestData();
		mtd.makeQuery(7000);
		mtd.makeQueryWithQuotation(3000);
	}
}
