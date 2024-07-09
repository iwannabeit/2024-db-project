
<?php

// 데이터베이스 접속 정보
$dbHost = "127.0.0.1"; // 데이터베이스 호스트 주소
$dbUser = "root"; // 데이터베이스 사용자 이름
$dbPass = "jaewoo"; // 데이터베이스 사용자 비밀번호
$dbName = "wifi2"; // 데이터베이스 이름

// POST 요청으로 전달된 데이터
$latitude = $_POST["latitude"];
$longitude = $_POST["longitude"];
$description = $_POST["description"];

// 데이터베이스 연결 생성
$conn = mysqli_connect($dbHost, $dbUser, $dbPass, $dbName);

// 연결 확인
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// 데이터베이스에 데이터 추가
$sql = "INSERT INTO markers (latitude, longitude, description) VALUES ('$latitude', '$longitude', '$description')";

if ($conn->query($sql) === TRUE) {
    echo "New record created successfully";
} else {
    echo "Error: " . $sql . "<br>" . $conn->error;
}

// 데이터베이스 연결 종료
$conn->close();

?>
