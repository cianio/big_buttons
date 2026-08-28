package com.bigbuttons.remote

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.widget.TextView
import kotlin.math.roundToInt

object AppColors { val Background=Color.rgb(12,14,18); val Surface=Color.rgb(24,27,34); val SurfaceRaised=Color.rgb(33,37,46); val Border=Color.rgb(67,73,86); val Text=Color.rgb(245,247,250); val Muted=Color.rgb(168,175,188); val Accent=Color.rgb(126,166,255); val AccentDark=Color.rgb(44,75,138); val Danger=Color.rgb(167,76,76) }
fun Activity.dp(value:Int)= (value*resources.displayMetrics.density).roundToInt()
fun Activity.applySystemBarPadding(view:View,horizontalDp:Int=12,verticalDp:Int=10){ val h=dp(horizontalDp); val v=dp(verticalDp); view.setPadding(h,v,h,v); view.setOnApplyWindowInsetsListener{target,insets-> if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){ val bars=insets.getInsets(WindowInsets.Type.systemBars()); target.setPadding(h+bars.left,v+bars.top,h+bars.right,v+bars.bottom) } else { @Suppress("DEPRECATION") target.setPadding(h+insets.systemWindowInsetLeft,v+insets.systemWindowInsetTop,h+insets.systemWindowInsetRight,v+insets.systemWindowInsetBottom) }; insets } }
fun Activity.roundedBackground(fillColor:Int,radiusDp:Int=18,strokeColor:Int?=null,strokeWidthDp:Int=1)=GradientDrawable().apply{shape=GradientDrawable.RECTANGLE;setColor(fillColor);cornerRadius=dp(radiusDp).toFloat();if(strokeColor!=null)setStroke(dp(strokeWidthDp),strokeColor)}
fun Activity.rippleBackground(fillColor:Int,radiusDp:Int=18,strokeColor:Int?=null)=RippleDrawable(ColorStateList.valueOf(Color.argb(55,255,255,255)),roundedBackground(fillColor,radiusDp,strokeColor),null)
fun TextView.makeSectionTitle(){setTextColor(AppColors.Text);textSize=18f;setTypeface(typeface,Typeface.BOLD)}
