package com.example.s501.data.json

import android.content.Context
import android.util.Log
import com.example.s501.data.model.Category
import com.example.s501.data.model.ImageCategory
import com.google.gson.Gson
import java.io.File

class JsonFileService(context: Context) {
    private val jsonFile: File = File(context.getExternalFilesDir("categories"), "categories.json")

    fun createFileIfNotExists() {
        if (!jsonFile.exists()) {
            val emptyJson = "[]"

            jsonFile.writeText(emptyJson)
            Log.d("JSON", "New JSON file created")
        } else {
            Log.d("JSON", "JSON file already exists")
        }
    }

    fun addCategoriesToJsonFile(imageId: Long, categories: List<Category>) {
        val imageCategoriesList = mutableListOf<ImageCategory>()

        if (jsonFile.exists()) {
            val existingJson = jsonFile.readText()
            val existingData = Gson().fromJson(existingJson, Array<ImageCategory>::class.java).toMutableList()

            imageCategoriesList.addAll(existingData)
        }

        val newCategory = ImageCategory(
            imageId = imageId,
            categories = categories
        )

        imageCategoriesList.add(newCategory)

        val updatedJson = Gson().toJson(imageCategoriesList)

        jsonFile.writeText(updatedJson)
        Log.d("JSON", "JSON file updated")
    }

    fun getCategoriesFromJsonFile(imageId: Long): List<Category> {
        if (jsonFile.exists()) {
            val jsonContent = jsonFile.readText()
            val imageCategoriesList = Gson().fromJson(jsonContent, Array<ImageCategory>::class.java).toList()
            val imageCategory = imageCategoriesList.find { it.imageId == imageId }

            return imageCategory?.categories ?: emptyList()
        }

        return emptyList()
    }
}
