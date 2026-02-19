package org.jetbrains.kotlin.firebase.sample.samples

import swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRStorage
import swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRStorageMetadata
import org.jetbrains.kotlin.firebase.sample.KMPError
import org.jetbrains.kotlin.firebase.sample.toKMPError
import platform.Foundation.NSData

/**
 * Sample demonstrating Firebase Storage operations:
 * uploading, downloading, and managing files in cloud storage.
 */
@Suppress("UNUSED")
object StorageSample {

    private val storage: FIRStorage
        get() = FIRStorage.storage()

    /**
     * Upload raw data to a storage path.
     */
    fun uploadData(
        path: String,
        data: NSData,
        contentType: String = "application/octet-stream",
        completion: (String?, KMPError?) -> Unit
    ) {
        val ref = storage.reference().child(path)
        val metadata = FIRStorageMetadata()
        metadata.setContentType(contentType)

        ref.putData(data, metadata) { resultMetadata, error ->
            if (error != null) {
                completion(null, error.toKMPError())
            } else {
                ref.downloadURLWithCompletion { url, urlError ->
                    if (urlError != null) {
                        completion(null, urlError.toKMPError())
                    } else {
                        completion(url?.absoluteString, null)
                    }
                }
            }
        }
    }

    /**
     * Download data from a storage path.
     */
    fun downloadData(
        path: String,
        maxSize: Long = 10L * 1024 * 1024,
        completion: (NSData?, KMPError?) -> Unit
    ) {
        val ref = storage.reference().child(path)
        ref.dataWithMaxSize(maxSize) { data, error ->
            if (error != null) {
                completion(null, error.toKMPError())
            } else {
                completion(data, null)
            }
        }
    }

    /**
     * Get the download URL for a file.
     */
    fun getDownloadUrl(path: String, completion: (String?, KMPError?) -> Unit) {
        val ref = storage.reference().child(path)
        ref.downloadURLWithCompletion { url, error ->
            if (error != null) {
                completion(null, error.toKMPError())
            } else {
                completion(url?.absoluteString, null)
            }
        }
    }

    /**
     * Delete a file from storage.
     */
    fun deleteFile(path: String, completion: (KMPError?) -> Unit) {
        val ref = storage.reference().child(path)
        ref.deleteWithCompletion { error ->
            completion(error?.toKMPError())
        }
    }

    /**
     * List files at a storage path.
     */
    fun listFiles(path: String, maxResults: Long = 100, completion: (List<String>, KMPError?) -> Unit) {
        val ref = storage.reference().child(path)
        ref.listWithMaxResults(maxResults) { result, error ->
            if (error != null) {
                completion(emptyList(), error.toKMPError())
            } else {
                val items = result?.items()?.mapNotNull { item ->
                    (item as? swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRStorageReference)?.fullPath()
                } ?: emptyList()
                completion(items, null)
            }
        }
    }
}
