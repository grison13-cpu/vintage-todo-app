package com.vintage.todo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var adapter: TaskAdapter
    private var taskList = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        db = AppDatabase.getInstance(this)
        
        val etInput = findViewById<EditText>(id.etTaskInput)
        val btnToggle = findViewById<ToggleButton>(id.btnToggleForward)
        val btnAdd = findViewById<Button>(id.btnAddTask)
        val rv = findViewById<RecyclerView>(id.rvTasks)

        adapter = TaskAdapter(taskList) { updatedTask ->
            db.taskDao().update(updatedTask)
            loadTasks()
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        loadTasks()

        btnAdd.setOnClickListener {
            val title = etInput.text.toString().trim()
            if (title.isNotEmpty()) {
                val today = getTodayMidnight()
                val newTask = Task(title = title, dueDate = today, isAutoForward = btnToggle.isChecked)
                db.taskDao().insert(newTask)
                etInput.text.clear()
                loadTasks()
            }
        }
    }

    private fun loadTasks() {
        taskList.clear()
        taskList.addAll(db.taskDao().getTasksForToday(getTodayMidnight()))
        adapter.notifyDataSetChanged()
    }

    private fun getTodayMidnight(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}

class TaskAdapter(private val tasks: List<Task>, val onCheckChanged: (Task) -> Unit) : 
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val cb = v.findViewById<CheckBox>(id.cbCompleted)
        val tv = v.findViewById<TextView>(id.tvTaskTitle)
        val indicator = v.findViewById<TextView>(id.tvForwardIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(layout.item_task, parent, false)
        return TaskViewHolder(v)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.tv.text = task.title
        holder.cb.setOnCheckedChangeListener(null)
        holder.cb.isChecked = task.isCompleted
        holder.indicator.visibility = if (task.isAutoForward) View.VISIBLE else View.GONE

        holder.cb.setOnCheckedChangeListener { _, isChecked ->
            task.isCompleted = isChecked
            onCheckChanged(task)
        }
    }

    override fun getItemCount() = tasks.size
}
