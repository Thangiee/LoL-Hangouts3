package com.thangiee.lolhangouts3
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.WindowManager

trait Conversions {
  def r2color(id: Int)(implicit ctx: Ctx): Int = ctx.bestAvailable.getResources.getColor(id)
  def r2drawable(id: Int)(implicit ctx: Ctx): Drawable = ctx.bestAvailable.getResources.getDrawable(id)

  implicit class Units[A](v: A)(implicit ctx: Ctx, numeric: Numeric[A]) {
    import Numeric.Implicits.infixNumericOps

    protected def displayMetrics(implicit ctx: Ctx) = {
      val display = ctx.application.getSystemService(Context.WINDOW_SERVICE).asInstanceOf[WindowManager].getDefaultDisplay
      val metrics = new DisplayMetrics
      display.getMetrics(metrics)
      metrics
    }
    /** Density-independent points */
    def dp = (v.toFloat() * displayMetrics.density).toInt
    /** Scale-independent points */
    def sp = (v.toFloat() * displayMetrics.scaledDensity).toInt
  }

}

object Conversions extends Conversions
