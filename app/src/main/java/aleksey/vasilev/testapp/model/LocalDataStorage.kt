package aleksey.vasilev.testapp.model

import aleksey.vasilev.testapp.R
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class LocalDataStorage(context: Context) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context.packageName,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    private val empty = context.getString(R.string.empty)

    @Synchronized
    operator fun <T> set(key: String, value: T) {
        with(sharedPreferences.edit()) {
            when (value) {
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is String -> putString(key, value)
            }
            commit()
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Synchronized
    operator fun <T> get(key: String, clazz: Class<T>): T? {
        return when (clazz) {
            Int::class.java -> sharedPreferences.getInt(key, 0) as T
            Long::class.java -> sharedPreferences.getLong(key, 0L) as T
            String::class.java -> sharedPreferences.getString(key, empty) as T
            else -> null
        }
    }
}