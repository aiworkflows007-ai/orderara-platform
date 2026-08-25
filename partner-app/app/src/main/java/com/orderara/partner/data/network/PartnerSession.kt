package com.orderara.partner.data.network

import android.content.Context
import android.content.SharedPreferences

/**
 * Remembers which restaurant this phone belongs to.
 *
 * Without this, the app forgets its identity on every restart and falls back to
 * demo data — the reason a freshly registered restaurant used to keep showing
 * Royal Biryani House's menu and orders.
 */
object PartnerSession {

    private const val PREFS = "orderara_partner_session"
    private const val KEY_RESTAURANT_ID = "restaurantId"
    private const val KEY_OWNER_NAME = "ownerName"
    private const val KEY_OWNER_PHONE = "ownerPhone"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    var restaurantId: String?
        get() = prefs.getString(KEY_RESTAURANT_ID, null)
        set(value) = prefs.edit().putString(KEY_RESTAURANT_ID, value).apply()

    var ownerName: String
        get() = prefs.getString(KEY_OWNER_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_OWNER_NAME, value).apply()

    var ownerPhone: String
        get() = prefs.getString(KEY_OWNER_PHONE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_OWNER_PHONE, value).apply()

    val isRegistered: Boolean get() = !restaurantId.isNullOrBlank()

    fun clear() = prefs.edit().clear().apply()
}
