package com.picassos.betamax.android.data.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.data.source.remote.APIService
import com.picassos.betamax.android.domain.repository.ChangePasswordRepository
import com.picassos.betamax.android.core.utilities.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChangePasswordRepositoryImpl @Inject constructor(private val service: APIService): ChangePasswordRepository {
    override suspend fun changePassword(token: String, currentPassword: String, newPassword: String): Flow<Resource<Int>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.changePassword(
                    token = token,
                    currentPassword = currentPassword,
                    newPassword = newPassword)
                emit(Resource.Success(response))
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