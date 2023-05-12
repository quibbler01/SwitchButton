package cn.quibbler.switchbutton

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.quibbler.switchbutton.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get switch button through ViewBinding or findViewById()
        val switchButton = binding.switchButton

        //set checked
        switchButton.toggle(true)

        //or set unchecked
        switchButton.toggle(false)
    }

}