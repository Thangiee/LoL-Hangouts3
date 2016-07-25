package com.thangiee.lolhangouts3

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.widget.LinearLayout
import com.thangiee.lolhangouts3.NavDrawer.DrawerItem
import com.thangiee.lolhangouts3.TypedViewHolder.friend_list_act
import lolchat.data.Region

class FriendListAct extends SessionAct with NavDrawer {
  type RootView = LinearLayout
  lazy val views  : friend_list_act = TypedViewHolder.setContentView(this, TR.layout.friend_list_act)
  lazy val toolbar: Toolbar         = views.toolbar.rootView
  val selectedDrawer: DrawerItem = NavDrawer.friendList

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
  }

  override def onBackPressed(): Unit = {
    // Make it so the user can come back to this screen in the current state after they press
    // the back button to go to the android home screen. Without this code, the login screen
    // will be launched instead.
    val androidHomeScreen = new Intent(Intent.ACTION_MAIN)
    androidHomeScreen.addCategory(Intent.CATEGORY_HOME)
    androidHomeScreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(androidHomeScreen)
  }
}

object FriendListAct {
  def apply()(implicit ctx: Ctx): Intent = new Intent(ctx, classOf[FriendListAct])
}
