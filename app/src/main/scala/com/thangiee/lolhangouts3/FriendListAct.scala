package com.thangiee.lolhangouts3

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.{LinearLayoutManager, Toolbar}
import android.view.ViewGroup
import android.widget.{LinearLayout, TextView}
import com.jude.easyrecyclerview.adapter.{BaseViewHolder, RecyclerArrayAdapter}
import com.jude.easyrecyclerview.decoration.DividerDecoration
import com.makeramen.roundedimageview.RoundedImageView
import com.thangiee.lolhangouts3.NavDrawer.DrawerItem
import com.thangiee.lolhangouts3.TypedViewHolder.friend_list_act
import lolchat._
import lolchat.data.Region
import lolchat.model._
import enrichments._

import scala.collection.JavaConversions._

class FriendListAct extends SessionAct with NavDrawer {
  type RootView = LinearLayout
  lazy val views  : friend_list_act = TypedViewHolder.setContentView(this, TR.layout.friend_list_act)
  lazy val toolbar: Toolbar         = views.toolbar.rootView

  val selectedDrawer: DrawerItem = NavDrawer.friendList

  lazy val friendListAdapter = FriendItem.adapter(session.region)

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    val divider = new DividerDecoration(TR.color.divider.value, 1.dp, 72.dp, 0)
    divider.setDrawLastItem(false)

    views.recyclerView +
      (_.setLayoutManager(new LinearLayoutManager(this))) +
      (_.setAdapter(friendListAdapter.parentType)) +
      (_.addItemDecoration(divider))
  }

  override def onResume(): Unit = {
    super.onResume()
    refreshFriendList()
    session.friendListStream.foreach(_ => refreshFriendList())
  }

  override def onPause(): Unit = {
    super.onPause()
    stopAllSessionStreams()
  }

  def refreshFriendList(): Unit = {
    LoLChat.run(friends(session)).map(fs => runOnUi {
      friendListAdapter.clear()
      friendListAdapter.addAll(fs.sortBy(f => (!f.isOnline, f.name)))
      friendListAdapter.notifyDataSetChanged()
    })
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

object FriendItem {
  def viewHolder(viewGroup: ViewGroup, region: Region)(implicit ctx: Ctx): BaseViewHolder[Friend] =
    new BaseViewHolder[Friend](viewGroup, TR.layout.friend_line_item.id) {
      val avatarImg = $[RoundedImageView](R.id.avatarImg)
      val nameTv = $[TextView](R.id.nameTv)
      val statusTv = $[TextView](R.id.statusTv)
      val msgTv = $[TextView](R.id.msgTv)

      override def setData(friend: Friend): Unit = {
        avatarImg.loadSummIcon(friend.name, region)
        nameTv.setText(friend.name)
        msgTv.setText("todo")

        if (friend.isOnline) {
          friend.chatMode match {
            case Chat => statusTv + (_.setText("Online")) + (_.setTextColor(TR.color.md_green_500.value))
            case AFK  => statusTv + (_.setText("Away")) + (_.setTextColor(TR.color.md_red_500.value))
            case Busy =>
              friend.gameStatus match {
                case Some("inGame") =>
                  val gameTime = Math.round((System.currentTimeMillis() - friend.gameStartTime.getOrElse(0L)) / 60000)
                  statusTv + (_.setText(s"In Game: ${friend.selectedChamp.getOrElse("???")} ($gameTime mins)"))
                case Some("championSelect") => statusTv + (_.setText("Champion Selection"))
                case Some("inQueue")        => statusTv + (_.setText("In Queue"))
                case Some(other)            => statusTv + (_.setText(other))
                case None                   => statusTv + (_.setText("Busy"))
              }
          }
        } else {
          statusTv + (_.setText("Offline")) + (_.setTextColor(TR.color.md_grey_500.value))
        }

      }
    }

  def adapter(region: Region)(implicit ctx: Ctx) = new RecyclerArrayAdapter[Friend](ctx) {
    def OnCreateViewHolder(viewGroup: ViewGroup, i: Int): BaseViewHolder[_] = viewHolder(viewGroup, region)
  }
}