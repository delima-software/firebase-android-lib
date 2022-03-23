package com.virtualsoft.firebase.services.link

import android.content.Intent
import android.net.Uri
import com.google.firebase.dynamiclinks.ktx.dynamicLink
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import com.virtualsoft.core.designpatterns.builder.IBuilder
import com.virtualsoft.firebase.utils.LogUtils
import kotlinx.coroutines.tasks.await

class DynamicLink : IDynamicLink {

    data class Properties(var domainUriPrefix: String? = null)

    override val id = DynamicLink::class.java.name

    private var dynamicLinkProperties: Properties? = null

    class Builder : IBuilder<IDynamicLink> {

        override val building = DynamicLink()

        fun setDynamicLinkProperties(dynamicLinkProperties: Properties?): Builder {
            building.dynamicLinkProperties = dynamicLinkProperties
            return this
        }
    }

    override fun createLink(link: String, params: IDynamicLink.DynamicLinkParams?): Uri {
        val dynamicLink = Firebase.dynamicLinks.dynamicLink {
            this.link = Uri.parse(link)
            dynamicLinkProperties?.domainUriPrefix?.let { uriPrefix ->
                this.domainUriPrefix = uriPrefix
            }
            params?.androidParameters?.let { androidParameters ->
                this.setAndroidParameters(androidParameters)
            }
            params?.iosParameters?.let { iosParameters ->
                this.setIosParameters(iosParameters)
            }
            params?.googleAnalyticsParameters?.let { googleAnalyticsParameters ->
                this.setGoogleAnalyticsParameters(googleAnalyticsParameters)
            }
            params?.itunesConnectAnalyticsParameters?.let { itunesConnectAnalyticsParameters ->
                this.setItunesConnectAnalyticsParameters(itunesConnectAnalyticsParameters)
            }
            params?.socialMetaTagParameters?.let { socialMetaTagParameters ->
                this.setSocialMetaTagParameters(socialMetaTagParameters)
            }
        }
        return dynamicLink.uri
    }

    override suspend fun createShortLink(link: String, params: IDynamicLink.DynamicLinkParams?): Uri? {
        return try {
            val dynamicLink = Firebase.dynamicLinks.shortLinkAsync {
                this.link = Uri.parse(link)
                dynamicLinkProperties?.domainUriPrefix?.let { uriPrefix ->
                    this.domainUriPrefix = uriPrefix
                }
                params?.androidParameters?.let { androidParameters ->
                    this.setAndroidParameters(androidParameters)
                }
                params?.iosParameters?.let { iosParameters ->
                    this.setIosParameters(iosParameters)
                }
                params?.googleAnalyticsParameters?.let { googleAnalyticsParameters ->
                    this.setGoogleAnalyticsParameters(googleAnalyticsParameters)
                }
                params?.itunesConnectAnalyticsParameters?.let { itunesConnectAnalyticsParameters ->
                    this.setItunesConnectAnalyticsParameters(itunesConnectAnalyticsParameters)
                }
                params?.socialMetaTagParameters?.let { socialMetaTagParameters ->
                    this.setSocialMetaTagParameters(socialMetaTagParameters)
                }
            }.await()
            dynamicLink.shortLink
        }
        catch (e: Exception) {
            LogUtils.logError("CREATE_LINK", "could not create short link", e)
            null
        }

    }

    override suspend fun getLink(uri: Uri): Uri? {
        return try {
            var deepLink: Uri? = null
            Firebase.dynamicLinks.getDynamicLink(uri).await()?.let { pendingDynamicLinkData ->
                deepLink = pendingDynamicLinkData.link
            }
            deepLink
        }
        catch (e: Exception) {
            LogUtils.logError("GET_LINK", "could not get link", e)
            null
        }
    }

    override suspend fun getLink(intent: Intent): Uri? {
        return try {
            var deepLink: Uri? = null
            Firebase.dynamicLinks.getDynamicLink(intent).await()?.let { pendingDynamicLinkData ->
                deepLink = pendingDynamicLinkData.link
            }
            deepLink
        }
        catch (e: Exception) {
            LogUtils.logError("GET_LINK", "could not get link", e)
            null
        }
    }
}