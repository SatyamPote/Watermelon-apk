package com.watermelon.data.repository

import com.watermelon.domain.model.User
import com.watermelon.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : AuthRepository {

    override suspend fun signUp(email: String, password: String): Result<Unit> = runCatching {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        client.auth.signOut()
    }

    override suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        client.auth.resetPasswordForEmail(email = email)
    }

    override fun isAuthenticated(): Flow<Boolean> = flow {
        val session = client.auth.currentSessionOrNull()
        emit(session?.user != null)
    }

    override fun getCurrentUser(): Flow<User?> = flow {
        val info = client.auth.currentUserOrNull()
        emit(
            info?.let {
                User(
                    id = it.id,
                    email = it.email ?: "",
                    displayName = (it.userMetadata?.get("display_name") as? JsonPrimitive)?.content,
                    avatarUrl = (it.userMetadata?.get("avatar_url") as? JsonPrimitive)?.content,
                    createdAt = System.currentTimeMillis()
                )
            }
        )
    }
}
