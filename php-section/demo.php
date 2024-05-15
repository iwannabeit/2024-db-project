<?php
ini_set('memory_limit', '-1');
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

$sql = "SELECT DISTINCT * FROM jjwifi";
$result = mysqli_query($conn, $sql);
$data = array();

if($result){
  while($row=mysqli_fetch_array($result)){
    array_push($data,
    array('id'=>$row[0],
    'region'=>$row[1],
    'city'=>$row[2],
    'address'=>$row[3],
    'place'=>$row[4],
    'x'=>$row[5],
    'y'=>$row[6]
    ));
  }
  $response = array("jwifi" => $data);

  header('Content-Type: application/json; charset=utf8');
  $json=json_encode($response, JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
  echo $json;
}

?>