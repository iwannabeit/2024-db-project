<?php
header("Content-Type: application/json; charset=UTF-8");

// 데이터베이스 연결 정보 설정
$servername = "127.0.0.1";
$username = "root";
$password = "jaewoo"; // 데이터베이스 비밀번호
$dbname = "wifi2"; // 데이터베이스 이름

// 데이터베이스 연결
$conn = new mysqli($servername, $username, $password, $dbname);

// 연결 확인
if ($conn->connect_error) {
    die(json_encode(array("success" => false, "message" => "Database connection failed: " . $conn->connect_error)));
}

// POST 데이터 가져오기
$data = json_decode(file_get_contents('php://input'), true);
$latitude = $data['latitude'];
$longitude = $data['longitude'];
$description = $data['description'];

// SQL 쿼리 작성 및 실행
$sql = "INSERT INTO markers (latitude, longitude, description) VALUES (?, ?, ?)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("dds", $latitude, $longitude, $description);

if ($stmt->execute()) {
    echo json_encode(array("success" => true, "message" => "Marker added successfully"));
} else {
    echo json_encode(array("success" => false, "message" => "Error: " . $stmt->error));
}

// 연결 종료
$stmt->close();
$conn->close();
?>

