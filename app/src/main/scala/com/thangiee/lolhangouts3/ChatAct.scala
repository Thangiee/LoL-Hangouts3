package com.thangiee.lolhangouts3
import java.util.Date

import android.os.Bundle
import android.support.v7.widget.{LinearLayoutManager, Toolbar}
import android.view.ViewGroup
import android.widget.{RelativeLayout, TextView, Toast}
import autowire._
import com.hanhuy.android.extensions._
import com.jude.easyrecyclerview.adapter.{BaseViewHolder, RecyclerArrayAdapter}
import com.makeramen.roundedimageview.RoundedImageView
import com.thangiee.metadroid.Case
import com.thangiee.lolhangouts3.ClientApi._
import com.thangiee.lolhangouts3.enrichments._
import lolchat._
import lolchat.data.Region
import lolchat.model._
import org.joda.time.format.DateTimeFormat
import share.Message

import scala.concurrent.duration._

@Case class ChatAct(userSummId: Int, friend: Friend) extends SessionAct {
  type RootView = RelativeLayout
  lazy val views: TypedViewHolder.chat_act = TypedViewHolder.setContentView(this, TR.layout.chat_act)
  lazy val toolbar: Toolbar = views.toolbar.rootView

  lazy val chatMsgAdapter = ChatMessage.adapter(session, friend)

  lazy val sendBtn = views.sendBtn
  lazy val msgField = views.messageEdt

  lazy val showReceivingMsg = session.msgStream.collect {
    case msg if msg.fromId == friend.id => runOnUi(addMessageToChatView(Message(userSummId, msg.fromId.toInt, msg.txt, sender = false)))
  }

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    getSupportActionBar.setTitle(friend.name)
    toolbar.setNavigationOnClickListener(_ => finish())

    showReceivingMsg // initialize

    val llm = new LinearLayoutManager(this)
    llm.setReverseLayout(true)
    llm.setStackFromEnd(true)

    views.recyclerView + (_.setLayoutManager(llm)) + (_.setAdapter(chatMsgAdapter.parentType))

    msgField.onClick0(if (msgField.hasFocus) delay(200.millis)(scrollToBottom()))

    clientApi.getRecentMsgsBtw(userSummId, friend.id.toInt, 1000, filterRead = false).call().toAsyncResult
      .map(msgs => runOnUi { chatMsgAdapter.addItems(msgs); scrollToBottom() })

    sendBtn.onClick0 {
      val txtMsg = msgField.txt
      if (txtMsg.nonEmpty) LoLChat.run(sendMsg(friend.id, txtMsg)(session)).fold(
        err => Toast.makeText(ctx, s"Failed to send message: ${err.msg}", Toast.LENGTH_LONG).show(),
        succ => {
          val msg: Message = Message(userSummId, friend.id.toInt, txtMsg)
          clientApi.saveMsg(msg).call()
          runOnUi { msgField.setText(""); addMessageToChatView(msg) }
        }
      )
    }
  }

  override def onResume(): Unit = {
    super.onResume()
    clientApi.markMsgsRead(userSummId, friend.id.toInt).call()
  }

  override def onDestroy(): Unit = {
    super.onDestroy()
    chatMsgAdapter.clear()
    showReceivingMsg.kill()
  }

  def addMessageToChatView(message: Message): Unit = {
    chatMsgAdapter.insertItem(message, 0) // add to the front
    scrollToBottom()
  }

  def scrollToBottom(): Unit = views.recyclerView.scrollToPosition(chatMsgAdapter.getCount - 2) // scroll to new message
}

object ChatMessage {
  def adapter(sess: Session, friend: Friend)(implicit ctx: Ctx) = new RecyclerArrayAdapter[Message](ctx) {
    val (myMsg, otherMsg) = (0, 1)

    def OnCreateViewHolder(viewGroup: ViewGroup, viewType: Int): BaseViewHolder[_] = {
      if (viewType == myMsg) myMsgViewHolder(viewGroup, R.layout.chat_mine_msg)
      else                   friendMsgViewHolder(viewGroup, R.layout.chat_friend_msg, friend, sess.region)
    }

    override def getCount: Int = 2

    override def getViewType(position: Int): Int = {
      val msg = getItem(position)
      if (msg.sender) myMsg else otherMsg
    }
  }

  def myMsgViewHolder(viewGroup: ViewGroup, layout: Int)(implicit ctx: Ctx) =
    new BaseViewHolder[Message](viewGroup, layout) {
      val msgTv = $[TextView](R.id.msgTv)
      val timestamp = $[TextView](R.id.timestampTv)

      override def setData(msg: Message): Unit = {
        msgTv.setText(msg.text)
        timestamp.setText(formatTime(msg.timestamp))
      }
    }

  def friendMsgViewHolder(viewGroup: ViewGroup, layout: Int, friend: Friend, region: Region)(implicit ctx: Ctx) =
    new BaseViewHolder[Message](viewGroup, layout) {
      val msgTv = $[TextView](R.id.msgTv)
      val summImg = $[RoundedImageView](R.id.summImg)
      val timestamp = $[TextView](R.id.timestampTv)

      override def setData(msg: Message): Unit = {
        msgTv.setText(msg.text)
        summImg.loadSummIcon(friend.name, region, friend.profileIconId)
        timestamp.setText(formatTime(msg.timestamp))
      }
    }

  private val timeFmt1 = DateTimeFormat.forPattern("h:mm a")
  private val timeFmt2 = DateTimeFormat.forPattern("MMM dd, YYYY")

  private def formatTime(date: Date): String = {
    val isWithIn24Hrs = date.after(new Date(System.currentTimeMillis() - 24.hours.toMillis))
    if (isWithIn24Hrs) timeFmt1.print(date.getTime) else timeFmt2.print(date.getTime)
  }
}