package com.qdesigns.publiceye.ui.home

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.materialdrawer.holder.ColorHolder
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
import com.qdesigns.publiceye.ui.auth.AuthActivity
import com.qdesigns.publiceye.ui.auth.SaveUserDetails
import com.qdesigns.publiceye.ui.home.TransitionRecognition.TransitionRecognition
import com.qdesigns.publiceye.ui.home.TransitionRecognition.TransitionRecognitionUtils
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var mTransitionRecognition: TransitionRecognition
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var headerView: AccountHeaderView
    var user = FirebaseAuth.getInstance().currentUser!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initTransitionRecognition()
        // Handle Toolbar
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
                PrimaryDrawerItem().withName(R.string.drawer_item_edit_profile)
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
                    .withIcon(FontAwesome.Icon.faw_sign_out_alt).withIdentifier(1)
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
                    drawerItem.identifier == 1L -> signOut()

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
        willDeleteTHis.setOnClickListener {
            signOut()
        }
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

    override fun onResume() {
        super.onResume()
        showPreviousTransitions()
    }

    override fun onPause() {
        mTransitionRecognition.stopTracking()
        super.onPause()
    }

    /**
     * INIT TRANSITION RECOGNITION
     */
    fun initTransitionRecognition() {
        mTransitionRecognition = TransitionRecognition()
        mTransitionRecognition.startTracking(this)
    }

    /**
     * Show previous transitions. This is an example to explain how to detect user's activity. To
     * see this activity we have to relaunch the app.
     */
    fun showPreviousTransitions() {
        val sharedPref = getSharedPreferences(
            TransitionRecognitionUtils.SHARED_PREFERENCES_FILE_KEY_TRANSITIONS, Context.MODE_PRIVATE
        )

        var previousTransitions =
            sharedPref.getString(TransitionRecognitionUtils.SHARED_PREFERENCES_KEY_TRANSITIONS, "")

        main_activity_tv.text = previousTransitions
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
