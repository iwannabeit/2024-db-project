<?php

// 데이터베이스 연결 정보
$servername = "127.0.0.1"; // MySQL 서버 주소
$username = "root"; // MySQL 사용자명
$password = ""; // MySQL 비밀번호
$dbname = "dbproject"; // 사용할 데이터베이스명

// MySQLi 연결 생성
$conn = mysqli_connect($servername, $username, $password, $dbname);

// 연결 확인
if (!$conn) {
  die("연결 실패: " . mysqli_connect_error());
}

echo "연결 성공!";


?>