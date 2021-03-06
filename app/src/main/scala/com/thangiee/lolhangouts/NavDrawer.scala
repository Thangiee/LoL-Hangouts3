package com.thangiee.lolhangouts

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.View
import android.widget.ImageView
import cats.implicits._
import com.afollestad.materialdialogs.MaterialDialog
import com.anjlab.android.iab.v3.{BillingProcessor, TransactionDetails}
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mikepenz.materialdrawer.model._
import com.mikepenz.materialdrawer.model.interfaces.{IDrawerItem, IProfile}
import com.mikepenz.materialdrawer.util.{AbstractDrawerImageLoader, DrawerImageLoader}
import com.mikepenz.materialdrawer.{AccountHeader, AccountHeaderBuilder, Drawer, DrawerBuilder}
import com.pixplicity.easyprefs.library.Prefs
import com.thangiee.lolhangouts.NavDrawer._
import com.thangiee.lolhangouts.enrichments._
import lolchat.model._
import lolchat.{LoLChat, ops}

trait NavDrawer extends SessionAct with BillingProcessor.IBillingHandler {

  def selectedDrawer: DrawerItem

  private val SKU_REMOVE_ADS = "lolhangouts.remove.ads"
  private lazy val billingProcessor = new BillingProcessor(ctx, TR.string.play_service_key.value, this)

  private lazy val profile  = new ProfileDrawerItem().withName("Loading...").withEmail("Loading...").withIcon(R.drawable.ic_summ_unknown)

  def colorCircle(color: TypedRes[TypedResource.ResColor]): Drawable = {
    val circle = TR.drawable.ic_circle.value
    circle.setTint(color.value)
    circle
  }

