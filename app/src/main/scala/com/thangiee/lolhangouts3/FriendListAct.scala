package com.thangiee.lolhangouts3

import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.{LinearLayoutManager, Toolbar}
import android.text.InputType
import android.view.{View, ViewGroup}
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.{AdapterView, RelativeLayout, TextView}
import cats.data.Xor
import com.afollestad.materialdialogs.MaterialDialog
import com.gordonwong.materialsheetfab.MaterialSheetFab
import com.hanhuy.android.extensions._
import com.jude.easyrecyclerview.adapter.{BaseViewHolder, RecyclerArrayAdapter}
import com.jude.easyrecyclerview.decoration.DividerDecoration
import com.makeramen.roundedimageview.RoundedImageView
import com.thangiee.lolhangouts3.NavDrawer.DrawerItem
import com.thangiee.lolhangouts3.TypedViewHolder._
import com.thangiee.lolhangouts3.enrichments._
import lolchat._
import lolchat.data.Region
import lolchat.model._
import riotapi.free.RiotApiOps

import scala.collection.JavaConversions._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class FriendListAct extends SessionAct with NavDrawer {
  type RootView = RelativeLayout
  lazy val views  : friend_list_act = TypedViewHolder.setContentView(this, TR.layout.friend_list_act)
  lazy val toolbar: Toolbar         = views.toolbar.rootView

  val selectedDrawer: DrawerItem = NavDrawer.friendList

  lazy val friendListAdapter  = FriendItem.adapter(session.region)
  lazy val friendGroupAdapter = MaterialSpinnerAdapter(Seq("All", "Online", "Offline"))

  lazy val materialSheetFab = new MaterialSheetFab[Fab](
    views.fab, views.fabSheet, views.overlay, TR.color.md_white.value, TR.color.accent.value)

  lazy val friendListEventHandler = session.friendListStream.foreachEvent(_ => if (isActVisible) refreshFriendList())
  lazy val msgEventHandler = session.msgStream.foreachEvent(msg => {
    // todo: improve
    val notif = new Notification.Builder(ctx)
      .setSmallIcon(TR.drawable.ic_launcher.resid)
      .setContentTitle("New Message")
      .setContentText(msg.txt)
      .setStyle(new Notification.InboxStyle().setSummaryText("Open chat"))
      .setPriority(Notification.PRIORITY_HIGH)
      .build()

    notifyMgr.notify(100, notif)
  })

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    val friendGroupSpinner: toolbar_spinner =
      TypedViewHolder.inflate(getLayoutInflater, TR.layout.toolbar_spinner, toolbar, attach = false)

    def setupFriendGroupsToolbarSpinner(): Unit = {
      // setup the spinner in the toolbar to filter friend list by groups
      val lp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
      toolbar.addView(friendGroupSpinner.rootView, lp)
      friendGroupSpinner.spinner +
        (_.setAdapter(friendGroupAdapter)) +
        (_.setOnItemSelectedListener(new OnItemSelectedListener {
          def onNothingSelected(adapterView: AdapterView[_]): Unit = {}
          def onItemSelected(a: AdapterView[_], v: View, position :Int,  l :Long): Unit =
            refreshFriendList(friendGroupAdapter.getItem(position))
        }))

      LoLChat.run(groupNames(session)).map(groups => friendGroupAdapter.addItems(groups.filter(_ != "**Default")))
    }

    def setupFriendsList(): Unit = {
      friendListAdapter.setOnItemClickListener(i => {
        val friend = friendListAdapter.getItem(i)
        startActivity(ChatAct(friend))
      })

      views.recyclerView +
        (_.setLayoutManager(new LinearLayoutManager(this))) +
        (_.setAdapter(friendListAdapter.parentType)) +
        (_.addItemDecoration(new DividerDecoration(TR.color.divider.value, 1.dp, 72.dp, 0)))
    }

    setupFriendGroupsToolbarSpinner()
    setupFriendsList()

    // initialize lazies
    materialSheetFab
    friendListEventHandler
    msgEventHandler

    views.sendFriendReqBtn.onClick0 {
      def doFriendReq(name: String): Unit = {
        val result = for {
          summ <- riotApi.run(RiotApiOps.summonerByName(name.toString), session.region)
          _    <- LoLChat.run(sendFriendRequest(summ.id.toString)(session))
        } yield ()

        result.value.map {
          case Xor.Right(_)               => s"Friend Request sent to $name."
          case Xor.Left(Error(404, _, _)) => s"Request not sent, $name not found."
          case Xor.Left(err)              => s"${err.code}: Unable to send friend request"
        }.foreach(msg => longSnackbar(views.coordinator, msg).show())
      }

      materialSheetFab.hideSheet()
      delay(.5.second) {
        new MaterialDialog.Builder(ctx)
        .title("Send Friend Request")
        .inputType(InputType.TYPE_CLASS_TEXT)
        .onInput("Summoner name", "", doFriendReq)
        .positiveText("Send")
        .negativeText("Cancel")
        .show()
      }
    }

    views.createGroupBtn.onClick0 {
      def doCreateNewGroup(name: String): Unit = {
        LoLChat.run(createGroup(name)(session))
          .fold(err => s"${err.code}: Fail to create group.", _ => s"$name group created.")
          .foreach(msg => runOnUi {
            friendGroupAdapter.addItem(name)
            friendGroupAdapter.notifyDataSetChanged()
            longSnackbar(views.coordinator, msg).show()
          })
      }

      materialSheetFab.hideSheet()
      delay(.5.second) {
        new MaterialDialog.Builder(ctx)
        .title("Create New Friend Group")
        .inputType(InputType.TYPE_CLASS_TEXT)
        .onInput("New group name", "", doCreateNewGroup)
        .positiveText("Create")
        .negativeText("Cancel")
        .show()
      }
    }

    views.moveFriendBtn.onClick0 {
      def doMoveFriendsToGroup(names: Seq[String], group: String): Unit = {
        Future.sequence(names.map(name => LoLChat.run(moveFriendToGroup(name, group)(session)).value))
          .foreach { _ =>
            refreshFriendList(groupFilter = friendGroupSpinner.spinner.getSelectedItem.toString)
            longSnackbar(views.coordinator, s"${names.size} friends moved to $group.").show()
          }
      }

      materialSheetFab.hideSheet()

      val groupItems = Await.result(LoLChat.run(groupNames(session)).getOrElse(Vector.empty), 1.minute)
      val friendsItems = Await.result(LoLChat.run(friends(session)).getOrElse(Vector.empty), 1.minute)

      new MaterialDialog.Builder(ctx)
        .title("Move Friends to Group")
        .content("Select a group to move friends into.")
        .items(groupItems)
        .itemsCallbackSingleChoice(0, (_: MaterialDialog, _: View, i: Int, selection: CharSequence) => { next(selection.toString); true })
        .positiveText("Next")
        .negativeText("Cancel")
        .show()

      def next(selectedGroup: String) = new MaterialDialog.Builder(ctx)
        .title("Move Friends to Group")
        .content(s"Select friends to move to $selectedGroup group.")
        .items(friendsItems.collect { case f if !f.groupName.contains(selectedGroup) => f.name }.sorted)
        .itemsCallbackMultiChoice(null, (_: MaterialDialog, _: Array[Integer], names: Array[CharSequence]) => {
          doMoveFriendsToGroup(names.map(_.toString), selectedGroup)
          true
        })
        .positiveText("Move")
        .negativeText("Cancel")
        .show()
    }
  }

  override def onResume(): Unit = {
    super.onResume()
    refreshFriendList()
  }

  override def onPause(): Unit = {
    super.onPause()
  }

  override def onDestroy(): Unit = {
    super.onDestroy()
    friendListEventHandler.kill()
    msgEventHandler.kill()
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
    if (materialSheetFab.isSheetVisible) {
      materialSheetFab.hideSheet()
    } else {
      // Make it so the user can come back to this screen in the current state after they press
      // the back button to go to the android home screen. Without this code, the login screen
      // will be launched instead.
      val androidHomeScreen = new Intent(Intent.ACTION_MAIN)
      androidHomeScreen.addCategory(Intent.CATEGORY_HOME)
      androidHomeScreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      startActivity(androidHomeScreen)
    }
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