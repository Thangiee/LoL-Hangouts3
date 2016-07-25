package com.thangiee.lolhangouts3

import android.support.v7.widget.RecyclerView.Adapter
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.{EditText, ImageView}
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.{DrawableRequestBuilder, DrawableTypeRequest, Glide}
import com.dd.morphingbutton.MorphingButton
import com.dd.morphingbutton.impl.LinearProgressButton
import com.github.florent37.viewanimator.AnimationListener.Update
import com.github.florent37.viewanimator.ViewAnimator
import com.jude.easyrecyclerview.adapter.{BaseViewHolder, RecyclerArrayAdapter}
import lolchat.data.Region

import scala.concurrent.duration.FiniteDuration

object enrichments {

  implicit class RichView[V <: View](val v: V) extends AnyVal {
    def + [A](fn: V => A): V = { fn(v); v }
  }

  implicit class RichImgView(val img: ImageView) extends AnyVal {
    type T = ImageView

    def loadImg(url: String, config: DrawableTypeRequest[String] => DrawableRequestBuilder[String])(implicit ctx: Ctx): T = {
      config(Glide.`with`(ctx).load(url)).into(img)
      img
    }

    def loadImg(url: String)(implicit ctx: Ctx): T = loadImg(url, config = _.centerCrop().crossFade())

    def loadSummIcon(summName: String, region: Region)(implicit ctx: Ctx): T = {
      val fmtName = summName.toLowerCase().replace(" ", "")
      loadImg(
        url = s"http://avatar.leagueoflegends.com/${region.abbr}/$fmtName.png",
        config = _.centerCrop().crossFade()
          .placeholder(TR.drawable.ic_summ_unknown.value)
          .animate(android.R.anim.fade_in)
          .diskCacheStrategy(DiskCacheStrategy.NONE)
      )
    }
  }

  implicit class RichEditText(val img: EditText) extends AnyVal {
    def txt: String = img.getText.toString
  }

  implicit class RichRecyclerArrayAdapter[T](val adapter: RecyclerArrayAdapter[T]) extends AnyVal {
    def parentType: Adapter[BaseViewHolder[T]] = adapter.asInstanceOf[Adapter[BaseViewHolder[T]]]
  }

  implicit class RichToolbar(val tb: Toolbar) extends AnyVal {
    type T = Toolbar

    def withWhiteNavArrow(implicit ctx: Ctx): T = {
      tb.setNavigationIcon(TR.drawable.ic_arrow_back.value)
      tb.setTitleTextColor(TR.color.md_white.value)
      tb
    }
  }

  implicit class RichLinearProgBtn(val btn: LinearProgressButton) extends AnyVal {
    type T = LinearProgressButton

    def morphToNormalBtn(txt: String, colorRes: Int = R.color.accent)(implicit ctx: Ctx): T = {
      btn.morph(
        MorphingButton.Params.create()
          .duration(1)
          .cornerRadius(2.dp)
          .width(100.dp)
          .height(56.dp)
          .color(r2color(colorRes))
          .colorPressed(TR.color.md_orange_700.value) // todo:
          .text(txt)
      )
      btn
    }

    def morphToSuccessBtn(implicit ctx: Ctx): T = {
      btn.morph(
        MorphingButton.Params.create()
          .duration(500)
          .cornerRadius(56.dp)
          .width(56.dp)
          .height(56.dp)
          .color(TR.color.md_light_green_500.value)
          .icon(TR.drawable.ic_done.resid)
      )
      btn
    }

    def morphToErrorBtn(implicit ctx: Ctx): T = {
      btn.morph(
        MorphingButton.Params.create()
          .duration(500)
          .cornerRadius(56.dp)
          .width(56.dp)
          .height(56.dp)
          .color(TR.color.md_red_500.value)
          .icon(TR.drawable.ic_close.resid)
      )
      btn
    }

    def fillProgressBar(startPercent: Int, endPercent: Int, duration: FiniteDuration): T = {
      ViewAnimator.animate(btn)
        .duration(duration.toMillis)
        .custom(new Update[T] {
          def update(view: T, value: Float): Unit = view.setProgress(value.toInt)
        }, startPercent, endPercent)
        .accelerate()
        .start()

      btn
    }
  }

}
