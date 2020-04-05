package com.qdesigns.publiceye.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.auth.AuthUI
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.location.DetectedActivity
import com.google.firebase.auth.FirebaseAuth
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.iconics.withIcon
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.SectionDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.*
import com.mikepenz.materialdrawer.util.addStickyDrawerItems
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import com.qdesigns.publiceye.R
import com.qdesigns.publiceye.services.BackgroundDetectedActivitiesService
import com.qdesigns.publiceye.ui.auth.AuthActivity
import com.qdesigns.publiceye.ui.auth.SaveUserDetails
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private val PROFILE_IMAGE_REQ_CODE = 101

    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var headerView: AccountHeaderView
    var user = FirebaseAuth.getInstance().currentUser!!

    internal lateinit var broadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Handle NavigationDrawer
        setupNavigationDrawer(savedInstanceState)
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == MainActivity.BROADCAST_DETECTED_ACTIVITY) {
                    val type = intent.getIntExtra("type", -1)
                    val confidence = intent.getIntExtra("confidence", 0)
                    handleUserActivity(type, confidence)
                }
            }
        }
        startTracking()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        name_tv.setText(user!!.displayName)

        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.user_placeholder)
            .error(R.drawable.user_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)
            .dontAnimate()
            .dontTransform()

        Glide.with(this)
            .applyDefaultRequestOptions(options)
            .load(user!!.photoUrl)
            .into(profile_pic)

        imagePicker.setOnClickListener {
            pickProfileImage()

        }
    }

    fun pickProfileImage() {
        ImagePicker.with(this)
            // Crop Square image
            .crop()
            .setImageProviderInterceptor { imageProvider -> // Intercept ImageProvider
                Log.d("ImagePicker", "Selected ImageProvider: " + imageProvider.name)
            }
            .compress(1024)
            // Image resolution will be less than 512 x 512
            .maxResultSize(512, 512)
            .start(PROFILE_IMAGE_REQ_CODE)
    }

    private fun handleUserActivity(type: Int, confidence: Int) {
        var label = getString(R.string.activity_unknown)

        when (type) {
            DetectedActivity.IN_VEHICLE -> {
                label = "You are in Vehicle"
            }
            DetectedActivity.ON_BICYCLE -> {
                label = "You are on Bicycle"
            }
            DetectedActivity.ON_FOOT -> {
                label = "You are on Foot"
            }
            DetectedActivity.RUNNING -> {
                label = "You are Running"
            }
            DetectedActivity.STILL -> {
                label = "You are Still"
            }
            DetectedActivity.TILTING -> {
                label = "Your phone is Tilted"
            }
            DetectedActivity.WALKING -> {
                label = "You are Walking"
            }
            DetectedActivity.UNKNOWN -> {
                label = "Unkown Activity"
            }
        }

        Log.e(TAG, "User activity: $label, Confidence: $confidence")

        if (confidence > MainActivity.CONFIDENCE) {
            main_activity_tv?.text = label
            txt_confidence?.text = "Confidence: $confidence"
        }
    }


    private fun setupNavigationDrawer(savedInstanceState: Bundle?) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            root,
            toolbar,
            com.mikepenz.materialdrawer.R.string.material_drawer_open,
            com.mikepenz.materialdrawer.R.string.material_drawer_close
        )

        // Create the AccountHeader
        headerView = AccountHeaderView(this).apply {
            attachToSliderView(slider) // attach to the slider
            addProfiles(
                ProfileDrawerItem().withName(user.displayName).withEmail(user.phoneNumber)
                    .withIcon(user.photoUrl!!)

            )

            headerBackground = ImageHolder(R.drawable.header_color)
            onAccountHeaderListener = { view, profile, current ->
                // react to profile changes
                false
            }
            withSavedInstance(savedInstanceState)
        }
        headerView.selectionListEnabledForSingleProfile = false


        slider.apply {
            itemAdapter.add(
                PrimaryDrawerItem().withName(R.string.drawer_item_home)
                    .withIcon(FontAwesome.Icon.faw_home).withIdentifier(1),
                PrimaryDrawerItem().withName(R.string.drawer_item_edit_profile).withIdentifier(2)
                    .withIcon(FontAwesome.Icon.faw_user_edit),
                PrimaryDrawerItem().withName(R.string.drawer_item_complaints)
                    .withIcon(FontAwesome.Icon.faw_clipboard_list),
                //add some more items to get a scrolling list
                SectionDrawerItem().withName(R.string.drawer_item_section_header),
                SecondaryDrawerItem().withName(R.string.drawer_item_settings)
                    .withIcon(FontAwesome.Icon.faw_cog),
                SecondaryDrawerItem().withName(R.string.drawer_item_help)
                    .withIcon(FontAwesome.Icon.faw_question),
                SecondaryDrawerItem().withName(R.string.drawer_item_open_how_to)
                    .withIcon(FontAwesome.Icon.faw_book_open),
                SecondaryDrawerItem().withName(R.string.drawer_item_contact)
                    .withIcon(FontAwesome.Icon.faw_phone)

            )
            addStickyDrawerItems(
                SecondaryDrawerItem().withName("log out")
                    .withIcon(FontAwesome.Icon.faw_sign_out_alt).withIdentifier(10)
            )
            onDrawerItemClickListener = { v, drawerItem, position ->
                if (drawerItem is Nameable) {
                    Toasty.info(
                        this@MainActivity,
                        drawerItem.name?.getText(this@MainActivity).toString(), Toast.LENGTH_SHORT
                    ).show()
                }
                var intent: Intent? = null
                when {
                    drawerItem.identifier == 2L -> intent =
                        Intent(this@MainActivity, SaveUserDetails::class.java)

                    drawerItem.identifier == 10L -> signOut()

                }
                if (intent != null) {
                    this@MainActivity.startActivity(intent)
                }
                false
            }
            setSavedInstance(savedInstanceState)
        }

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            toolbar.updatePadding(top = insets.systemWindowInsetRight)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        startTracking()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            broadcastReceiver,
            IntentFilter(BROADCAST_DETECTED_ACTIVITY)
        )
    }

    override fun onPause() {
        super.onPause()
        stopTracking()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }


    override fun onDestroy() {
        stopTracking()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        actionBarDrawerToggle.syncState()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(_outState: Bundle) {
        var outState = _outState
        //add the values which need to be saved from the drawer to the bundle
        outState = slider.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (root.isDrawerOpen(slider)) {
            root.closeDrawer(slider)
        } else {
            super.onBackPressed()
        }
    }

    private fun startTracking() {
        val intent = Intent(this@MainActivity, BackgroundDetectedActivitiesService::class.java)
        startService(intent)
    }

    private fun stopTracking() {
        val intent = Intent(this@MainActivity, BackgroundDetectedActivitiesService::class.java)
        stopService(intent)
    }

    companion object {

        val BROADCAST_DETECTED_ACTIVITY = "activity_intent"

        internal val DETECTION_INTERVAL_IN_MILLISECONDS: Long = 7000

        val CONFIDENCE = 70
    }


    fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                startActivity(
                    Intent(this, AuthActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                finish()
            }
    }
}
