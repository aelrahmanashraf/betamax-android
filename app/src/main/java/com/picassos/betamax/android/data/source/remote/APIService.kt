package com.picassos.betamax.android.data.source.remote

import com.picassos.betamax.android.BuildConfig
import com.picassos.betamax.android.core.utilities.ConnectivityInterceptor
import com.picassos.betamax.android.data.source.remote.dto.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface APIService {
    @FormUrlEncoded
    @POST("auth/signin/request_signin.inc.php")
    suspend fun signin(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("imei") imei: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): AccountDto

    @FormUrlEncoded
    @POST("auth/account/request_account.inc.php")
    suspend fun account(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String,
        @Field("imei") imei: String
    ): AccountDto

    @FormUrlEncoded
    @POST("auth/signout/request_signout.inc.php")
    suspend fun signout(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String,
        @Field("imei") imei: String
    ): Int

    @FormUrlEncoded
    @POST("configuration/request_configuration.inc.php")
    suspend fun configuration(
        @Field("secret_api_key") sak: String = SECRET_API_KEY
    ): ConfigurationDto

    @FormUrlEncoded
    @POST("auth/reset_password/request_send_email.inc.php")
    suspend fun sendResetEmail(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("email") email: String
    ): Int

    @FormUrlEncoded
    @POST("auth/reset_password/request_verify_code.inc.php")
    suspend fun verifyResetPasswordCode(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("email") email: String,
        @Field("verification_code") verificationCode: Int
    ): AccountDto

    @FormUrlEncoded
    @POST("auth/reset_password/request_reset_password.inc.php")
    suspend fun resetPassword(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("confirm_password") confirmPassword: String
    ): Int

    @FormUrlEncoded
    @POST("auth/account_settings/video_preference/request_video_quality.inc.php")
    suspend fun videoQuality(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String
    ): Int

    @FormUrlEncoded
    @POST("auth/account_settings/video_preference/request_update_video_quality.inc.php")
    suspend fun updateVideoQuality(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String,
        @Field("quality") quality: Int
    ): Int

    @FormUrlEncoded
    @POST("auth/register/request_register.inc.php")
    suspend fun register(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("imei") imei: String,
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): AccountDto

    @FormUrlEncoded
    @POST("auth/account_settings/login_info/password/request_change_password.inc.php")
    suspend fun changePassword(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String,
        @Field("current_password") currentPassword: String,
        @Field("new_password") newPassword: String
    ): Int

    @FormUrlEncoded
    @POST("genre/request_special_genres.inc.php")
    suspend fun specialGenres(
        @Field("secret_api_key") sak: String = SECRET_API_KEY
    ): GenresDto

    @FormUrlEncoded
    @POST("genre/request_tv_genres.inc.php")
    suspend fun tvGenres(
        @Field("secret_api_key") sak: String = SECRET_API_KEY
    ): GenresDto

    @FormUrlEncoded
    @POST("movie_cast/request_movie_cast.inc.php")
    suspend fun cast(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("movie_id") movieId: Int
    ): CastDto

    @FormUrlEncoded
    @POST("continue_watching/request_continue_watching.inc.php")
    suspend fun continueWatching(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String
    ): ContinueWatchingDto

    @FormUrlEncoded
    @POST("movie/request_featured_movies.inc.php")
    suspend fun featuredMovies(
        @Field("secret_api_key") sak: String = SECRET_API_KEY
    ): MoviesDto

    @FormUrlEncoded
    @POST("movie/request_home_trending_movies.inc.php")
    suspend fun homeTrendingMovies(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String
    ): MoviesDto

    @FormUrlEncoded
    @POST("movie/request_trending_movies.inc.php")
    suspend fun trendingMovies(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String,
        @Field("filter") filter: String = "all"
    ): MoviesDto

    @FormUrlEncoded
    @POST("continue_watching/request_update_continue_watching.inc.php")
    suspend fun updateContinueWatching(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String,
        @Field("content_id") contentId: Int,
        @Field("title") title: String,
        @Field("url") url: String,
        @Field("thumbnail") thumbnail: String,
        @Field("duration") duration: Int,
        @Field("current_position") currentPosition: Int,
        @Field("series") series: Int
    ): Int

    @FormUrlEncoded
    @POST("continue_watching/request_delete_continue_watching.inc.php")
    suspend fun deleteContinueWatching(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String,
        @Field("content_id") contentId: Int
    ): Int

    @FormUrlEncoded
    @POST("season/request_seasons.inc.php")
    suspend fun seasons(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("movie_id") movieId: Int
    ): SeasonsDto

    @FormUrlEncoded
    @POST("episode/request_episodes.inc.php")
    suspend fun episodes(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String,
        @Field("movie_id") movieId: Int,
        @Field("season_level") seasonLevel: Int
    ): EpisodesDto

    @FormUrlEncoded
    @POST("genre/request_all_genres.inc.php")
    suspend fun allGenres(
        @Field("secret_api_key") sak: String = SECRET_API_KEY
    ): GenresDto

    @FormUrlEncoded
    @POST("movie/request_save_movie.inc.php")
    suspend fun saveMovie(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String,
        @Field("movie_id") movieId: Int
    ): String

    @FormUrlEncoded
    @POST("genre/request_home_genres.inc.php")
    suspend fun homeGenres(
        @Field("secret_api_key") sak: String = SECRET_API_KEY
    ): GenresDto

    @FormUrlEncoded
    @POST("movie/request_movies.inc.php")
    suspend fun movies(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String
    ): MoviesDto

    @FormUrlEncoded
    @POST("movie/request_series.inc.php")
    suspend fun series(
        @Field("secret_api_key") sak: String = SECRET_API_KEY
    ): MoviesDto

    @FormUrlEncoded
    @POST("movie/request_newly_release_movies.inc.php")
    suspend fun newlyReleaseMovies(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String,
        @Field("filter") filter: String = "all"
    ): MoviesDto

    @FormUrlEncoded
    @POST("movie/request_home_movies.inc.php")
    suspend fun homeMovies(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String
    ): MoviesDto

    @FormUrlEncoded
    @POST("movie/request_home_series.inc.php")
    suspend fun homeSeries(
        @Field("secret_api_key") sak: String = SECRET_API_KEY
    ): MoviesDto

    @FormUrlEncoded
    @POST("movie/request_related_movies.inc.php")
    suspend fun relatedMovies(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String,
        @Field("movie_id") movieId: Int
    ): MoviesDto

    @FormUrlEncoded
    @POST("subscription/request_update_subscription.inc.php")
    suspend fun updateSubscription(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String,
        @Field("subscription_package") subscriptionPackage: Int
    ): Int

    @FormUrlEncoded
    @POST("subscription/request_check_subscription.inc.php")
    suspend fun checkSubscription(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String
    ): SubscriptionDto

    @FormUrlEncoded
    @POST("movie/request_saved_movies.inc.php")
    suspend fun savedMovies(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String
    ): MoviesDto

    @FormUrlEncoded
    @POST("tv_channel/request_saved_tv_channels.inc.php")
    suspend fun savedTvChannels(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String
    ): TvChannelsDto

    @FormUrlEncoded
    @POST("tv_channel/request_save_tv_channel.inc.php")
    suspend fun saveTvChannel(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String,
        @Field("tvchannel_id") tvChannelId: Int
    ): String

    @FormUrlEncoded
    @POST("movie/request_home_saved_movies.inc.php")
    suspend fun homeSavedMovies(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String
    ): MoviesDto

    @FormUrlEncoded
    @POST("tv_channel/request_tv_channels.inc.php")
    suspend fun tvChannels(
        @Field("secret_api_key") sak: String = SECRET_API_KEY
    ): TvChannelsDto

    @FormUrlEncoded
    @POST("tv_channel/request_tv_channel.inc.php")
    suspend fun tvChannel(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("tv_channel_id") tvChannelId: Int
    ): TvChannelsDto

    @FormUrlEncoded
    @POST("tv_channel/request_tv_channels_by_genre.inc.php")
    suspend fun tvChannelsByGenre(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("genre_id") genreId: Int
    ): TvChannelsDto

    @FormUrlEncoded
    @POST("movie/request_search_movies.inc.php")
    suspend fun searchMovies(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("query") query: String,
        @Field("filter") filter: String
    ): MoviesDto

    @FormUrlEncoded
    @POST("auth/account_settings/profile_info/request_update_profile_info.inc.php")
    suspend fun updateProfileInfo(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String,
        @Field("username") username: String
    ): Int

    @FormUrlEncoded
    @POST("movie/request_movies_by_genre.inc.php")
    suspend fun moviesByGenre(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String,
        @Field("genre_id") genreId: Int,
        @Field("filter") filter: String = "all"
    ): MoviesDto

    @FormUrlEncoded
    @POST("genre/request_genre.inc.php")
    suspend fun genre(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("genre_id") genreId: Int
    ): GenreDto

    @FormUrlEncoded
    @POST("movie/request_movie.inc.php")
    suspend fun movie(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("movie_id") movieId: Int
    ): MoviesDto

    @FormUrlEncoded
    @POST("movie/request_check_movie_saved.inc.php")
    suspend fun checkMovieSaved(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String,
        @Field("movie_id") movieId: Int
    ): Int

    @FormUrlEncoded
    @POST("tv_channel/request_check_tv_channel_saved.inc.php")
    suspend fun checkTvChannelSaved(
        @Field("secret_api_key") sak: String = SECRET_API_KEY,
        @Field("token") token: String,
        @Field("tvchannel_id") tvChannelId: Int
    ): Int

    companion object {
        private const val BASE_URL = BuildConfig.BASE_URL
        private const val SECRET_API_KEY = BuildConfig.SECRET_API_KEY

        private val okHttpClient by lazy {
            OkHttpClient.Builder()
                .addInterceptor(ConnectivityInterceptor())
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        }

        fun create(): APIService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(APIService::class.java)
        }
    }
}
