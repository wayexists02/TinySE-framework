package edu.hanyang.indexer;

import java.util.List;

/**
 * 학생들이 구현할 인터페이스.
 * 이 인터페이스를 implements하여 학생들이 class를 만들고 각 method를 구현해야 한다.
 */
public interface Tokenizer {

	/**
	 * tokenizer initialization 코드 구현
	 */
	public void setup();
	
	/**
	 * tokenization을 실행하는 코드 구현
	 * @param str 토큰화를 수행할 문자열
	 * @return 토큰들의 배열
	 */
	public List<String> split (String str);

	/**
	 * tokenizer finalization코드. 사용헀던 자원의 정리 등을 수행
	 */
	public void clean();
}
