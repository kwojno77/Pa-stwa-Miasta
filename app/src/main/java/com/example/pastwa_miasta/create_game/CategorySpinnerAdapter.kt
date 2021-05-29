package com.example.pastwa_miasta.create_game

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.R


class CategorySpinnerAdapter (private val data: MutableList<SpinnerModel>, private val context: Context, val listener: (Int) -> Unit)
    : RecyclerView.Adapter<CategorySpinnerAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var spinner: Spinner = view.findViewById(R.id.categorySpinner)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_list_of_spinners, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        createSpinner(viewHolder.spinner, R.array.categories)
    }

    override fun getItemCount() : Int = data.size

    // Creates a Spinner by getting Spinner object and a resource array
    private fun createSpinner(spinner: Spinner, array: Int) {
        ArrayAdapter.createFromResource(
            context,
            array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }
}