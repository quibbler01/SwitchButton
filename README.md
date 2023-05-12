# SwitchButton

![switch_preview](./demo.gif)

[![](https://jitpack.io/v/quibbler01/SwitchButton.svg)](https://jitpack.io/#quibbler01/SwitchButton)

Step 1. Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.quibbler01:SwitchButton:1.0.2'
	}

Step 3. Add SwitchButton in your layout xml:

    <cn.quibbler.switchbutton.SwitchButton
        android:id="@+id/switch_button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />

Step 4. Use it in your way.
        
        //find this SwitchButton
        val switchButton = binding.switchButton

        //set checked
        switchButton.toggle(true)

        //set unchecked
        switchButton.toggle(false)