package com.thangiee.lolhangouts3

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.ViewGroup
import android.widget._
import com.hanhuy.android.appcompat.extensions._
import com.jude.easyrecyclerview.adapter.{BaseViewHolder, RecyclerArrayAdapter}
import com.thangiee.lolhangouts3.TypedViewHolder.region_act
import enrichments._
import lolchat.data.Region
import boopickle.Default._

import scala.collection.JavaConversions._
import scala.language.postfixOps

class RegionSelectionAct extends BaseActivity {
  type RootView = LinearLayout
  lazy val views: region_act = TypedViewHolder.setContentView(this, TR.layout.region_act)
  lazy val toolbar = views.toolbar.rootView

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    getSupportActionBar.setTitle("Region")
    toolbar.navigationOnClick0(finish())

    val adapter = RegionItem.adapter
    adapter.setOnItemClickListener(index => onRegionItemClick(RegionItem.all(index)))

    views.recyclerView.setLayoutManager(new LinearLayoutManager(this))
    views.recyclerView.setAdapter(adapter.parentType)

  }

  def onRegionItemClick(region: RegionItem): Unit = {
    RegionItem.cache(region)
    startActivity(LoginAct())
  }

}

case class RegionItem(region: Region, name: String, iconId: Int)

object RegionItem {
  import AuxFunctions._

  val key = "RO291i43bN"

  def loadSelected: Option[Item] = prefsGet[RegionItem](RegionItem.key)

  def cache(region: RegionItem) = prefsPut(RegionItem.key, region)

  lazy val all = Vector(
    RegionItem(Region.NA, "North America", TR.drawable.ic_na.resid),
    RegionItem(Region.BR, "Brazil", TR.drawable.ic_br.resid),
    RegionItem(Region.EUNE, "Europe Nordic and East", TR.drawable.ic_eune.resid),
    RegionItem(Region.EUW, "Europe West", TR.drawable.ic_euw.resid),
    RegionItem(Region.KR, "Korea", TR.drawable.ic_south_korea.resid),
    RegionItem(Region.LAN, "Latin America North", TR.drawable.ic_latamn.resid),
    RegionItem(Region.LAS, "Latin America South", TR.drawable.ic_latams.resid),
    RegionItem(Region.OCE, "Oceania", TR.drawable.ic_oce.resid),
    RegionItem(Region.RU, "Russia", TR.drawable.ic_ru.resid),
    RegionItem(Region.TR, "Turkey", TR.drawable.ic_tr.resid)
  )

  private type Item = RegionItem

  def viewHolder(viewGroup: ViewGroup)(implicit ctx: Ctx): BaseViewHolder[Item] =
    new BaseViewHolder[Item](viewGroup, TR.layout.region_line_item.id) {
      val nameTV  = $[TextView](R.id.regionName)
      val iconImg = $[ImageView](R.id.regionIcon)

      override def setData(data: Item): Unit = {
        nameTV.setText(data.name)
        iconImg.setImageDrawable(r2drawable(data.iconId))
      }
    }

  def adapter(implicit ctx: Ctx) =
    new RecyclerArrayAdapter[Item](ctx, all) {
      def OnCreateViewHolder(viewGroup: ViewGroup, i: Int): BaseViewHolder[Item] = viewHolder(viewGroup)
    }
}
