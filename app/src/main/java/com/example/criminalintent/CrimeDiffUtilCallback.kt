package com.example.criminalintent

import androidx.recyclerview.widget.DiffUtil

class CrimeDiffUtilCallback: DiffUtil.ItemCallback<Crime>() {
    override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
        return oldItem.id ==newItem.id
    }

    override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
        return (oldItem.title == newItem.title && oldItem.date == newItem.date &&oldItem.isSolved == newItem.isSolved)
    }
}