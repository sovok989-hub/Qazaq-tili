package com.example.kazakhtrainer

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import kotlin.math.min
import kotlin.random.Random

private data class Word(val ru: String, val kk: String)

class MainActivity : Activity() {
    private val words = listOf(
        Word("картошка", "картоп"), Word("ребёнок", "бала"), Word("вишня", "шие"), Word("молоток", "балға"),
        Word("дом", "үй"), Word("вода", "су"), Word("хлеб", "нан"), Word("собака", "ит"), Word("кошка", "мысық"),
        Word("молоко", "сүт"), Word("человек", "адам"), Word("работать", "жұмыс істеу"), Word("идти", "бару"), Word("приходить", "келу"),
        Word("большой", "үлкен"), Word("маленький", "кішкентай"), Word("хороший", "жақсы"), Word("сегодня", "бүгін"), Word("завтра", "ертең"), Word("где", "қайда")
    )
    private val prefs by lazy { getSharedPreferences("progress", MODE_PRIVATE) }
    private var position = prefs.getInt("pos", 0)
    private var completed = prefs.getInt("completed", 0)
    private var errors = prefs.getInt("errors", 0)
    private var directionReverse = false
    private var current: Word = words[0]
    private lateinit var prompt: TextView
    private lateinit var buttons: List<Button>
    private lateinit var stats: TextView

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); showHome() }

    private fun base(): LinearLayout = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(28,36,28,28); setBackgroundColor(Color.BLACK) }
    private fun tv(text:String, size:Float)=TextView(this).apply { this.text=text; setTextColor(Color.WHITE); textSize=size; gravity=Gravity.CENTER; setPadding(8,16,8,16) }
    private fun btn(text:String)=Button(this).apply { this.text=text; setTextColor(Color.WHITE); setTextSize(18f); setBackgroundColor(Color.rgb(36,36,36)); isAllCaps=false }

    private fun showHome() {
        val root=base(); root.addView(tv("ҚАЗАҚША",32f), LinearLayout.LayoutParams(-1,0,1f))
        val continueBtn=btn(if(completed>0) "Продолжить уровень 1" else "Начать обучение")
        continueBtn.setOnClickListener { showQuiz(false) }; root.addView(continueBtn, LinearLayout.LayoutParams(-1,70))
        val review=btn("🔄 Повторение"); review.setOnClickListener { Toast.makeText(this,"Режим повторения будет добавлен в следующем прототипе",Toast.LENGTH_SHORT).show() }; root.addView(review, LinearLayout.LayoutParams(-1,70))
        val st=btn("Статистика"); st.setOnClickListener { showStats() }; root.addView(st, LinearLayout.LayoutParams(-1,70))
        setContentView(root)
    }

    private fun showQuiz(reverse:Boolean) {
        directionReverse=reverse; if(position>=400){ showLevelDone(); return }
        val root=base(); prompt=tv("",30f); root.addView(prompt, LinearLayout.LayoutParams(-1,0,1f))
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        val list=MutableList(4){0}; val correct=Random.nextInt(4); list[correct]=position%20
        val others=(0 until 20).filter{it!=position%20}.shuffled().take(3); var oi=0
        for(i in 0..3) if(i!=correct) { list[i]=others[oi]; oi++ }
        val bs=mutableListOf<Button>()
        for(i in 0..3){ val b=btn(""); bs.add(b); box.addView(b,LinearLayout.LayoutParams(-1,60).apply{setMargins(0,4,0,4)}); b.setOnClickListener{answer(b, list[i]==position%20, bs)} }
        buttons=bs; root.addView(box,LinearLayout.LayoutParams(-1,0,1.4f)); setContentView(root); renderQuestion(list)
    }

    private fun renderQuestion(ids:List<Int>){ current=words[position%20]; prompt.text=if(directionReverse) current.kk else current.ru; for(i in 0..3) buttons[i].text=if(directionReverse) words[ids[i]].ru else words[ids[i]].kk }

    private fun answer(b:Button, correct:Boolean, all:List<Button>){ if(correct){ b.setBackgroundColor(Color.rgb(46,125,50)); completed++; position++; prefs.edit().putInt("pos",position).putInt("completed",completed).putInt("errors",errors).apply(); b.postDelayed({showQuiz(directionReverse)},450) } else { b.setBackgroundColor(Color.rgb(198,40,40)); b.isEnabled=false; errors++; prefs.edit().putInt("errors",errors).apply() } }

    private fun showLevelDone(){ val root=base(); root.addView(tv("Уровень 1 завершён!",28f),LinearLayout.LayoutParams(-1,0,1f)); root.addView(tv("400 / 400",24f)); val next=btn("Открыть уровень 1.1"); next.setOnClickListener{ position=0; showQuiz(true) }; root.addView(next); val home=btn("Главное меню"); home.setOnClickListener{showHome()}; root.addView(home); setContentView(root) }

    private fun showStats(){ val root=base(); root.addView(tv("СТАТИСТИКА",30f)); root.addView(tv("Уровень 1: $position / 400\nЗавершённых вопросов: $completed\nОшибок: $errors\nИзучаемых слов: 20",20f),LinearLayout.LayoutParams(-1,0,1f)); val b=btn("Назад"); b.setOnClickListener{showHome()}; root.addView(b); setContentView(root) }
}
