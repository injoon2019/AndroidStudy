package com.example.todolist

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_edit.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.yesButton
import java.util.*

class EditActivity : AppCompatActivity() {

    val realm = Realm.getDefaultInstance() // 인스턴스 얻기

    val calendar: Calendar = Calendar.getInstance() //날짜를 다룰 캘린더 객체

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        val id = intent.getLongExtra("id", -1L)
        if (id == -1L) {
            insetMode()
        } else {
            updateMode(id)
        }

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
    }

    private fun insetMode() {
        deleteFab.visibility = View.GONE
        doneFab.setOnClickListener {
            insertTodo()
        }
    }

    private fun updateMode(id: Long) {
        val todo = realm.where<Todo>().equalTo("id", id).findFirst()!!
        todoEditText.setText(todo.title)
        calendarView.date = todo.date

        doneFab.setOnClickListener {
            updateTodo(id)
        }

        deleteFab.setOnClickListener {
            deleteTodo(id)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close() // 인스턴스 해제
    }

    private fun insertTodo() {
        realm.beginTransaction() // Transaction Start

        val newItem = realm.createObject<Todo>(nextId())// 새 객체 생성

        // 값 설정
        newItem.title = todoEditText.text.toString()
        newItem.date = calendar.timeInMillis

        realm.commitTransaction() // 트랜잭션 종료 반영

        // 다이얼로그 표시
        alert("내용이 추가되었습니다.") {
            yesButton { finish() }
        }.show()
    }

    //다음 id를 반환
    private fun nextId(): Int {
        val maxId = realm.where<Todo>().max("id")
        if (maxId != null) {
            return maxId.toInt() + 1
        }
        return 0
    }

    private fun updateTodo(id: Long) {
        realm.beginTransaction()    // 트랜잭션 시작

        val updateItem = realm.where<Todo>().equalTo("id", id).findFirst()!!
        // 값 수정
        updateItem.title = todoEditText.text.toString()
        updateItem.date = calendar.timeInMillis

        realm.commitTransaction()   //트랜잭션 종료반영

        // 다이얼로그 표시
        alert("내용이 변경되었습니다.") {
            yesButton { finish() }
        }.show()

    }

    private fun deleteTodo(id: Long) {
        realm.beginTransaction()    // 트랜잭션 시작

        val deleteItem = realm.where<Todo>().equalTo("id", id).findFirst()!! // 삭제할 객체

        deleteItem.deleteFromRealm() //삭제
        realm.commitTransaction()   //트랜잭션 종료반영

        alert("내용이 삭제되었습니다.") {
            yesButton { finish() }
        }.show()


    }
}
