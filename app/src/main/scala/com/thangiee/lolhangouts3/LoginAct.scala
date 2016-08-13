package com.thangiee.lolhangouts3

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import com.afollestad.materialdialogs.{DialogAction, MaterialDialog}
import com.hanhuy.android.extensions._
import com.thangiee.lolhangouts3.TypedViewHolder.login_act
import com.thangiee.lolhangouts3.enrichments._
import lolchat._
import lolchat.model._
import boopickle.Default._
import com.thangiee.metadroid.Case
import ClientApi._
import android.net.Uri
import autowire._
import cats.data.OptionT

import scala.concurrent.duration._

@Case class LoginAct(errMsg: Option[String]) extends BaseActivity {
  type RootView = RelativeLayout
  lazy val selectedRegion   = RegionItem.loadSelected
  lazy val views: login_act = TypedViewHolder.setContentView(this, TR.layout.login_act)
  lazy val toolbar          = views.toolbar.rootView

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    getSupportActionBar.setTitle(selectedRegion.map(_.name).getOrElse("Login"))
    toolbar.setNavigationOnClickListener(_ => startActivity(new Intent(this, classOf[RegionSelectionAct])))
    views.loginBtn.morphToNormalBtn("Login", R.color.login_btn_normal)

    errMsg.foreach(errMsg =>
      new MaterialDialog.Builder(ctx)
        .title("Error")
        .content(errMsg)
        .positiveText("Ok")
        .show()
    )

    selectedRegion match {
      case Some(regionItem) =>
        views.loginBtn.onClick0 {
          val sess = Session(views.usernameEdit.txt, views.passwordEdit.txt, regionItem.region, acceptFriendRequest = true)
          attemptLogin(sess)
        }
      case None => startActivity(new Intent(this, classOf[RegionSelectionAct]))
    }
  }

  def attemptLogin(sess: Session): Unit = {
    views.loginBtn.morphToProgress(TR.color.md_grey_300.value, TR.color.md_light_blue_500.value, 5, 300, 10, 500)
    delay(500.millis)(views.loginBtn.fillProgressBar(0, 37, 370.millis))

    val loginOp = if (views.offlineLoginSwitch.isChecked) offlineLogin else login
    LoLChat.run(loginOp(sess)).flatMap(_ => CurrentUserInfo.load(sess)).fold(failLogin, succLogin)

    def failLogin(chatError: Error): Unit = delayRunOnUi(1.second) {
      List(views.usernameEdit, views.passwordEdit).foreach(_.setError(chatError.msg))
      views.loginBtn.morphToErrorBtn
    }

    def succLogin(userInfo: CurrentUserInfo): Unit = delayRunOnUi(1000.millis) {
      views.loginBtn.fillProgressBar(37, 100, 500.millis)
      delay(1.second)(views.loginBtn.morphToSuccessBtn)
      delay(1.5.seconds) { startActivity(FriendListAct(userInfo.summoner.id)); finish() }
    }
  }

  override def onResume(): Unit = {
    super.onResume()
    LoginConfig.load.foreach(config => {
      views.usernameEdit.setText(config.user)
      views.passwordEdit.setText(config.passwd)
      views.savePasswdSwitch.setChecked(config.passwd.nonEmpty)
      views.offlineLoginSwitch.setChecked(config.offlineLogin)
    })

    OptionT(clientApi.getBuildVersion().call()).map { newestBuildVer =>
      val packageName: ErrMsg = getPackageName
      if (getPackageManager.getPackageInfo(packageName, 0).versionCode < newestBuildVer.code) runOnUi {
        new MaterialDialog.Builder(ctx)
          .title("Update Available!")
          .content(s"New app version ${newestBuildVer.version} is now available in the Play store. ${newestBuildVer.msg}")
          .negativeText("Later")
          .positiveText("Update")
          .onPositive((_: MaterialDialog, _: DialogAction) => {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)))  // open app page in play store
          })
          .show()
      }
    }
  }

  override def onPause(): Unit = {
    super.onPause()
    val config = LoginConfig(
      views.usernameEdit.txt,
      if (views.savePasswdSwitch.isChecked) views.passwordEdit.txt else "",
      views.offlineLoginSwitch.isChecked
    )
    LoginConfig.save(config)
  }
}

object LoginAct {
  def apply()(implicit ctx: Ctx): Intent = LoginAct(None)
}

case class LoginConfig(user: String, passwd: String, offlineLogin: Boolean)

object LoginConfig {
  import AuxFunctions._

  val key = "4474d03OEG"

  def save(config: LoginConfig) = prefsPut(LoginConfig.key, config)
  def load: Option[LoginConfig] = prefsGet[LoginConfig](LoginConfig.key)
}