package com.thangiee.lolhangouts3

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.ViewGroup
import android.widget._
import com.hanhuy.android.appcompat.extensions._
import com.jude.easyrecyclerview.adapter.{BaseViewHolder, RecyclerArrayAdapter}
import com.thangiee.lolhangouts3.free.CanStore
import lolchat.model.Region
import enrichments._

import scala.collection.JavaConversions._
import scala.language.postfixOps

class RegionSelectionAct extends BaseActivity {

  lazy val views: TypedViewHolder.region_act = TypedViewHolder.setContentView(this, TR.layout.region_act)
  lazy val toolbar = views.toolbar.rootView

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    PrefStore.run(KVStoreOps.get[RegionItem](RegionItem.key)).fold(println("EMPTY"))(println)
    setSupportActionBar(toolbar.withWhiteNavArrow)
    getSupportActionBar.setTitle("Region")
    toolbar.navigationOnClick0(finish())

    val adapter = RegionItem.adapter
    adapter.setOnItemClickListener(index => onRegionItemClick(RegionItem.all(index)))

    views.recyclerView.setLayoutManager(new LinearLayoutManager(this))
    views.recyclerView.setAdapter(adapter.parentType)

  }

  def expensiveOp(): Double = new util.Random().nextDouble()

  def onRegionItemClick(region: RegionItem): Unit = {
    PrefStore.run(KVStoreOps.put(RegionItem.key, region))
    startActivity(LoginAct())
  }

}

case class RegionItem(region: Region, name: String, abbr: String, iconId: Int)

object RegionItem {

  val key = "RO291i43bN"

  import upickle.default._
  implicit val pkl = upickle.default.macroRW[RegionItem]
  implicit val canStore: CanStore[Item] = new CanStore[RegionItem] {
    def storeFmt(value: Item): String = write(value)
    def fetchFmt(string: String): Item = read[Item](string)
  }

  lazy val all = Vector(
    RegionItem(Region.NA, "North America", "na", TR.drawable.ic_na.resid),
    RegionItem(Region.BR, "Brazil", "br", TR.drawable.ic_br.resid),
    RegionItem(Region.EUNE, "Europe Nordic and East", "eune", TR.drawable.ic_eune.resid),
    RegionItem(Region.EUW, "Europe West", "euw", TR.drawable.ic_euw.resid),
    RegionItem(Region.KR, "Korea", "kr", TR.drawable.ic_south_korea.resid),
    RegionItem(Region.LAN, "Latin America North", "lan", TR.drawable.ic_latamn.resid),
    RegionItem(Region.LAS, "Latin America South", "las", TR.drawable.ic_latams.resid),
    RegionItem(Region.OCE, "Oceania", "oce", TR.drawable.ic_oce.resid),
    RegionItem(Region.RU, "Russia", "ru", TR.drawable.ic_ru.resid),
    RegionItem(Region.TR, "Turkey", "tr", TR.drawable.ic_tr.resid)
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
    new RecyclerArrayAdapter[Item](ctx.bestAvailable, all) {
      def OnCreateViewHolder(viewGroup: ViewGroup, i: Int): BaseViewHolder[Item] = viewHolder(viewGroup)
    }
}
