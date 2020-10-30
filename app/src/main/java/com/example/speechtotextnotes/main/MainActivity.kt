package com.example.speechtotextnotes.main

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.example.speechtotextnotes.R
import com.example.speechtotextnotes.data.NoteList
import com.example.speechtotextnotes.databinding.ActivityMainBinding
import com.example.speechtotextnotes.util.DialogNotification
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.RealmList


class MainActivity : AppCompatActivity() {

    private lateinit var realm: Realm
    private lateinit var binding: ActivityMainBinding
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(MainViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        realm = Realm.getDefaultInstance()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this@MainActivity)
        notesAdapter = NotesAdapter(arrayListOf())
        viewModel.notifyWhenDbIsEmpty(realm)
        observeDataBase()
        viewModel.checkPermissions()
        observePermission()
        startOrEndListening()
        setSpeechRecognitionListener()
        hideFabOnScroll()
    }

    private fun observeDataBase() {
        viewModel.isEmpty.observe(this, Observer { emptyDataBase ->
            when (emptyDataBase) {
                true -> {
                    val dialog =
                        DialogNotification.createDialog(
                            this@MainActivity,
                            resources.getString(R.string.title_notification),
                            resources.getString(R.string.message_notification)
                        )
                }
                false -> {
                    binding.notesRecycler.apply {
                        layoutManager = LinearLayoutManager(this@MainActivity)
                        notesAdapter.populateList(realm.where(NoteList::class.java).findAll())
                        adapter = notesAdapter
                    }
                }
            }
        })
    }

    private fun observePermission() {
        viewModel.hasPermission.observe(this, Observer { permissionsGranted ->
            permissionsGranted?.let {
                if (permissionsGranted) {
                    Log.d("GRANTED", "observePermissions: Permissions have been granted.")
                } else {
                    ActivityCompat.requestPermissions(this, MainViewModel.permissions, 1)
                }
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissions.forEach {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, it)) {
                val dialog = AlertDialog.Builder(this)
                dialog.setIcon(R.drawable.record_audio_not_granted)
                dialog.setTitle(getString(R.string.dialog_title))
                dialog.setMessage(getString(R.string.dialog_message))
                dialog.setCancelable(false)
                dialog.setPositiveButton(getString(R.string.positive_button)
                ) { _, _ ->
                    ActivityCompat.requestPermissions(this, MainViewModel.permissions, 1)
                }
                dialog.setNegativeButton(resources.getString(R.string.negative_button)
                ) { _, _ ->
                    val logoutIntent = Intent()
                    logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(logoutIntent)
                }
                dialog.show()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun startOrEndListening() {
        binding.createNotesFab.setOnTouchListener { _, motion ->
            when (motion.action) {
                ACTION_DOWN -> {
                    binding.createNotesFab.backgroundTintList = ContextCompat.getColorStateList(
                        applicationContext,
                        R.color.color_fab_pressed_state
                    )
                    YoYo.with(Techniques.Bounce)
                        .duration(500)
                        .repeat(5)
                        .playOn(binding.createNotesFab)
                    speechRecognizer.startListening(createSpeechRecognitionIntent())
                    binding.blast.visibility = View.VISIBLE
                }
                ACTION_UP -> {
                    binding.createNotesFab.backgroundTintList = ContextCompat.getColorStateList(
                        applicationContext,
                        R.color.color_accent_fab
                    )
                    binding.blast.visibility = View.INVISIBLE
                }
            }
            return@setOnTouchListener true
        }
    }

    private fun createSpeechRecognitionIntent(): Intent {
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "sr-Latn-RS")
        return speechRecognizerIntent
    }


    private fun setSpeechRecognitionListener() {
        //speechRecognizer should be launched only from main thread
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
                Log.d("On Ready Method", "onReadyForSpeech:")
            }

            override fun onBeginningOfSpeech() {
                Log.d("On Beginning Of Speech", "onBeginningOfSpeech:")
                Snackbar.make(
                    binding.createNotesFab, getString(R.string.listening_notification),
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            }

            override fun onRmsChanged(floatResult: Float) {
                //listen for amplitude changes
                floatResult.let {
                    val floatByteArray = byteArrayOf(it.toBits().toByte())
                    binding.blast.setRawAudioBytes(floatByteArray)
                    binding.blast.display
                }
            }

            override fun onBufferReceived(p0: ByteArray?) {
                Log.d("On Buffer Received", "onBeginningOfSpeech:")
            }

            override fun onEndOfSpeech() {
                Log.d("On End Of Speech", "onBeginningOfSpeech: ")
                Snackbar.make(
                    binding.createNotesFab, getString(R.string.results_generated_notification),
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            }

            override fun onError(errorNotification: Int) {
                Log.d("On Error", "On Error handle case...${errorNotification}")
                errorNotification.let {
                    when (errorNotification) {
                        1 -> Snackbar.make(
                            binding.createNotesFab,
                            getString(R.string.network_timeout),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        2 -> Snackbar.make(
                            binding.createNotesFab,
                            getString(R.string.network_connection),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        3 -> Snackbar.make(
                            binding.createNotesFab,
                            getString(R.string.audio_error),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        4 -> Snackbar.make(
                            binding.createNotesFab,
                            getString(R.string.server_error),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        5 -> Snackbar.make(
                            binding.createNotesFab,
                            getString(R.string.user_audio_error),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        6 -> Snackbar.make(
                            binding.createNotesFab,
                            getString(R.string.speech_timeout),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        7 -> Snackbar.make(
                            binding.createNotesFab,
                            getString(R.string.match_timeout),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onResults(resultBundle: Bundle?) {
                resultBundle?.let {
                    val result =
                        resultBundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    //realm operations
                    try {
                        realm.beginTransaction()
                        result?.let { voiceList ->
                            val notesList = RealmList<String>()
                            notesList.addAll(voiceList)
                            val note = NoteList(NoteList.cachedNextId, notesList)
                            realm.insertOrUpdate(note)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        realm.commitTransaction()
                    }

                    binding.notesRecycler.apply {
                        realm.beginTransaction()
                        val updatedListFromRealm = realm.where(NoteList::class.java).findAll()
                        updatedListFromRealm?.let {
                            val realmArrayList = arrayListOf<NoteList>()
                            realmArrayList.addAll(updatedListFromRealm)
                            realm.commitTransaction()
                            layoutManager = LinearLayoutManager(this@MainActivity)
                            //update list in adapter
                            notesAdapter.populateList(realmArrayList)
                            adapter = notesAdapter
                        }
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                Log.d("On Partial Results", "Partial results Error")
            }

            override fun onEvent(p0: Int, p1: Bundle?) {
                Log.d("On Event", "Event handling... ")
            }
        })
    }

    private fun hideFabOnScroll() {
        binding.notesRecycler.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    binding.createNotesFab.hide()
                } else {
                    binding.createNotesFab.show()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.beginTransaction()
        realm.deleteAll()
        realm.commitTransaction()
    }
}
