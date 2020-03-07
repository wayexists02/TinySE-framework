package edu.hanyang.indexer;

/**
 * 단어의 배열을 가지고 있는 Query 클래스.
 * 단어의 배열에서, 순차적으로 그 단어를 검색하라고 하는 명령 sequence이다.
 */
public class Query {
	
	public int numOfWord; // 이 query에 들어갈 단어 검색명령 개수
	public int docID;	  // 이 query가 어느 document에서 검색하는 명령 sequence인지 나타냄
	public int[] query;   // -1: empty, -2: quotation mark("), other integer values: termid
	
	/**
	 * Query 생성자
	 * @param docID 이 query가 구동할 document의 id
	 * @param num	query가 검색할 단어의 개수
	 */
	public Query(int docID, int num) {
		numOfWord = num;
		this.docID = docID;
		query = new int[num];

		// 일단 query (단어) 배열을 모두 빈칸으로 채움
		// 여기에 각각 단어 id가 들어가서, 이 배열의 원소(단어)순으로
		// document에서 검색을 수행하게 됨
		// -1은 빈칸을 의미, -2는 quotation mark가 해당 자리에 있음을 의미하여,
		// 문장이 시작되거나 끝남을 알림
		for(int i = 0; i < query.length; i++) {
			query[i] = -1;
		}
	}
	
	/**
	 * 해당 단어를 검색하라는 명령(단어 id로 표시)을
	 * query 배열의 처음 빈 자리에 넣는다.
	 * @param termid 검색할 단어의 id
	 */
	public void put(int termid) {
		for(int i = 0; i < query.length; i++) {
			if(query[i] == -1) {
				query[i] = termid;
			}
		}
	}
	
	/**
	 * 해당 단어를 검색하라는 명령(단어 id로 표시)을
	 * query 배열의 빈 자리 중 아무곳에 넣는다.
	 * @param termid 검색할 단어의 id
	 */
	public void put_random(int termid) {
		int position = (int) (Math.random() * numOfWord);
		while(query[position] != -1) {
			// 빈자리 찾을때까지 loop
			position = (int) (Math.random() * numOfWord);
		}
		query[position] = termid;
	}
	
	//XXX: must put quotation query at first initialization of 'Query' class
	/**
	 * 문장 하나를 query 배열의 처음에 넣는다.
	 * 문장의 각 단어가 query 배열의 첫 일부분을 차지하게 된다.
	 * 단, 첫 번째 원소는 -2(quotation mark)로 시작한다.
	 * 따라서, query 배열의 length(문장)+2 길이만큼은 문장이 차지한다(quotation mark 2개 포함).
	 * @param termids
	 */
	public void put_quotation(int[] termids) {
		query[0] = -2;
		for(int i = 0; i < termids.length; i++) {
			query[i+1] = termids[i];
		}
		query[termids.length+1] = -2;
	}
	
	/**
	 * query에 빈 공간이 있는지 검사
	 * 빈 공간(-1)이 하나라도 배열에 있으면 true를 반환
	 * @return query배열에 빈 공간이 하나라도 있는가
	 */
	public boolean isEmpty() {
		for(int termid : query) {
			if(termid == -1) { return true; }
		}
		return false;
	}
	
	/**
	 * 해당 단어 id가 query에 존재하는가를 반환
	 * 즉, 해당 단어 id를 검색하라는 명령이 존재하는가를 반환
	 * @param val 단어 id
	 * @return 해당 단어 id가 query 배열에 있는가
	 */
	public boolean hasValue(int val) {
		for(int termid: query) {
			if(termid == val) { return true; }
		}
		return false;
	}
	
	/**
	 * query를 출력하는 메소드. Comma separated로 출력한다.
	 * 예시:
	 * [" 1 7 32 11 3 " 65 12]
	 */
	public void introduce() {
		System.out.print(docID+": ");
		System.out.print("[");
		for(int value : query) {
			if(value == -2) {
				System.out.print('"');
			}
			else {
				System.out.print(value+", ");
			}

		}
		System.out.println("]");
	}
}
