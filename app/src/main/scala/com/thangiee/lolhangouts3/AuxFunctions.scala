package com.thangiee.lolhangouts3

import android.os.{Handler, Looper}
import android.support.design.widget.Snackbar
import android.view.View

import scala.concurrent.duration.FiniteDuration

trait AuxFunctions {

  def delay(duration: FiniteDuration)(fn: => Unit): Unit = {
    new Handler().postDelayed(() => fn, duration.toMillis)
  }

  def delayRunOnUi(duration: FiniteDuration)(fn: => Unit): Unit = {
    new Handler(Looper.getMainLooper).postDelayed(() => fn, duration.toMillis)
  }

  def runOnUi(f: => Unit): Unit = new Handler(Looper.getMainLooper).post(() => f)

  def snackbar(view: View, txt: String, millis: Int): Snackbar = Snackbar.make(view, txt, millis)

  def shortSnackbar(view: View, txt: String): Snackbar = snackbar(view, txt, Snackbar.LENGTH_SHORT)

  def longSnackbar(view: View, txt: String): Snackbar = snackbar(view, txt, Snackbar.LENGTH_LONG)

  def infSnackbar(view: View, txt: String): Snackbar = snackbar(view, txt, Snackbar.LENGTH_INDEFINITE)
}

object AuxFunctions extends AuxFunctions