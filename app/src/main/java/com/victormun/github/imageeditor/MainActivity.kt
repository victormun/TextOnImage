package com.victormun.github.imageeditor

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.victormun.github.imageeditor.databinding.ActivityMainBinding
import com.victormun.github.imageeditor.utils.Constants
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var openButton: Button
    private lateinit var imageView: ImageView
    private lateinit var addTextButton: FloatingActionButton
    private var pressedX: Int = 0
    private var pressedY: Int = 0

    private var isClicked = false

    /**
     * Class OnCreate method
     *
     * @param savedInstanceState state of the rotation
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews(binding)
        initListeners()
    }

    /**
     * Method that opens Android's File Opener
     */
    private fun openFile() {
        isClicked = true
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = Constants.MAIN_IMAGE_INTENT_TYPE
        startActivityForResult(intent, Constants.MAIN_REQUEST_OPEN)
    }

    /**
     * Method that handles the [Activity] return data
     *
     * @param requestCode is the code sent through the [Intent]
     * @param resultCode  is the result given back from the opened [Activity]
     * @param resultData  is the [Intent] sent back from the previous [Activity]
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                  resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == Constants.MAIN_REQUEST_OPEN && resultCode == RESULT_OK) {
            if (resultData != null) {
                val uri = resultData.data
                showImage(uri)
            }
        }
    }

    /**
     * Method that paints the chosen image inside the ImageView
     *
     * @param uri of the selected [android.graphics.drawable.Drawable]
     */
    private fun showImage(uri: Uri?) {
        Thread {
            val bitmap: Bitmap
            try {
                bitmap = getBitmapFromUri(uri)
                imageView.post {
                    imageView.setImageBitmap(bitmap)
                    openButton.visibility = View.GONE
                    addTextButton.show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Error loading image: $e")
            } catch (e: NullPointerException) {
                e.printStackTrace()
                Log.e(TAG, "Error loading image: $e")
            }
        }.start()
    }

    /**
     * Method that adds a [android.widget.TextView] to the layout
     */
    private fun addTextView() {
        val textView = TextView(this)
        textView.id = View.generateViewId()
        textView.text = "Introduce your text here"
        textView.textSize = 20f
        textView.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        val baseLayout = findViewById<ConstraintLayout>(R.id.main_base_layout)
        baseLayout.addView(textView)
        val set = ConstraintSet()
        set.clone(baseLayout)
        set.connect(textView.id, ConstraintSet.TOP, baseLayout.id, ConstraintSet.TOP, 0)
        set.connect(textView.id, ConstraintSet.BOTTOM, baseLayout.id, ConstraintSet.BOTTOM, 0)
        set.connect(textView.id, ConstraintSet.START, baseLayout.id, ConstraintSet.START, 0)
        set.connect(textView.id, ConstraintSet.END, baseLayout.id, ConstraintSet.END, 0)
        set.applyTo(baseLayout)



        textView.setOnTouchListener { view, event ->
            val constraintLayoutParams = view.layoutParams as ConstraintLayout.LayoutParams
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d("TAG", "@@@@ TV1 ACTION_UP")
                    // Where the user started the drag
                    pressedX = event.rawX.toInt()
                    pressedY = event.rawY.toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    Log.d("TAG", "@@@@ TV1 ACTION_UP")
                    // Where the user's finger is during the drag
                    val x = event.rawX.toInt()
                    val y = event.rawY.toInt()

                    // Calculate change in x and change in y
                    val dx: Int = x - pressedX
                    val dy: Int = y - pressedY

                    // Update the margins
                    constraintLayoutParams.leftMargin += dx
                    constraintLayoutParams.topMargin += dy
                    view.layoutParams = constraintLayoutParams

                    // Save where the user's finger was for the next ACTION_MOVE
                    pressedX = x
                    pressedY = y
                }
                MotionEvent.ACTION_UP -> Log.d("TAG", "@@@@ TV1 ACTION_UP")
            }
            view.performClick()
            true
        }
    }

    /**
     * Method that initializes all the listeners
     */
    private fun initListeners() {
        openButton.setOnClickListener { if (!isClicked) openFile() }
        addTextButton.setOnClickListener { addTextView() }
    }

    /**
     * Method that initialized all the views
     */
    private fun initViews(binding: ActivityMainBinding) {
        openButton = binding.mainButtonOpen
        imageView = binding.mainImageView
        addTextButton = binding.mainButtonText
    }

    @Throws(IOException::class, NullPointerException::class)
    private fun getBitmapFromUri(uri: Uri?): Bitmap {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri!!, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    companion object {
        // Class fields
        private val TAG = MainActivity::class.java.simpleName
    }
}