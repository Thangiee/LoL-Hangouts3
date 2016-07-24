package com.thangiee.lolhangouts3

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.View
import android.widget.ImageView
import cats.implicits.futureInstance
import com.afollestad.materialdialogs.{DialogAction, MaterialDialog}
import com.afollestad.materialdialogs.MaterialDialog.{InputCallback, SingleButtonCallback}
import com.bumptech.glide.Glide
import com.mikepenz.materialdrawer.model._
import com.mikepenz.materialdrawer.model.interfaces.{IDrawerItem, IProfile}
import com.mikepenz.materialdrawer.util.{AbstractDrawerImageLoader, DrawerImageLoader}
import com.mikepenz.materialdrawer.{AccountHeader, AccountHeaderBuilder, Drawer, DrawerBuilder}
import com.thangiee.lolhangouts3.NavDrawer._
import enrichments._
import lolchat.{LoLChat, ops}
import lolchat.model._

trait NavDrawer extends BaseActivity {

  def session: Session
  def selectedDrawer: DrawerItem

  private lazy val profile  = new ProfileDrawerItem().withName("Loading...").withEmail("Loading...").withIcon(R.drawable.ic_summ_unknown)

  def colorCircle(color: TypedRes[TypedResource.ResColor]): Drawable = {
    val circle = TR.drawable.ic_circle.value
    circle.setTint(color.value)
    circle
  }

  private lazy val greenCircle = colorCircle(TR.color.md_light_green_500)
  private lazy val redCircle = colorCircle(TR.color.md_red_500)
  private lazy val greyCircle = colorCircle(TR.color.md_grey_500)

  private lazy val appearanceStatus =
    new ExpandableDrawerItem().withName("Online").withIcon(greenCircle).withSelectable(false).withSubItems(
      new SecondaryDrawerItem().withName("Online").withLevel(2).withIdentifier(online.id).withIcon(greenCircle),
      new SecondaryDrawerItem().withName("Away").withLevel(2).withIdentifier(away.id).withIcon(redCircle),
      new SecondaryDrawerItem().withName("Offline").withLevel(2).withIdentifier(offline.id).withIcon(greyCircle)
    )

  lazy val header: AccountHeader =
    new AccountHeaderBuilder()
    .withActivity(this)
    .withHeaderBackground(R.drawable.lol_gray_logo) // todo: make image size smaller
    .addProfiles(profile)
    .withOnAccountHeaderListener((view: View, iProfile: IProfile[_], b: Boolean) => true)
    .withOnAccountHeaderSelectionViewClickListener((view: View, iProfile: IProfile[_]) => true)
    .build()

  lazy val drawer: Drawer =
    new DrawerBuilder()
    .withActivity(this)
    .withAccountHeader(header)
    .withToolbar(toolbar)
    .addDrawerItems(
      new PrimaryDrawerItem().withIdentifier(friendList.id).withName("Friend List").withIcon(TR.drawable.ic_list.value),
      new PrimaryDrawerItem().withIdentifier(myProfile.id).withName("My Profile").withIcon(TR.drawable.ic_person.value),
      new PrimaryDrawerItem().withIdentifier(search.id).withName("Search Summoner").withIcon(TR.drawable.ic_search.value),
      new PrimaryDrawerItem().withIdentifier(scouter.id).withName("Game Scouter").withIcon(TR.drawable.ic_videogame_asset.value),
      new SectionDrawerItem().withName("Settings"),
      appearanceStatus,
      new PrimaryDrawerItem().withIdentifier(statusMsg.id).withName("Change Status Msg").withIcon(TR.drawable.ic_format_quote.value),
      new PrimaryDrawerItem().withIdentifier(preferences.id).withName("Preferences").withIcon(TR.drawable.ic_settings.value),
      new DividerDrawerItem(),
      new PrimaryDrawerItem().withIdentifier(ads.id).withName("Remove Ads").withIcon(TR.drawable.ic_thumb_up.value),
      new PrimaryDrawerItem().withIdentifier(logout.id).withName("Logout").withIcon(TR.drawable.ic_exit_to_app.value)
    )
    .withOnDrawerItemClickListener((_: View, _: Int, item: IDrawerItem[_, _ <: ViewHolder]) => handleDrawerItemClick(item.getIdentifier))
    .build()

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    drawer.setSelection(selectedDrawer.id)

    DrawerImageLoader.init(new AbstractDrawerImageLoader {
      override def set(imageView: ImageView, uri: Uri, placeholder: Drawable): Unit = imageView.loadImg(uri.toString)
      override def cancel(imageView: ImageView): Unit = Glide.clear(imageView)
    })

    CurrentUserInfo.load(session).map(user => runOnUi {
      profile.withName(user.summoner.name)
      profile.withEmail(user.profile.statusMsg)
      val summIconUrl = s"http://avatar.leagueoflegends.com/${session.region.abbr}/${user.summoner.name.toLowerCase().replace(" ", "")}.png"
      profile.withIcon(summIconUrl)
      header.updateProfile(profile)
    })
  }

  def handleDrawerItemClick(id: Long): Boolean = {
    val (keepOpen, close) = (true, false)
    if (id == selectedDrawer.id) keepOpen // ignore when clicking on an already selected item
    else id match {
      case friendList.id =>
        close
      case myProfile.id =>
        close
      case search.id =>
        close
      case scouter.id =>
        close
      case preferences.id =>
        close
      case ads.id =>
        drawer.setSelection(selectedDrawer.id)
        keepOpen
      case logout.id =>
        drawer.setSelection(selectedDrawer.id)
        keepOpen
      case statusMsg.id =>
        drawer.setSelection(selectedDrawer.id)
        new MaterialDialog.Builder(ctx)
          .title("Set Status Message")
          .positiveText("Update")
          .negativeText("Cancel")
          .input("Enter new status message", "", new InputCallback {
            def onInput(materialDialog: MaterialDialog, charSequence: CharSequence): Unit = {}
          })
          .onPositive((dialog: MaterialDialog, _: DialogAction) => {
            LoLChat.run(ops.modifyProfile(_.copy(statusMsg = dialog.getInputEditText.txt))(session)).map { p =>
              CurrentUserInfo.saveStatusMsg(p.statusMsg, session)
              runOnUi {
                profile.withEmail(p.statusMsg)
                header.updateProfile(profile)
              }
            }
          })
          .show()
        keepOpen
      case online.id =>
        drawer.updateItem(appearanceStatus.withName("Online").withIcon(greenCircle))
        drawer.setSelection(selectedDrawer.id)
        keepOpen
      case away.id =>
        drawer.updateItem(appearanceStatus.withName("Away").withIcon(redCircle))
        drawer.setSelection(selectedDrawer.id)
        keepOpen
      case offline.id =>
        drawer.updateItem(appearanceStatus.withName("Offline").withIcon(greyCircle))
        drawer.setSelection(selectedDrawer.id)
        keepOpen
      case _ => keepOpen
    }
  }

}

object NavDrawer {
  case class DrawerItem(id: Int) extends AnyVal

  val friendList  = DrawerItem(100)
  val myProfile   = DrawerItem(200)
  val search      = DrawerItem(300)
  val scouter     = DrawerItem(400)
  val preferences = DrawerItem(500)
  protected val ads = DrawerItem(600)
  protected val logout = DrawerItem(700)
  protected val statusMsg = DrawerItem(800)
  protected val online = DrawerItem(900)
  protected val away = DrawerItem(901)
  protected val offline = DrawerItem(902)
}
