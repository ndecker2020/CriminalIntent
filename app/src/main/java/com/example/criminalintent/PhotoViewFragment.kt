package com.example.criminalintent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import java.io.File



private const val ARG_PHOTO = "photo"
class PhotoViewFragment:DialogFragment() {
    private lateinit var crimePhoto: ImageView
    private lateinit var photoFile: File

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_view,container,false)
        photoFile = arguments?.getSerializable(ARG_PHOTO) as File
        crimePhoto = view.findViewById(R.id.photo_view)
        if (!photoFile.exists()){
            crimePhoto.setImageDrawable(null)
        }
        else{
            val bitmap = getScaledBitmap(photoFile.path,1000,1000)
            crimePhoto.setImageBitmap(bitmap)
        }
        return view
    }


    companion object{
        fun newInstance(file: File):PhotoViewFragment {
            val args = Bundle().apply {  putSerializable(ARG_PHOTO, file) }
            return PhotoViewFragment().apply { arguments=args }
        }
    }


}