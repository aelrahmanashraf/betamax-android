package com.picassos.betamax.android.presentation.app.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.gson.Gson
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleStarted
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.view.bottom_navigation.BottomNavigationViewBehavior
import com.picassos.betamax.android.data.source.local.shared_preferences.SharedPreferences
import com.picassos.betamax.android.databinding.ActivityMainBinding
import com.picassos.betamax.android.di.AppEntryPoint
import com.picassos.betamax.android.domain.model.Subscription
import com.picassos.betamax.android.presentation.app.home.HomeFragment
import com.picassos.betamax.android.presentation.app.movie.movies.MoviesFragment
import com.picassos.betamax.android.presentation.app.series.SeriesFragment
import com.picassos.betamax.android.presentation.app.search.SearchFragment
import com.picassos.betamax.android.presentation.app.subscription.subscribe.SubscribeActivity
import com.picassos.betamax.android.presentation.app.tvchannel.TvFragment
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@AndroidEntryPoint
@DelicateCoroutinesApi
class MainActivity : AppCompatActivity() {
    private lateinit var layout: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()

    private val homeFragment: Fragment = HomeFragment()
    private val moviesFragment: Fragment = MoviesFragment()
    private val tvFragment: Fragment = TvFragment()
    private val seriesFragment: Fragment = SeriesFragment()
    private val searchFragment: Fragment = SearchFragment()

    private var active = homeFragment

    @SuppressLint("NonConstantResourceId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val entryPoint = EntryPointAccessors.fromApplication(this, AppEntryPoint::class.java)
        val sharedPreferences = SharedPreferences(this@MainActivity)

        Helper.darkMode(this)

        layout = DataBindingUtil.setContentView(this, R.layout.activity_main)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                Helper.restrictVpn(this@MainActivity)
                entryPoint.getConfigurationUseCase().invoke().collectLatest { configuration ->
                    if (!Helper.verifyLicense(configuration.developedBy)) {
                        finishAffinity()
                        exitProcess(0)
                    }
                }
            }
        }

        val fragmentManager = supportFragmentManager.apply {
            beginTransaction().add(R.id.fragment_container, searchFragment, "5").hide(searchFragment).commit()
            beginTransaction().add(R.id.fragment_container, seriesFragment, "4").hide(seriesFragment).commit()
            beginTransaction().add(R.id.fragment_container, tvFragment, "3").hide(tvFragment).commit()
            beginTransaction().add(R.id.fragment_container, moviesFragment, "2").hide(moviesFragment).commit()
            beginTransaction().add(R.id.fragment_container, homeFragment, "1").commit()
        }

        layout.bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.home -> {
                    fragmentManager.beginTransaction().hide(active).show(homeFragment).commit()
                    active = homeFragment
                    return@setOnItemSelectedListener true
                }
                R.id.movies -> {
                    fragmentManager.beginTransaction().hide(active).show(moviesFragment).commit()
                    active = moviesFragment
                    return@setOnItemSelectedListener true
                }
                R.id.tv -> {
                    fragmentManager.beginTransaction().hide(active).show(tvFragment).commit()
                    active = tvFragment
                    return@setOnItemSelectedListener true
                }
                R.id.series -> {
                    fragmentManager.beginTransaction().hide(active).show(seriesFragment).commit()
                    active = seriesFragment
                    return@setOnItemSelectedListener true
                }
                R.id.search -> {
                    fragmentManager.beginTransaction().hide(active).show(searchFragment).commit()
                    active = searchFragment
                    return@setOnItemSelectedListener true
                }
            }
            false
        }

        layout.bottomNavigation.setOnItemReselectedListener { obj: MenuItem -> obj.itemId }

        val layoutParams = layout.bottomNavigation.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.behavior = BottomNavigationViewBehavior()

        lifecycleScope.launch {
            mainViewModel.requestCheckSubscription()
        }
        collectLatestOnLifecycleStarted(mainViewModel.checkSubscription) { state ->
            if (state.response != null) {
                lifecycleScope.launch {
                    val subscription = state.response
                    entryPoint.setSubscriptionUseCase().invoke(Gson().toJson(Subscription(
                        subscriptionPackage = subscription.subscriptionPackage,
                        subscriptionEnd = subscription.subscriptionEnd,
                        daysLeft = subscription.daysLeft)))

                    Log.d("subscriptionState", subscription.daysLeft.toString())

                    if (subscription.subscriptionPackage == 0) {
                        delay(2000L)
                        if (!sharedPreferences.loadSubscription()) {
                            startActivity(Intent(this@MainActivity, SubscribeActivity::class.java))
                            sharedPreferences.setSubscription(true)
                        }
                    }
                }
            }
            if (state.error != null) {
                Log.d("subscriptionState", state.error.toString())
            }
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (active !== homeFragment) {
                    layout.bottomNavigation.selectedItemId = R.id.home
                    fragmentManager.beginTransaction().hide(active).show(homeFragment).commit()
                    active = homeFragment
                } else {
                    finishAffinity()
                    exitProcess(0)
                }
            }
        })
    }
}