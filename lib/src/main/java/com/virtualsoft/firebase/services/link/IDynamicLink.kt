package com.virtualsoft.firebase.services.link

import android.content.Intent
import android.net.Uri
import com.google.firebase.dynamiclinks.DynamicLink
import com.virtualsoft.firebase.IFirebase

interface IDynamicLink : IFirebase {

    data class DynamicLinkParams(var androidParameters: DynamicLink.AndroidParameters? = null,
                                 var iosParameters: DynamicLink.IosParameters? = null,
                                 var googleAnalyticsParameters: DynamicLink.GoogleAnalyticsParameters? = null,
                                 var itunesConnectAnalyticsParameters: DynamicLink.ItunesConnectAnalyticsParameters? = null,
                                 var socialMetaTagParameters: DynamicLink.SocialMetaTagParameters? = null)

    fun createLink(link: String, params: DynamicLinkParams? = null): Uri

    suspend fun createShortLink(link: String, params: DynamicLinkParams? = null): Uri?

    suspend fun getLink(uri: Uri): Uri?

    suspend fun getLink(intent: Intent): Uri?
}