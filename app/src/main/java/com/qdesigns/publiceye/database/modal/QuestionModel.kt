package com.qdesigns.publiceye.database.modal

data class QuestionModel(
    var questionId: String? = null,
    val question: String? = null,
    var option_a: String? = null,
    var option_b: String? = null,
    var option_c: String? = null,
    var answer: String? = null,
    var imageURL: String? = null,
    val timer: Long? = null
)