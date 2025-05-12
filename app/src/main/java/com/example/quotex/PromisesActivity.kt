package com.example.quotex

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PromisesActivity : AppCompatActivity() {
    private lateinit var promiseAdapter: PromiseAdapter
    private val promises = mutableListOf<Promise>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_promises)

        // Setup RecyclerView
        val promisesList = findViewById<RecyclerView>(R.id.promises_list)
        promisesList.layoutManager = LinearLayoutManager(this)
        promiseAdapter = PromiseAdapter(promises) { promise ->
            showEditDeleteDialog(promise)
        }
        promisesList.adapter = promiseAdapter

        // Load saved promises
        loadPromises()

        // Setup FAB for adding new promises
        val fabAddNewPromise = findViewById<ExtendedFloatingActionButton>(R.id.fab_add_new_promise)
        fabAddNewPromise.setOnClickListener {
            showAddPromiseDialog()
        }
    }

    private fun loadPromises() {
        val prefs = getSharedPreferences("PromisesPrefs", Context.MODE_PRIVATE)
        val promisesJson = prefs.getString("promises", null)
        promises.clear()
        if (!promisesJson.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<Promise>>() {}.type
                val loadedPromises: List<Promise> = Gson().fromJson(promisesJson, type)
                promises.addAll(loadedPromises)
                promiseAdapter.updatePromises(promises)
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to load promises", Toast.LENGTH_SHORT).show()
            }
        }
        // Toggle empty state view
        findViewById<TextView>(R.id.empty_view).visibility =
            if (promises.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun savePromises() {
        val prefs = getSharedPreferences("PromisesPrefs", Context.MODE_PRIVATE)
        val promisesJson = Gson().toJson(promises)
        prefs.edit().putString("promises", promisesJson).apply()
        // Toggle empty state view
        findViewById<TextView>(R.id.empty_view).visibility =
            if (promises.isEmpty()) View.VISIBLE else View.GONE
    }
    
    private fun showAddPromiseDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_promise, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.promise_title_input)
        val verseInput = dialogView.findViewById<EditText>(R.id.promise_verse_input)
        val referenceInput = dialogView.findViewById<EditText>(R.id.promise_reference_input)

        MaterialAlertDialogBuilder(this)
            .setTitle("Add New Promise")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = titleInput.text.toString().trim()
                val verse = verseInput.text.toString().trim()
                val reference = referenceInput.text.toString().trim()

                if (title.isNotEmpty() && verse.isNotEmpty()) {
                    val newPromise = Promise(
                        title = title,
                        verse = verse,
                        reference = reference
                    )
                    promises.add(newPromise)
                    promiseAdapter.updatePromises(promises)
                    savePromises()
                    Toast.makeText(this, "Promise added successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Title and verse are required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDeleteDialog(promise: Promise) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Manage Promise")
            .setItems(arrayOf("Edit", "Delete")) { _, which ->
                when (which) {
                    0 -> showEditPromiseDialog(promise)
                    1 -> {
                        promises.remove(promise)
                        promiseAdapter.updatePromises(promises)
                        savePromises()
                        Toast.makeText(this, "Promise deleted", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditPromiseDialog(promise: Promise) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_promise, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.promise_title_input)
        val verseInput = dialogView.findViewById<EditText>(R.id.promise_verse_input)
        val referenceInput = dialogView.findViewById<EditText>(R.id.promise_reference_input)

        // Pre-fill fields with existing promise data
        titleInput.setText(promise.title)
        verseInput.setText(promise.verse)
        referenceInput.setText(promise.reference)

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Promise")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = titleInput.text.toString().trim()
                val verse = verseInput.text.toString().trim()
                val reference = referenceInput.text.toString().trim()

                if (title.isNotEmpty() && verse.isNotEmpty()) {
                    // Update the promise
                    val index = promises.indexOf(promise)
                    if (index != -1) {
                        promises[index] = Promise(
                            id = promise.id,
                            title = title,
                            verse = verse,
                            reference = reference
                        )
                        promiseAdapter.updatePromises(promises)
                        savePromises()
                        Toast.makeText(this, "Promise updated successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Title and verse are required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}