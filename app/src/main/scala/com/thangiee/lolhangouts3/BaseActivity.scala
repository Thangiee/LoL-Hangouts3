package com.thangiee.lolhangouts3

import android.support.v7.app.AppCompatActivity

trait BaseActivity extends AppCompatActivity with TypedFindView {
  implicit val ctx: Ctx = this
}
