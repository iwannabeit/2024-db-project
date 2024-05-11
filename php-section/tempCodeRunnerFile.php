<?php
/* PHP 샘플 코드 */


$ch = curl_init();
$url = 'http://openapi.jeonju.go.kr/rest/wifizone/getWifiList'; /*URL*/
$queryParams = '?' . urlencode('serviceKey') . '=wM6oYEfLXgbc%2BGJAYa5xqPsqroqFkmkbxaydPsscccrdpqao4iuar8GR%2FiuP9DPYzy621k8X%2BncvU2OH5pfVDA%3D%3D'; /*Service Key*/
$queryParams .= '&' . urlencode('pageNo') . '=' . urlencode('1'); /**/
$queryParams .= '&' . urlencode('numOfRows') . '=' . urlencode('10'); /**/
$queryParams .= '&' . urlencode('instplaceNm') . '=' . urlencode('평화1동'); /**/
$queryParams .= '&' . urlencode('instfacType') . '=' . urlencode('관공서'); /**/
$queryParams .= '&' . urlencode('roadAdd') . '=' . urlencode('장승배기로'); /**/

curl_setopt($ch, CURLOPT_URL, $url . $queryParams);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
curl_setopt($ch, CURLOPT_HEADER, FALSE);
curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'GET');
$response = curl_exec($ch);
curl_close($ch);

var_dump($response);