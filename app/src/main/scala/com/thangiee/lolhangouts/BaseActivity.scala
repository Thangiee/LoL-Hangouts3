package com.thangiee.lolhangouts

import android.app.{Notification, NotificationManager}
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import com.pixplicity.easyprefs.library.Prefs
import enrichments._

trait BaseActivity extends AppCompatActivity with TypedFindView with AuxFunctions {
  implicit val ctx: Ctx = this
  type RootView <: View
  def views: TypedViewHolder[RootView]
  def toolbar: Toolbar

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    views
    setSupportActionBar(toolbar.withWhiteNavArrow)
  }

  val infSnackbar  : (String) => Snackbar = AuxFunctions.infSnackbar(views.rootView, _)
  val longSnackbar : (String) => Snackbar = AuxFunctions.longSnackbar(views.rootView, _)
  val shortSnackbar: (String) => Snackbar = AuxFunctions.shortSnackbar(views.rootView, _)

  lazy val notifyMgr = getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]

  def showNotification(id: Int, notification: Notification) = {
    if (Prefs.getBoolean(TR.string.pref_notify_vibrate.value, true)) {
      notification.defaults |= Notification.DEFAULT_VIBRATE
    }
    notifyMgr.notify(id, notification)
  }

  val msgNotifiId = 100
  def showMsgNotifi(notification: Notification) = showNotification(msgNotifiId, notification)
}
