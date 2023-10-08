package com.picassos.betamax.android.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ConfigurationDto(
    @SerializedName("app")
    val app: App) {

    data class App(
        @SerializedName("configuration")
        val configuration: Configuration,
        @SerializedName("app_build")
        val appBuild: AppBuild,
        @SerializedName("response")
        val responseCode: ResponseCode) {

        data class Configuration(
            @SerializedName("email")
            val email: String,
            @SerializedName("privacy_url")
            val privacyURL: String,
            @SerializedName("help_url")
            val helpURL: String,
            @SerializedName("telegram_url")
            val telegramURL: String,
            @SerializedName("whatsapp_url")
            val whatsappURL: String,
            @SerializedName("about_text")
            val aboutText: String,
            @SerializedName("banner_image")
            val bannerImage: String,
            @SerializedName("silver_package_price")
            val silverPackagePrice: String,
            @SerializedName("gold_package_price")
            val goldPackagePrice: String,
            @SerializedName("diamond_package_price")
            val diamondPackagePrice: String): Serializable

        data class AppBuild(
            @SerializedName("developed_by")
            val developedBy: String)

        data class ResponseCode(
            @SerializedName("code")
            val code: Int)
    }
}
