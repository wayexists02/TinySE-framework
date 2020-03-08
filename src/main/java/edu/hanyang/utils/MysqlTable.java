package edu.hanyang.utils;

import java.sql.*;

public class MysqlTable {
	// 데이터베이스 주소
	private static final String dbhost = "localhost";

	// MySQL 데이터베이스와의 연결 상태 또는 드라이버를 저장하는 객체
    private static Connection conn = null;
    
    private static PreparedStatement get_doc_stmt = null;
	
	/**
	 * MySQL 데이터베이스와 연결을 시작함
	 * @param dbname 데이터베이스 이름
	 * @param dbuser MySQL 사용자 id
	 * @param dbpass MySQL 사용자 password
	 * @throws Exception
	 */
	public static void init_conn(String dbname, String dbuser, String dbpass) throws Exception {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		conn = DriverManager.getConnection("jdbc:mysql://" + dbhost + "/" + dbname, dbuser, dbpass);

		// prepare statements
		// query의 placeholder를 정의.
		// '?'자리는 나중에 채워야 함
		// 아직 query를 실행하지는 않고, query 객체를 준비만 함.
		get_doc_stmt = conn.prepareStatement("select txt from docs where id=?");
	}
	
	/**
	 * MySQL 데이터베이스와 연결을 끊음
	 * @throws Exception
	 */
	public static void final_conn () throws Exception {
		conn.close();
	}
	
	/**
	 * docs 데이터베이스 테이블로부터 txt 를 읽어오는데,
	 * id값이 해당 인자로 받은 id값인 것만 읽어옴
	 * @param id docs 테이블에서 읽을 row의 id 조건
	 * @return
	 */
	public static String get_doc(long id) {
		String res = null;
		try {
			get_doc_stmt.setLong(1, id);				// '?'부분을 id(인자로 받은)로 대체
			ResultSet rs = get_doc_stmt.executeQuery();	// query 실행
			rs.next();									// 첫 번째 row로 커서 이동
			res = rs.getString("txt");					// 첫 번째 row의 txt컬럼만 받아옴
			rs.close();									// result set 객체 소멸
		} catch (SQLException e) {
			System.err.println("[db error] get_message: " + e.getMessage());
		}
		
		return res;
	}
}

