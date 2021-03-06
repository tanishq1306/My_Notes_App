package com.example.mynotesapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.mynotesapp.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        replaceFragment(HomeFragment.newInstance(), false)
    }

    private fun replaceFragment(fragment: Fragment, isTransition:Boolean) {
        val fragmentTransition = supportFragmentManager.beginTransaction()

        if (isTransition) {
            fragmentTransition.setCustomAnimations(android.R.anim.slide_out_right,android.R.anim.slide_in_left)
        }

        fragmentTransition.add(R.id.frame_layout, fragment).addToBackStack(fragment.javaClass.simpleName).commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val fragments = supportFragmentManager.fragments
        if (fragments.size == 0){
            finish()
        }
    }
}