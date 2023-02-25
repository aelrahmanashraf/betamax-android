package com.picassos.betamax.android.data.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.data.mapper.toAccount
import com.picassos.betamax.android.data.mapper.toConfiguration
import com.picassos.betamax.android.data.source.remote.APIService
import com.picassos.betamax.android.domain.model.Launch
import com.picassos.betamax.android.domain.repository.LaunchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LaunchRepositoryImpl @Inject constructor(private val service: APIService): LaunchRepository {
    override suspend fun launch(token: String, imei: String): Flow<Resource<Launch>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                coroutineScope {
                    val configuration = async(Dispatchers.IO) {
                        service.configuration()
                    }
                    val account = async(Dispatchers.IO) {
                        service.account(
                            token = token,
                            imei = imei)
                    }
                    emit(Resource.Success(Launch(
                        configuration.await().toConfiguration(),
                        account.await().toAccount())))
                }
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }
}