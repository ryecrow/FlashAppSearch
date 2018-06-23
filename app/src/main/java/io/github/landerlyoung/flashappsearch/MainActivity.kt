package io.github.landerlyoung.flashappsearch

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.github.landerlyoung.flashappsearch.search.repo.AppInfoRepo
import io.github.landerlyoung.flashappsearch.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
        }
        AppInfoRepo.prepareAppInfo()
    }

}