  private lazy val greenCircle = colorCircle(TR.color.status_online)
  private lazy val redCircle = colorCircle(TR.color.status_away)
  private lazy val greyCircle = colorCircle(TR.color.status_offline)

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
      new PrimaryDrawerItem().withIdentifier(friendList.id).withName("Friend List").withIconAppColor(TR.drawable.ic_list.value),
      new PrimaryDrawerItem().withIdentifier(myProfile.id).withName("My Profile").withIconAppColor(TR.drawable.ic_person.value),
      new PrimaryDrawerItem().withIdentifier(search.id).withName("Search Summoner").withIconAppColor(TR.drawable.ic_search.value),
      new PrimaryDrawerItem().withIdentifier(scouter.id).withName("Game Scouter").withIconAppColor(TR.drawable.ic_videogame_asset.value),
      new SectionDrawerItem().withName("Settings"),
      appearanceStatus,
      new PrimaryDrawerItem().withIdentifier(statusMsg.id).withName("Change Status Msg").withIconAppColor(TR.drawable.ic_format_quote.value),
      new PrimaryDrawerItem().withIdentifier(preferences.id).withName("Preferences").withIconAppColor(TR.drawable.ic_settings.value),
      new DividerDrawerItem(),
      new PrimaryDrawerItem().withIdentifier(ads.id).withName("Remove Ads").withIconAppColor(TR.drawable.ic_thumb_up.value),
      new PrimaryDrawerItem().withIdentifier(logout.id).withName("Logout").withIconAppColor(TR.drawable.ic_exit_to_app.value)
    )
    .withOnDrawerItemClickListener((_: View, _: Int, item: IDrawerItem[_, _ <: ViewHolder]) => handleDrawerItemClick(item.getIdentifier))
    .build()

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    drawer.setSelection(selectedDrawer.id)

    DrawerImageLoader.init(new AbstractDrawerImageLoader {
      override def set(imageView: ImageView, uri: Uri, placeholder: Drawable): Unit =
        imageView.loadImg(uri.toString, config = _.diskCacheStrategy(DiskCacheStrategy.NONE))
      override def cancel(imageView: ImageView): Unit = Glide.clear(imageView)
    })

    CurrentUserInfo.load(session).map(user => runOnUi {
      profile.withName(user.summoner.name)
      profile.withEmail(user.profile.statusMsg)
      val summIconUrl = s"http://ddragon.leagueoflegends.com/cdn/6.15.1/img/profileicon/${user.summoner.profileIconId}.png"
      profile.withIcon(summIconUrl)
      header.updateProfile(profile)
    })

    LoLChat.run(ops.getAppearance(session)).map {
      case Online  => runOnUi(showOnlineStatus())
      case Away    => runOnUi(showAwayStatus())
      case Offline => runOnUi(showOfflineStatus())
    }
  }

  def handleDrawerItemClick(id: Long): Boolean = {
    val (keepOpen, close) = (true, false)
    val comingSoonDialog = new MaterialDialog.Builder(ctx).title("Coming Soon").positiveText("Ok")

    if (id == selectedDrawer.id) keepOpen // ignore when clicking on an already selected item
    else id match {
      case friendList.id =>
        CurrentUserInfo.load(session).map(user => startActivity(FriendListAct(user.summoner.id)))
        close
      case myProfile.id =>
        comingSoonDialog.show()
        close
      case search.id =>
        comingSoonDialog.show()
        close
      case scouter.id =>
        comingSoonDialog.show()
        close
      case preferences.id =>
        drawer.setSelection(selectedDrawer.id)
        startActivity(SettingAct())
        close
      case ads.id =>
        drawer.setSelection(selectedDrawer.id)
        if (billingProcessor.listOwnedProducts.contains(SKU_REMOVE_ADS)) {
          toast(TR.string.ads_already_disabled)
        } else {
          Prefs.putBoolean("is_ads_enable", true)
          billingProcessor.purchase(this, SKU_REMOVE_ADS)
        }
        keepOpen
      case logout.id =>
        drawer.setSelection(selectedDrawer.id)
        LoLChat.run(ops.logout(session)).map(_ => runOnUi { finish(); startActivity(LoginAct()) })
        keepOpen
      case statusMsg.id =>
        drawer.setSelection(selectedDrawer.id)
        new MaterialDialog.Builder(ctx)
          .title("Set Status Message")
          .positiveText("Update")
          .negativeText("Cancel")
          .onInput("Enter new status message", "", input => {
            LoLChat.run(ops.modifyProfile(_.copy(statusMsg = input))(session)).map { p =>
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
        LoLChat.run(ops.appearOnline(session)).map(_ => runOnUi(showOnlineStatus()))
        drawer.setSelection(selectedDrawer.id)
        keepOpen
      case away.id =>
        LoLChat.run(ops.appearAway(session)).map(_ => runOnUi(showAwayStatus()))
        drawer.setSelection(selectedDrawer.id)
        keepOpen
      case offline.id =>
        LoLChat.run(ops.appearOffline(session)).map(_ => runOnUi(showOfflineStatus()))
        drawer.setSelection(selectedDrawer.id)
        keepOpen
      case _ => keepOpen
    }
  }

  def showOnlineStatus(): Unit = drawer.updateItem(appearanceStatus.withName("Online").withIcon(greenCircle))

  def showAwayStatus(): Unit = drawer.updateItem(appearanceStatus.withName("Away").withIcon(redCircle))

  def showOfflineStatus(): Unit = drawer.updateItem(appearanceStatus.withName("Offline").withIcon(greyCircle))

  def onProductPurchased(productId: String, details: TransactionDetails): Unit = {
    Prefs.putBoolean("is_ads_enable", false)
    toast(TR.string.ads_disabled)
  }

  def onPurchaseHistoryRestored(): Unit = {
    if (billingProcessor.listOwnedProducts.contains(SKU_REMOVE_ADS)) {
      Prefs.putBoolean("is_ads_enable", false)
      toast(TR.string.ads_disabled)
    }
  }

  def onBillingInitialized(): Unit = {}

  def onBillingError(errorCode: Int, error: Throwable): Unit = error.printStackTrace()

  override def onDestroy(): Unit = {
    super.onDestroy()
    billingProcessor.release()
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

  implicit class CustomPrimaryDrawerItem(val primaryDrawerItem: PrimaryDrawerItem) extends AnyVal {
    def withIconAppColor(drawable: Drawable)(implicit ctx: Ctx): PrimaryDrawerItem =
      primaryDrawerItem.withIcon(drawable)
        .withIconColorRes(R.color.nav_drawer_icon)
        .withSelectedIconColorRes(R.color.nav_drawer_icon_selected)
        .withIconTintingEnabled(true)
  }
}
