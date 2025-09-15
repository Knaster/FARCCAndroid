package com.example.FARCCAndroid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

//import android.view.ViewGroup.LayoutParams

class HarmonicFragment : androidx.fragment.app.Fragment()/*, View.OnClickListener*/ {
    //var listener : View.OnClickListener? = null
    var harmonicNumber : Int = -1
    var harmonicMidRatio : Int = -1
    var harmonicRatio : Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View? = inflater.inflate(R.layout.harmonic_fragment, container, false)

        view?.setOnClickListener {
            val assface = 5
        }

        val seekbar = view?.findViewById<SeekBar>(R.id.seekbarHarmonicFragment)

        seekbar?.updatePadding(harmonicNumber * 50)
        seekbar?.setOnClickListener {
            val fuckme = 7
        }

        //view?.updatePadding(harmonicNumber * 100)

        return view
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
    }

}

class HarmonicEditor : AppCompatActivity() {
    var harmonicSliderCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_harmonic_editor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.harmonicEditMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        hideSystemUI(this.window)

        // Associate the fragmentManager with the fragmentviewcontainer
/*        val fm = supportFragmentManager
        val harmFrag  = HarmonicFragment()
        val transaction = fm.beginTransaction()
        transaction.add(R.id.harmonicFragmentContainer, harmFrag)
        transaction.commit()
*/
        findViewById<ConstraintLayout>(R.id.harmonicEditMain).setOnClickListener {
            val thisView = findViewById<ConstraintLayout>(R.id.harmonicEditMain)
            var ass = 5
        }

        addMyFragment(0, "x", null)
        addMyFragment(1, "x", null)
        //addMyFragment(2, "x", null)
       // addMyFragment(1, "y", 0)
    }

    private fun addMyFragment(index: Int, data: String, previousViewId: Int?) {
        val containerId = View.generateViewId()
/*
        val containerView = FrameLayout(this).apply {
            //FragmentContainerView(this).apply {
            id = containerId
            tag = "harmonicSlider" + harmonicSliderCount.toString()
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            //localClassName = "com.example.FARCCAndroid.HarmonicFragment"
        }
*/
        val containerHolder = findViewById<FrameLayout>(R.id.dynamicHarmonicContents)
//        containerHolder.addView(containerView)

        val fragment = HarmonicFragment()
        fragment.harmonicNumber = harmonicSliderCount

//        containerHolder.addView(fragment.view)
/*
        val layoutParams = containerView.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.topToTop = if (previousViewId == null) {
            ConstraintLayout.LayoutParams.PARENT_ID
        } else {
            ConstraintLayout.LayoutParams.UNSET
        }

        if (previousViewId != null) {
            layoutParams.topToBottom = previousViewId
        }

        containerView.layoutParams = layoutParams
*/

        supportFragmentManager.beginTransaction()
            .add(R.id.dynamicHarmonicContents, fragment)
            //.addToBackStack("harmonicSlider" + harmonicSliderCount.toString())
            .show(fragment)
            .commit()
        val layoutParams = fragment.view?.updatePadding(harmonicSliderCount * 108)
        //fragment.view?.id = View.generateViewId()
        harmonicSliderCount++
    }
/*
    //@RequiresApi(Build.VERSION_CODES.R)
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(
            window,
            window.decorView.findViewById(android.R.id.content)
        ).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())

            // When the screen is swiped up at the bottom
            // of the application, the navigationBar shall
            // appear for some time
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }*/
}
