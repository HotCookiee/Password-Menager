package com.yourname.passwordmanager.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

/**
 * Handles encryption and decryption of sensitive data
 */
class CryptoManager(context: Context) {
    
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        "password_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    /**
     * Encrypt a password using AES-256-GCM
     */
    fun encryptPassword(password: String): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = getOrCreateSecretKey()
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val iv = cipher.iv
            val encryptedData = cipher.doFinal(password.toByteArray(Charsets.UTF_8))
            
            // Combine IV and encrypted data
            val combined = iv + encryptedData
            Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            throw SecurityException("Encryption failed", e)
        }
    }
    
    /**
     * Decrypt a password using AES-256-GCM
     */
    fun decryptPassword(encryptedPassword: String): String {
        return try {
            val combined = Base64.decode(encryptedPassword, Base64.DEFAULT)
            val iv = combined.sliceArray(0 until IV_LENGTH)
            val encryptedData = combined.sliceArray(IV_LENGTH until combined.size)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = getOrCreateSecretKey()
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            
            val decryptedData = cipher.doFinal(encryptedData)
            String(decryptedData, Charsets.UTF_8)
        } catch (e: Exception) {
            throw SecurityException("Decryption failed", e)
        }
    }
    
    /**
     * Get or create a secret key for encryption
     */
    private fun getOrCreateSecretKey(): SecretKey {
        val keyString = sharedPreferences.getString(SECRET_KEY_ALIAS, null)
        return if (keyString != null) {
            val keyBytes = Base64.decode(keyString, Base64.DEFAULT)
            SecretKeySpec(keyBytes, ALGORITHM)
        } else {
            val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
            keyGenerator.init(KEY_LENGTH)
            val secretKey = keyGenerator.generateKey()
            val keyBytes = secretKey.encoded
            val keyString = Base64.encodeToString(keyBytes, Base64.DEFAULT)
            sharedPreferences.edit().putString(SECRET_KEY_ALIAS, keyString).apply()
            secretKey
        }
    }
    
    /**
     * Generate a secure random password
     */
    fun generateSecurePassword(
        length: Int = DEFAULT_PASSWORD_LENGTH,
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        val chars = buildString {
            if (includeUppercase) append(UPPERCASE_CHARS)
            if (includeLowercase) append(LOWERCASE_CHARS)
            if (includeNumbers) append(NUMBER_CHARS)
            if (includeSymbols) append(SYMBOL_CHARS)
        }
        
        if (chars.isEmpty()) {
            throw IllegalArgumentException("At least one character type must be selected")
        }
        
        val random = SecureRandom()
        return (1..length)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }
    
    /**
     * Check if encryption is available
     */
    fun isEncryptionAvailable(): Boolean {
        return try {
            encryptPassword("test")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    companion object {
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val SECRET_KEY_ALIAS = "secret_key"
        private const val KEY_LENGTH = 256
        private const val IV_LENGTH = 12 // GCM standard IV length
        private const val GCM_TAG_LENGTH = 128 // GCM tag length in bits
        private const val DEFAULT_PASSWORD_LENGTH = 16
        
        // Character sets for password generation
        private const val UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz"
        private const val NUMBER_CHARS = "0123456789"
        private const val SYMBOL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?"
    }
}