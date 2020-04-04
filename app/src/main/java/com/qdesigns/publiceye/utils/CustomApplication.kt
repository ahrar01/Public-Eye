package com.qdesigns.publiceye.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.multidex.MultiDexApplication
import com.bumptech.glide.Glide
import com.mikepenz.iconics.Iconics
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.utils.backgroundColorRes
import com.mikepenz.iconics.utils.sizeDp
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerUtils
import com.qdesigns.publiceye.R

class CustomApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        Iconics.init(this)

        //initialize and create the image loader logic
        /*
        DrawerImageLoader.init(object : AbstractDrawerImageLoader() {
            override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable) {
                Picasso.get().load(uri).placeholder(placeholder).into(imageView)
            }
            override fun cancel(imageView: ImageView) {
                Picasso.get().cancelRequest(imageView)
            }
        })
        */

        //initialize and create the image loader logic
        DrawerImageLoader.init(object : AbstractDrawerImageLoader() {
            override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable, tag: String?) {
                Glide.with(imageView.context).load(uri).placeholder(placeholder).into(imageView)
            }

            override fun cancel(imageView: ImageView) {
                Glide.with(imageView.context).clear(imageView)
            }

            override fun placeholder(ctx: Context, tag: String?): Drawable {
                //define different placeholders for different imageView targets
                //default tags are accessible via the DrawerImageLoader.Tags
                //custom ones can be checked via string. see the CustomUrlBasePrimaryDrawerItem LINE 111
                return when (tag) {
                    DrawerImageLoader.Tags.PROFILE.name -> DrawerUtils.getPlaceHolder(ctx)
                    DrawerImageLoader.Tags.ACCOUNT_HEADER.name -> IconicsDrawable(
                        ctx,
                        " "
                    ).apply { backgroundColorRes = R.color.colorPrimaryDark; sizeDp = 56 }
                    "customUrlItem" -> IconicsDrawable(ctx, " ").apply {
                        backgroundColorRes = R.color.md_red_500; sizeDp = 100
                    }
                    //we use the default one for
                    //DrawerImageLoader.Tags.PROFILE_DRAWER_ITEM.name()
                    else -> super.placeholder(ctx, tag)
                }
            }
        })
    }
}
