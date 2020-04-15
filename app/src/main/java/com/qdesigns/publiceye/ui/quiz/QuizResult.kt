package com.qdesigns.publiceye.ui.quiz

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.qdesigns.publiceye.R
import com.qdesigns.publiceye.ui.home.MainActivity
import kotlinx.android.synthetic.main.activity_quiz_result.*

class QuizResult : AppCompatActivity() {
    private var correctAnswers = 0
    private var wrongAnswers = 0
    private var notAnswered = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_result)

        correctAnswers = intent.extras?.get("correct") as Int
        wrongAnswers = intent.extras?.get("wrong") as Int
        notAnswered = intent.extras?.get("unanswered") as Int

        results_correct_text.text = correctAnswers.toString()
        results_wrong_text.text = wrongAnswers.toString()
        results_missed_text.text = notAnswered.toString()

        //Calculate Progress

        //Calculate Progress
        val total: Int = correctAnswers + wrongAnswers + notAnswered
        val percent: Long = (correctAnswers * 100 / total).toLong()

        results_percent.text = "$percent%"
        results_progress.progress = percent.toInt()

        if (percent >= 70) {
            results_home_btn.text = " Open Camera"

        } else {
            results_home_btn.text = "Cannot Open Camera"
        }

        results_home_btn.setOnClickListener {
            if (percent >= 70) {
                var sendToQuizResult = Intent(this, MainActivity::class.java)
                sendToQuizResult.putExtra("openCamera", true)
                sendToQuizResult.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(sendToQuizResult)
            } else {
                var sendToQuizResult = Intent(this, MainActivity::class.java)
                sendToQuizResult.putExtra("openCamera", false)
                sendToQuizResult.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(sendToQuizResult)
            }

        }
    }
}
