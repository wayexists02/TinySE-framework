package edu.hanyang.indexer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.TreeMap;

/**
 * 학생들이 구현한 토크나이저 한국어 버전을 테스트하는 클래스
 * 어떤 디렉토리 안에 있는 모든 파일들의 내용물을 토큰화한 후 id값으로 대체한다.
 * 코드 및 주석에서 나오는 token과 term은 같은 의미로 사용된다.
 * 
 * 1. 학생들이 구현한 토크나이저 클래스로부터 객체를 생성하고,
 * 2. 테스트용 파일들에 대해 토크나이저를 돌려보고
 * 3. 하나의 최종 파일에 그 결과를 저장한다.
 */
public class TokenizeKor {
	
	private Tokenizer tokenizer = null;
	
	private int current_termid = 0;
	private TreeMap<String, Integer> termids = new TreeMap<String, Integer> ();
	
	/**
	 * 생성자.
	 * 학생들이 구현한 토크나이저 클래스를 찾고, 객체화시킨다.
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public TokenizeKor () throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		// external code binding
		// 학생들이 구현한 토크나이저 클래스를 동적으로 불러옴
		Class<?> cls = Class.forName("edu.hanyang.submit.TinySETokenizer");

		// 그리고 객체화
		tokenizer = (Tokenizer) cls.newInstance();
	}
	
	/**
	 * term 또는 token에 대해 그 id를 반환한다.
	 * termids 라는 매핑 테이블(term -> term id)을 참조한다.
	 * @param term 토큰 또는 term
	 * @return term id 또는 token id
	 */
	private int get_termid (String term) {

		// 만약, 해당 토큰이 매핑테이블에 있으면 그 id를 반환하고,
		// 없다면, 새로운 id값과 함께 매핑시켜 테이블에 추가한 후 id를 반환한다.
		if (termids.containsKey(term)) {
			return termids.get(term);
		}
		else {
			termids.put(term, current_termid);
			return current_termid++;
		}
	}
	
	/**
	 * "어떤 단어 토큰"  -> 토큰 id값
	 * 매핑을 termidfile에 기록함
	 * termidfile은 newline-separated value 형태로 기록됨
	 * 파일의 한줄은 "token	   2" 이런식으로 됨(tab이 사이에 있음)
	 * @param termidfile 토큰-토큰id 매핑테이블을 기록할 파일 경로
	 * @throws IOException
	 */
	private void write_termids (String termidfile) throws IOException {
		try (PrintStream os = new PrintStream(new BufferedOutputStream(new FileOutputStream(termidfile)) )) {
			// 매핑 테이블 전체를 돌면서 기록
			for (String term: termids.keySet()) {
				os.println(term + "\t" + termids.get(term));
			}
		}
	}
	
	/**
	 * dir라는 디렉토리 안에 있는 파일들을 모두 tokenize시켜본다.
	 * 각 파일을 toeknize시킨 후, 각 token들을 id값으로 매핑(term -> term id)까지 시킨다.
	 * 그 결과를 postings 파일 1개에 모두 기록함
	 * @param dir
	 * @param outputfile
	 * @param termidfile
	 * @throws IOException
	 */
	public void run (String dir, String outputfile, String termidfile) throws IOException {
		try (DataOutputStream postings = new DataOutputStream(new BufferedOutputStream (new FileOutputStream (outputfile)))) {
			// initialize tokenizer
			tokenizer.setup();
			
			// dir 디렉토리에 있는 모든 파일명 리스트를 읽어옴
			File directory = new File(dir);
			File[] contents = directory.listFiles();
			
			// 파일명 리스트를 돌면서
			for (File f : contents) {

				// 파일이면,
				if (f.isFile()) {
					// 파일명이 integer인가 봅니다
					int docid = Integer.parseInt(f.getName());
					
					// 각 term(token) 위치도 함께 기록하기 위함
					int pos = 0;
					try (BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath()))) {
	
						// line-by-line 으로 읽으면서 tokenize
						String line = null;
						while ((line = br.readLine()) != null) {
							// 토큰별로 자름
							List<String> arr = tokenizer.split(line);
							
							// 토큰을 term-id로 매핑함
							for (String term: arr) {
								int id = get_termid (term);
								
								// 매핑하면서 postings에 기록하는데, term-id, document id, position 정보를 함께 기록함
								postings.writeInt(id);
								postings.writeInt(docid);
								postings.writeInt(pos++);
							}
						}
	
					} catch (IOException e) {
						System.err.println("[warn] fail to read file: " + f.getAbsolutePath());
					}
				}
			}
		}
		finally {
			// finalize tokenizer
			tokenizer.clean();
			
			// write term and termid pairs
			write_termids(termidfile);
		}
	}

	public static void main(String[] args) throws Exception {
		// 인자 개수 체크
		if (args.length != 3) {
			System.err.print("usage: <directory to corpus> <output posting list file> <termid mapping file>");
			System.exit(1);
		}
		
		// 토크나이저 테스트 객체 생성 및 실행
		TokenizeKor indexer = new TokenizeKor();
		indexer.run(args[0], args[1], args[2]);
	}

}
