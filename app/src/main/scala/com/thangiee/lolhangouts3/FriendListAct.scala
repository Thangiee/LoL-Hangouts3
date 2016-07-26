package com.thangiee.lolhangouts3

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.{LinearLayoutManager, Toolbar}
import android.view.{View, ViewGroup}
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.{AdapterView, LinearLayout, TextView}
import com.jude.easyrecyclerview.adapter.{BaseViewHolder, RecyclerArrayAdapter}
import com.jude.easyrecyclerview.decoration.DividerDecoration
import com.makeramen.roundedimageview.RoundedImageView
import com.thangiee.lolhangouts3.NavDrawer.DrawerItem
import com.thangiee.lolhangouts3.TypedViewHolder._
import com.thangiee.lolhangouts3.enrichments._
import lolchat._
import lolchat.data.Region
import lolchat.model._

import scala.collection.JavaConversions._

class FriendListAct extends SessionAct with NavDrawer {
  type RootView = LinearLayout
  lazy val views  : friend_list_act = TypedViewHolder.setContentView(this, TR.layout.friend_list_act)
  lazy val toolbar: Toolbar         = views.toolbar.rootView

  val selectedDrawer: DrawerItem = NavDrawer.friendList

  lazy val friendListAdapter  = FriendItem.adapter(session.region)
  lazy val friendGroupAdapter = MaterialSpinnerAdapter(Seq("All", "Online", "Offline"))


  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    val friendGroupSpinner: toolbar_spinner =
      TypedViewHolder.inflate(getLayoutInflater, TR.layout.toolbar_spinner, toolbar, attach = false)

    // setup the spinner in the toolbar to filter friend list by groups
    val lp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    toolbar.addView(friendGroupSpinner.rootView, lp)

    LoLChat.run(groupNames(session)).map(groups => friendGroupAdapter.addItems(groups.filter(_ != "**Default")))

    friendGroupSpinner.spinner +
      (_.setAdapter(friendGroupAdapter)) +
      (_.setOnItemSelectedListener(new OnItemSelectedListener {
        def onNothingSelected(adapterView: AdapterView[_]): Unit = {}
        def onItemSelected(a: AdapterView[_], v: View, position :Int,  l :Long): Unit =
          refreshFriendList(friendGroupAdapter.getItem(position))
      }))

    val divider = new DividerDecoration(TR.color.divider.value, 1.dp, 72.dp, 0)

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

  def refreshFriendList(groupFilter: String = "all"): Unit = {
    LoLChat.run(friends(session)).map(fs => runOnUi {
      val filteredFriends = groupFilter.toLowerCase() match {
        case "all"     => fs
        case "online"  => fs.filter(_.isOnline)
        case "offline" => fs.filter(!_.isOnline)
        case group     => fs.filter(_.groupName.map(_.toLowerCase).contains(group))
      }

      friendListAdapter.clear()
      friendListAdapter.addAll(filteredFriends.sortBy(f => (!f.isOnline, f.name)))
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
        avatarImg.loadSummIcon(friend.name, region, friend.profileIconId)
        nameTv.setText(friend.name)
        msgTv.setText("todo")

        if (friend.isOnline) {
          friend.chatMode match {
            case Chat => statusTv.textWithColor("Online", TR.color.md_green_500)
            case AFK  => statusTv.textWithColor("Away", TR.color.md_red_500)
            case Busy =>
              val orangeTxt = (txt: String) => (tv: TextView) => tv.textWithColor(txt, TR.color.md_orange_500)
              friend.gameStatus match {
                case Some("inGame") =>
                  val gameTime = Math.round((System.currentTimeMillis() - friend.gameStartTime.getOrElse(0L)) / 60000)
                  statusTv + orangeTxt(s"In Game: ${friend.selectedChamp.getOrElse("???")} ($gameTime mins)")
                case Some("championSelect") => statusTv + orangeTxt("Champion Selection")
                case Some("inQueue")        => statusTv + orangeTxt("In Queue")
                case Some(other)            => statusTv + orangeTxt(other)
                case None                   => statusTv + orangeTxt("Busy")
              }
          }
        } else {
          statusTv.textWithColor("Offline", TR.color.md_grey_500)
        }

      }
    }

  def adapter(region: Region)(implicit ctx: Ctx) = new RecyclerArrayAdapter[Friend](ctx) {
    def OnCreateViewHolder(viewGroup: ViewGroup, i: Int): BaseViewHolder[_] = viewHolder(viewGroup, region)
  }
}