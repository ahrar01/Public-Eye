package com.qdesigns.publiceye.ui.quiz

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.qdesigns.publiceye.R
import com.qdesigns.publiceye.database.modal.QuestionModel
import kotlinx.android.synthetic.main.activity_quiz.*
import java.util.*

class QuizActivity : AppCompatActivity() {
    val TAG = "QUIZ_ACTIVITY_LOG"

    private var firebaseFirestore: FirebaseFirestore? = null
    private val firebaseAuth: FirebaseAuth? = null

    private lateinit var allQuestionsList: ArrayList<QuestionModel>
    private val questionsToAnswer: ArrayList<QuestionModel> =
        ArrayList<QuestionModel>()
    private val totalQuestionsToAnswer = 2
    private var countDownTimer: CountDownTimer? = null

    private var canAnswer = false
    private var currentQuestion = 0

    private var correctAnswers = 0
    private var wrongAnswers = 0
    private var notAnswered = 0

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)
        // Firestore
        firebaseFirestore = Firebase.firestore

        allQuestionsList = ArrayList()
        //Query Firestore Data
        firebaseFirestore!!.collection("QuizQuestions")
            .get()
            .addOnSuccessListener { result ->
                var availableQuizItemList: MutableList<QuestionModel> = mutableListOf()
                for (document in result) {
                    var quizItem = document.toObject(QuestionModel::class.java)
                    availableQuizItemList.add(quizItem)
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
                allQuestionsList = availableQuizItemList as ArrayList<QuestionModel>
                pickQuestions()
                loadUI()
            }
            .addOnFailureListener { exception ->
                quiz_title.text = "Error : " + exception!!.message

                Log.d(TAG, "Error getting documents: ", exception)
            }




        setupClickListener()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupClickListener() {
        quiz_option_one.setOnClickListener {
            verifyAnswer(quiz_option_one)
        }

        quiz_option_two.setOnClickListener {
            verifyAnswer(quiz_option_two)
        }

        quiz_option_three.setOnClickListener {
            verifyAnswer(quiz_option_three)
        }

        quiz_next_btn.setOnClickListener {
            if (currentQuestion == totalQuestionsToAnswer) {
                //Load Results
                submitResults()
            } else {
                currentQuestion++
                loadQuestion(currentQuestion)
                resetOptions()
            }
        }
    }

    private fun submitResults() {
        var sendToQuizResult = Intent(this, QuizResult::class.java)
        sendToQuizResult.putExtra("correct", correctAnswers)
        sendToQuizResult.putExtra("wrong", wrongAnswers)
        sendToQuizResult.putExtra("unanswered", notAnswered)
        sendToQuizResult.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(sendToQuizResult)


    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun resetOptions() {
        quiz_option_one.setBackground(
            resources.getDrawable(
                R.drawable.outline_light_btn_bg,
                null
            )
        )
        quiz_option_two.setBackground(
            resources.getDrawable(
                R.drawable.outline_light_btn_bg,
                null
            )
        )
        quiz_option_three.setBackground(
            resources.getDrawable(
                R.drawable.outline_light_btn_bg,
                null
            )
        )
        quiz_option_one.setTextColor(resources.getColor(R.color.colorLightText, null))
        quiz_option_two.setTextColor(resources.getColor(R.color.colorLightText, null))
        quiz_option_three.setTextColor(resources.getColor(R.color.colorLightText, null))
        quiz_question_feedback.setVisibility(View.INVISIBLE)
        quiz_next_btn.setVisibility(View.INVISIBLE)
        quiz_next_btn.setEnabled(false)
    }


    private fun loadUI() {
        //Quiz Data Loaded, Load the UI
        quiz_title.text = "quizName"
        quiz_question.text = "Load First Question"

        //Enable Options
        enableOptions()

        //Load First Question
        loadQuestion(1)
    }

    private fun loadQuestion(questNum: Int) {
        //Set Question Number
        quiz_question_number.setText(questNum.toString() + "")

        //Load Question Text
        Glide.with(this).load(questionsToAnswer[questNum - 1].imageURL)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontAnimate().into(quiz_image_item)

        quiz_question.setText(questionsToAnswer[questNum - 1].question)

        //Load Options
        quiz_option_one.setText(questionsToAnswer[questNum - 1].option_a)
        quiz_option_two.setText(questionsToAnswer[questNum - 1].option_b)
        quiz_option_three.setText(questionsToAnswer[questNum - 1].option_c)

        //Question Loaded, Set Can Answer
        canAnswer = true
        currentQuestion = questNum

        //Start Question Timer
        startTimer(questNum)
    }

    private fun startTimer(questionNumber: Int) {

        //Set Timer Text
        val timeToAnswer: Long? = questionsToAnswer[questionNumber - 1].timer
        quiz_question_time.setText(timeToAnswer.toString())

        //Show Timer ProgressBar
        quiz_question_progress.setVisibility(View.VISIBLE)

        //Start CountDown
        countDownTimer = object : CountDownTimer(timeToAnswer!! * 1000, 10) {
            override fun onTick(millisUntilFinished: Long) {
                //Update Time
                quiz_question_time.setText((millisUntilFinished / 1000).toString() + "")

                //Progress in percent
                val percent = millisUntilFinished / (timeToAnswer!! * 10)
                quiz_question_progress.setProgress(percent.toInt())
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onFinish() {
                //Time Up, Cannot Answer Question Anymore
                canAnswer = false
                quiz_question_feedback.setText("Time Up! No answer was submitted.")
                quiz_question_feedback.setTextColor(resources.getColor(R.color.colorPrimary, null))
                notAnswered++
                showNextBtn()
            }
        }
        (countDownTimer as CountDownTimer).start()
    }


    private fun pickQuestions() {
        for (i in 0 until totalQuestionsToAnswer) {
            val randomNumber: Int = getRandomInt(0, allQuestionsList.size)
            questionsToAnswer.add(allQuestionsList[randomNumber])
            allQuestionsList.removeAt(randomNumber)
            Log.d(
                "QUESTIONS LOG",
                "Question " + i + " : " + questionsToAnswer[i.toInt()].question
            )
        }
    }

    private fun getRandomInt(min: Int, max: Int): Int {
        return (Math.random() * (max - min)).toInt() + min
    }

    private fun enableOptions() {
        //Show All Option Buttons
        quiz_option_one.visibility = View.VISIBLE
        quiz_option_two.visibility = View.VISIBLE
        quiz_option_three.visibility = View.VISIBLE

        //Enable Option Buttons
        quiz_option_one.isEnabled = true
        quiz_option_two.isEnabled = true
        quiz_option_three.isEnabled = true

        //Hide Feedback and next Button
        quiz_question_feedback.visibility = View.INVISIBLE
        quiz_next_btn.visibility = View.INVISIBLE
        quiz_next_btn.isEnabled = false
    }

    @SuppressLint("NewApi")
    private fun verifyAnswer(selectedAnswerBtn: Button) {
        //Check Answer
        if (canAnswer) {
            //Set Answer Btn Text Color to Black
            selectedAnswerBtn.setTextColor(resources.getColor(R.color.black, null))
            if (questionsToAnswer[currentQuestion - 1].answer
                    ?.equals(selectedAnswerBtn.text)!!
            ) {
                //Correct Answer
                correctAnswers++
                selectedAnswerBtn.background = resources.getDrawable(
                    R.drawable.correct_answer_btn_bg,
                    null
                )

                //Set Feedback Text
                quiz_question_feedback.setText("Correct Answer")
                quiz_question_feedback.setTextColor(resources.getColor(R.color.colorPrimary, null))
            } else {
                //Wrong Answer
                wrongAnswers++
                selectedAnswerBtn.background = resources.getDrawable(
                    R.drawable.wrong_answer_btn_bg,
                    null
                )

                //Set Feedback Text
                quiz_question_feedback.setText(
                    """Wrong Answer 
 
 Correct Answer : ${questionsToAnswer[currentQuestion - 1].answer}"""
                )
                quiz_question_feedback.setTextColor(resources.getColor(R.color.colorAccent, null))
            }
            //Set Can answer to false
            canAnswer = false

            //Stop The Timer
            countDownTimer!!.cancel()

            //Show Next Button
            showNextBtn()
        }
    }


    private fun showNextBtn() {
        if (currentQuestion == totalQuestionsToAnswer) {
            quiz_next_btn.setText("Submit Results")
        }
        quiz_question_feedback.setVisibility(View.VISIBLE)
        quiz_next_btn.setVisibility(View.VISIBLE)
        quiz_next_btn.setEnabled(true)
    }

}
