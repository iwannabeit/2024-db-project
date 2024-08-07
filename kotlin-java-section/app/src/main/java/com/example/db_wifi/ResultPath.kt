package com.example.db_wifi

data class ResultPath(
    val route : Result_trackoption,
    val message : String,
    val code : Int
)
data class Result_trackoption(
    val traoptimal : List<Result_path>
)
data class Result_path(
    val summary : Result_distance,
    val path : List<List<Double>>
)
data class Result_distance(
    val distance : Int
)

data class Summary(
    val start: Point,
    val goal: Point
)

data class Point(
    val location: List<Double>
)