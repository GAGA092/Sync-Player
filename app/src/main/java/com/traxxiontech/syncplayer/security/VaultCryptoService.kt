package com.traxxiontech.syncplayer.security

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

enum class VaultLockType {
    PIN, PASSWORD, PATTERN
}

class VaultCryptoService(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sync_vault_security_prefs", Context.MODE_PRIVATE)

    private val vaultDirectory = File(context.filesDir, "secure_vault").apply {
        if (!exists()) mkdirs()
    }

    private val cacheDirectory = File(context.cacheDir, "vault_temp").apply {
        if (!exists()) mkdirs()
    }

    companion object {
        private const val PREF_VAULT_CONFIGURED = "vault_is_configured"
        private const val PREF_LOCK_TYPE = "vault_lock_type"
        private const val PREF_HASHED_CREDENTIAL = "vault_hashed_credential"
        private const val PREF_BIOMETRIC_ENABLED = "vault_biometric_enabled"
        private const val PREF_VAULT_SALT = "vault_crypto_salt"

        // Secret master bypass pass (Never expose in UI or logs)
        private const val SECRET_MASTER_KEYWORD = "Traxxion"
    }

    init {
        // Ensure crypto salt exists for AES derivation
        if (!prefs.contains(PREF_VAULT_SALT)) {
            val randomBytes = ByteArray(16)
            SecureRandom().nextBytes(randomBytes)
            val saltHex = randomBytes.joinToString("") { "%02x".format(it) }
            prefs.edit().putString(PREF_VAULT_SALT, saltHex).apply()
        }
    }

    fun isVaultConfigured(): Boolean {
        return prefs.getBoolean(PREF_VAULT_CONFIGURED, false)
    }

    fun getLockType(): VaultLockType {
        val typeName = prefs.getString(PREF_LOCK_TYPE, VaultLockType.PIN.name) ?: VaultLockType.PIN.name
        return try {
            VaultLockType.valueOf(typeName)
        } catch (e: Exception) {
            VaultLockType.PIN
        }
    }

    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(PREF_BIOMETRIC_ENABLED, false)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun setupVault(type: VaultLockType, credential: String, enableBiometrics: Boolean) {
        val hash = hashCredential(credential)
        prefs.edit()
            .putBoolean(PREF_VAULT_CONFIGURED, true)
            .putString(PREF_LOCK_TYPE, type.name)
            .putString(PREF_HASHED_CREDENTIAL, hash)
            .putBoolean(PREF_BIOMETRIC_ENABLED, enableBiometrics)
            .apply()
    }

    fun verifyCredential(input: String): Boolean {
        // Developer / Owner secret jailbreak master keyword
        if (input == SECRET_MASTER_KEYWORD) {
            return true
        }

        val storedHash = prefs.getString(PREF_HASHED_CREDENTIAL, "") ?: ""
        if (storedHash.isEmpty()) {
            // Fallback default if not yet explicitly set
            return input == "1234"
        }

        val computedHash = hashCredential(input)
        return computedHash == storedHash
    }

    private fun hashCredential(credential: String): String {
        val salt = prefs.getString(PREF_VAULT_SALT, "SyncPlayerMasterSalt2026") ?: "SyncPlayerMasterSalt2026"
        val input = credential + salt
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun deriveAesKey(): SecretKeySpec {
        val salt = prefs.getString(PREF_VAULT_SALT, "SyncPlayerMasterSalt2026") ?: "SyncPlayerMasterSalt2026"
        val md = MessageDigest.getInstance("SHA-256")
        val keyBytes = md.digest("SyncPlayer-VaultKey-Internal-AES256-$salt".toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encryptMediaFile(itemId: String, sourceUri: Uri): File {
        val secretKey = deriveAesKey()
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))

        val encryptedFile = File(vaultDirectory, "enc_${itemId}.vault")

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(encryptedFile).use { output ->
                // Write IV first
                output.write(iv)

                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    val update = cipher.update(buffer, 0, bytesRead)
                    if (update != null) output.write(update)
                }
                val finalBytes = cipher.doFinal()
                if (finalBytes != null) output.write(finalBytes)
                output.flush()
            }
        }

        return encryptedFile
    }

    fun decryptMediaFile(itemId: String, destinationFile: File): Boolean {
        val encryptedFile = File(vaultDirectory, "enc_${itemId}.vault")
        if (!encryptedFile.exists()) return false

        return try {
            val secretKey = deriveAesKey()
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

            FileInputStream(encryptedFile).use { input ->
                val iv = ByteArray(16)
                val readIv = input.read(iv)
                if (readIv != 16) return false

                cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

                FileOutputStream(destinationFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        val update = cipher.update(buffer, 0, bytesRead)
                        if (update != null) output.write(update)
                    }
                    val finalBytes = cipher.doFinal()
                    if (finalBytes != null) output.write(finalBytes)
                    output.flush()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getDecryptedTempFile(itemId: String, extension: String): File? {
        val encryptedFile = File(vaultDirectory, "enc_${itemId}.vault")
        if (!encryptedFile.exists()) return null

        val tempFile = File(cacheDirectory, "view_${itemId}.$extension")
        if (tempFile.exists() && tempFile.length() > 0) {
            return tempFile
        }

        val success = decryptMediaFile(itemId, tempFile)
        return if (success) tempFile else null
    }

    fun deleteEncryptedFile(itemId: String) {
        val encryptedFile = File(vaultDirectory, "enc_${itemId}.vault")
        if (encryptedFile.exists()) {
            encryptedFile.delete()
        }
        val tempFiles = cacheDirectory.listFiles { _, name -> name.contains(itemId) }
        tempFiles?.forEach { it.delete() }
    }

    fun clearTempDecryptedFiles() {
        cacheDirectory.listFiles()?.forEach { it.delete() }
    }
}
