package com.thangiee.lolhangouts3
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.WindowManager

trait Conversions {
  def r2color(id: Int)(implicit ctx: Ctx): Int = ctx.getResources.getColor(id)
  def r2drawable(id: Int)(implicit ctx: Ctx): Drawable = ctx.getResources.getDrawable(id)
  def toBitmap(drawableId: Int)(implicit ctx: Ctx) = BitmapFactory.decodeResource(ctx.getResources, drawableId)

  implicit class Units[A](v: A)(implicit ctx: Ctx, numeric: Numeric[A]) {
    import Numeric.Implicits.infixNumericOps

    protected def displayMetrics(implicit ctx: Ctx) = {
      val display = ctx.getSystemService(Context.WINDOW_SERVICE).asInstanceOf[WindowManager].getDefaultDisplay
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
