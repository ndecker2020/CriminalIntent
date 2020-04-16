package com.example.criminalintent

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.ContactsContract
import android.provider.ContactsContract.*
import android.provider.ContactsContract.Contacts.*
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer
import java.io.File
import java.sql.Time
import java.util.*
import java.util.jar.Manifest
import kotlin.math.round
import kotlin.time.hours
import kotlin.time.minutes

private const val ARG_CRIME_ID = "crime_id"
private const val TAG ="CrimeFragment"
private const val DIALOG_DATE ="DialogDate"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 0
private const val REQUEST_CONTACT = 1
private const val PERMISSION_READ_CONTACTS = 2
private const val REQUEST_PHOTO = 3
private const val DIALOG_TIME= "DialogTime"
private const val DATE_FORMAT= "EEE, MMM, dd"
class CrimeFragment: Fragment(),DatePickerFragment.Callbacks,TimePickerFragment.Callbacks {
    private var hasPhone = false
    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var timeButton: Button
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var callSuspectButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView


    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID =arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view= inflater.inflate(R.layout.fragment_crime,container,false)
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        timeButton = view.findViewById(R.id.time_button) as Button
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        callSuspectButton = view.findViewById(R.id.call_suspect) as Button
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView



        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer {crime->
                crime?.let {
                    this.crime = crime
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(requireActivity(),
                        "com.example.criminalintent.fileprovider",
                        photoFile)
                    updateUI()
                }
            }
        )
        if(ContextCompat.checkSelfPermission(activity!!.applicationContext,android.Manifest.permission.READ_CONTACTS )!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity!!, arrayOf(android.Manifest.permission.READ_CONTACTS),PERMISSION_READ_CONTACTS)
        }
        else{
            hasPhone = true
            callSuspectButton.isEnabled = true
        }
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
            }
        }


        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }
        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }
        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(Time(crime.date.time)).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_TIME)
                show(
                    this@CrimeFragment.requireFragmentManager(),
                    DIALOG_TIME
                )
            }
        }
        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_suspect))
            }.also { intent -> startActivity(intent) }
        }
        suspectButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, CONTENT_URI)
            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }
            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null)
                isEnabled = false

        }
        callSuspectButton.setOnClickListener {
            val callSuspectIntent=Intent(Intent.ACTION_DIAL,Uri.parse("tel:${crime.phoneNumber}"))
            startActivity(callSuspectIntent)
        }
        callSuspectButton.isEnabled = hasPhone

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolveActivity: ResolveInfo? =
                packageManager.resolveActivity(captureImage,PackageManager.MATCH_DEFAULT_ONLY)
            if (resolveActivity==null){
                isEnabled = false
            }

            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT,photoUri)
                val cameraActivities: List<ResolveInfo> =
                packageManager.queryIntentActivities(captureImage,PackageManager.MATCH_DEFAULT_ONLY)

                for (cameraActivity in cameraActivities){
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                startActivityForResult(captureImage,REQUEST_PHOTO)
            }
        }
    }




    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }
    override fun onDateSelected(date: Date){
        crime.date=date
        updateUI()
    }

    override fun onTimeSelected(time: Time) {
        crime.date.hours=time.hours-18
         crime.date.minutes=time.minutes
        updateUI()
    }

    private fun updateUI(){
        titleField.setText(crime.title)
        dateButton.text = DateFormat.format("EEEE, MM-dd-yyyy",crime.date)
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        val hours = crime.date.hours
        val minutes = crime.date.minutes
        if (hours==0&&minutes==0){
            timeButton.text="0:00"
        }
        else if (minutes==0){
            timeButton.text="$hours:00"
        }
        else{
            if (minutes<10){
                timeButton.text ="$hours:0$minutes"
            }
            else{
                timeButton.text = "$hours:$minutes"}
            }
        if (crime.suspect.isNotEmpty()){
            suspectButton.text = crime.suspect
        }
        callSuspectButton.isEnabled = crime.phoneNumber.isNotEmpty()

        updatePhotoView()
    }

    private fun updatePhotoView(){
        if (photoFile.exists()){
            val bitmap = getScaledBitmap(photoFile.path,requireActivity())
            photoView.setImageBitmap(bitmap)
        }
        else{
            photoView.setImageDrawable(null)
        }
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var number = ""
        when{
            resultCode == REQUEST_PHOTO->{
                requireActivity().revokeUriPermission(photoUri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                updatePhotoView()
            }
            resultCode != Activity.RESULT_OK -> return
            requestCode == REQUEST_CONTACT && data != null ->{

                val contactUri: Uri? =data.data
                //specify which fields you want your query to return values for
                val queryFields = arrayOf(DISPLAY_NAME, ContactsContract.Contacts._ID,HAS_PHONE_NUMBER)
                //Perform your query - the contact Uri is like a "where" clause here
                val contentResolver = requireActivity().contentResolver
                val cursor = contentResolver.query(contactUri!!,queryFields,null,null,null)
                cursor?.use {
                    if (it.count == 0){
                        return
                    }

                it.moveToFirst()
                val suspect = it.getString(cursor.getColumnIndex(DISPLAY_NAME))
                val id = it.getString(cursor.getColumnIndex(BaseColumns._ID))
                val validNumber = it.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER))
                if (validNumber.toInt()>=1&&hasPhone){

                    val numbers = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        CommonDataKinds.Phone.CONTACT_ID + "="+ id,
                        null,null )
                    numbers?.use {
                        it.moveToFirst()
                        number = it.getString(it.getColumnIndex(CommonDataKinds.Phone.NUMBER))

                    }




                }
                cursor.close()
                crime.phoneNumber = number
                crime.suspect = suspect
                crimeDetailViewModel.saveCrime(crime)
                suspectButton.text = suspect
                }
            }


        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            PERMISSION_READ_CONTACTS -> {
                if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    hasPhone = true
                return
            }

        }
    }

    private fun getCrimeReport():String{
        val solvedString= if(crime.isSolved){
            getString(R.string.crime_report_solved)}
            else{
                getString(R.string.crime_report_unsolved)
            }
        val dateString = DateFormat.format(DATE_FORMAT,crime.date)
        var suspect= if (crime.suspect.isBlank()){
            getString(R.string.crime_report_no_suspect)
        }
        else{
            getString(R.string.crime_report_suspect, crime.suspect)
        }
        return getString(R.string.crime_report,crime.title,dateString,solvedString,suspect)
    }

     companion object{
         fun newInstance(crimeId:UUID): CrimeFragment {
             val args = Bundle().apply { putSerializable(ARG_CRIME_ID, crimeId) }
             return CrimeFragment().apply { arguments=args }
         }
     }

}