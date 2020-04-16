package com.example.criminalintent

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*
import java.util.Date
@Entity
data class Crime (@PrimaryKey val id: UUID = UUID.randomUUID(),
                  var title: String = "",
                  var date: Date = Date(),
                  var isSolved: Boolean= false,
                  var suspect: String = "",
                  var phoneNumber: String =""
                  ){
    val photoFileName get() = "IMG_$id.jpg"
}



