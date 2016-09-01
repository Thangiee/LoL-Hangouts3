package com.thangiee.lolhangouts

import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v7.widget.Toolbar
import android.widget.LinearLayout
import com.thangiee.metadroid.Case

@Case class SettingAct() extends SessionAct{
  type RootView = LinearLayout
  lazy val views: TypedViewHolder.setting_act = TypedViewHolder.setContentView(this, TR.layout.setting_act)
  lazy val toolbar: Toolbar = views.toolbar.rootView

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    getSupportActionBar.setTitle("Settings")
    toolbar.setNavigationOnClickListener(_ => finish())
    getFragmentManager.beginTransaction().replace(R.id.content, new SettingAct.PrefFrag()).commit()
  }
}

object SettingAct {
  class PrefFrag extends PreferenceFragment {
    override def onCreate(savedInstanceState: Bundle): Unit = {
      super.onCreate(savedInstanceState)
      addPreferencesFromResource(R.xml.pref_settings)
    }
  }
}
