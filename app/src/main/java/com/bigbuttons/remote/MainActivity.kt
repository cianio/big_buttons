package com.bigbuttons.remote

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.*
import android.view.Gravity
import android.view.WindowManager
import android.widget.*
import java.util.ArrayDeque

class MainActivity:Activity(){
    private val handler=Handler(Looper.getMainLooper()); private val pageBackStack=ArrayDeque<String>(); private var firstResume=true; private var renderedModeIndex=-1; private var currentPageId=AppPrefs.MAIN_PAGE_ID
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);AppPrefs.ensureMigrated(this)}
    override fun onResume(){super.onResume();val idx=AppPrefs.activeModeIndex(this);val changed=idx!=renderedModeIndex;if(changed){currentPageId=AppPrefs.MAIN_PAGE_ID;pageBackStack.clear()};render(idx);if(firstResume||changed){firstResume=false;renderedModeIndex=idx;runModeStartup(idx,AppPrefs.mode(this,idx))}else renderedModeIndex=idx}
    private fun render(modeIndex:Int){
        val global=AppPrefs.global(this); val mode=AppPrefs.mode(this,modeIndex); val page=mode.pages.firstOrNull{it.id==currentPageId}?:mode.pages.first{it.id==AppPrefs.MAIN_PAGE_ID}; currentPageId=page.id
        if(global.keepScreenAwake)window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(AppColors.Background)};applySystemBarPadding(root,12,10)
        val top=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        if(currentPageId!=AppPrefs.MAIN_PAGE_ID){ val back=ImageButton(this).apply{setImageResource(R.drawable.ic_back);imageTintList=ColorStateList.valueOf(AppColors.Text);background=rippleBackground(AppColors.Surface,14);contentDescription="Back";setOnClickListener{navigateBack(modeIndex)}};top.addView(back,LinearLayout.LayoutParams(dp(48),dp(48)).apply{marginEnd=dp(8)}) }
        val modeButton=Button(this).apply{text=if(currentPageId==AppPrefs.MAIN_PAGE_ID)mode.name else "${mode.name}  /  ${page.name}";textSize=20f;setTextColor(AppColors.Text);setTypeface(typeface,Typeface.BOLD);gravity=Gravity.CENTER_VERTICAL;isAllCaps=false;minHeight=0;minWidth=0;background=rippleBackground(AppColors.Background,14);setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_expand_more,0);compoundDrawablePadding=dp(6);setOnClickListener{startActivity(Intent(this@MainActivity,ModePickerActivity::class.java))}}
        top.addView(modeButton,LinearLayout.LayoutParams(0,dp(52),1f))
        val settings=ImageButton(this).apply{setImageResource(R.drawable.ic_settings);imageTintList=ColorStateList.valueOf(AppColors.Text);background=rippleBackground(AppColors.Surface,14,AppColors.Border);contentDescription="Settings";setOnClickListener{startActivity(Intent(this@MainActivity,SettingsActivity::class.java))}}
        top.addView(settings,LinearLayout.LayoutParams(dp(48),dp(48)).apply{marginStart=dp(8)});root.addView(top)
        val landscape=resources.configuration.orientation==Configuration.ORIENTATION_LANDSCAPE; val columns=if(landscape)3 else 2; val rows=if(landscape)2 else 3
        val grid=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};root.addView(grid,LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1f).apply{topMargin=dp(4)})
        repeat(rows){r->val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};grid.addView(row,LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1f));repeat(columns){c->val i=r*columns+c;val config=page.buttons[i];val actionable=when(config.actionType){ButtonActionType.COMMAND->config.value.isNotBlank();ButtonActionType.PAGE->mode.pages.any{it.id==config.value};ButtonActionType.BACK->true};val b=Button(this).apply{text=config.label;textSize=if(landscape)18f else 20f;setTextColor(if(actionable)AppColors.Text else AppColors.Muted);setTypeface(typeface,Typeface.BOLD);gravity=Gravity.CENTER;isAllCaps=false;minHeight=0;minWidth=0;isEnabled=actionable;alpha=if(actionable)1f else .42f;background=rippleBackground(AppColors.SurfaceRaised,20,AppColors.Border);contentDescription=if(config.label.isBlank())"Empty button" else config.label;setOnClickListener{handleButton(modeIndex,mode,page,i,config)}};row.addView(b,LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1f).apply{val g=dp(5);setMargins(g,g,g,g)})}}
        setContentView(root)
    }
    private fun handleButton(modeIndex:Int,mode:ModeConfig,page:PageConfig,buttonIndex:Int,config:BigButtonConfig){when(config.actionType){ButtonActionType.COMMAND->try{CommandDispatcher.send(this,modeIndex,mode,config.value,"button",buttonIndex,config.label);if(AppPrefs.global(this).vibration)vibrateBriefly()}catch(e:Exception){Toast.makeText(this,e.message?:"Could not send command",Toast.LENGTH_LONG).show()};ButtonActionType.PAGE->{val d=mode.pages.firstOrNull{it.id==config.value}?:return;pageBackStack.addLast(page.id);currentPageId=d.id;render(modeIndex)};ButtonActionType.BACK->navigateBack(modeIndex)}}
    private fun navigateBack(modeIndex:Int){if(pageBackStack.isNotEmpty()){currentPageId=pageBackStack.removeLast();render(modeIndex)}else if(currentPageId!=AppPrefs.MAIN_PAGE_ID){currentPageId=AppPrefs.MAIN_PAGE_ID;render(modeIndex)}}
    @Deprecated("Deprecated in Java") override fun onBackPressed(){if(pageBackStack.isNotEmpty()||currentPageId!=AppPrefs.MAIN_PAGE_ID)navigateBack(AppPrefs.activeModeIndex(this))else super.onBackPressed()}
    private fun runModeStartup(modeIndex:Int,mode:ModeConfig){if(!mode.startupEnabled)return;val command=mode.startupCommand.trim();if(command.isBlank())return;if(mode.wakeTarget&&mode.effectiveTargetPackage().isNotBlank())launchTargetPackage(mode.effectiveTargetPackage().trim());handler.postDelayed({try{CommandDispatcher.send(applicationContext,modeIndex,mode,command,"startup");if(AppPrefs.global(this).vibration)vibrateBriefly();if(mode.wakeTarget&&mode.returnToBigButtons)bringBigButtonsToFront()}catch(e:Exception){Toast.makeText(applicationContext,e.message?:"Could not send startup command",Toast.LENGTH_LONG).show()}},mode.startupDelayMs.toLong())}
    private fun launchTargetPackage(packageName:String){val i=Intent(Intent.ACTION_MAIN).apply{addCategory(Intent.CATEGORY_LAUNCHER);setPackage(packageName);addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)};try{startActivity(i)}catch(_:ActivityNotFoundException){Toast.makeText(this,"Could not open the configured receiver app",Toast.LENGTH_LONG).show()}}
    private fun bringBigButtonsToFront(){try{startActivity(Intent(this,MainActivity::class.java).apply{addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)})}catch(_:Exception){}}
    private fun vibrateBriefly(){val vibrator:Vibrator?=if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S)getSystemService(VibratorManager::class.java)?.defaultVibrator else {@Suppress("DEPRECATION") getSystemService(VIBRATOR_SERVICE) as? Vibrator};if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)vibrator?.vibrate(VibrationEffect.createOneShot(45,VibrationEffect.DEFAULT_AMPLITUDE))else {@Suppress("DEPRECATION") vibrator?.vibrate(45)}}
}
