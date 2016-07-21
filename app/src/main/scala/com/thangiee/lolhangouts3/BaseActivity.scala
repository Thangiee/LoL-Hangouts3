package com.thangiee.lolhangouts3

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import enrichments._

trait BaseActivity extends AppCompatActivity with TypedFindView {
  implicit val ctx: Ctx = this
  type RootView <: View
  def views: TypedViewHolder[RootView]
  def toolbar: Toolbar

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    views
    setSupportActionBar(toolbar.withWhiteNavArrow)
  }
}
