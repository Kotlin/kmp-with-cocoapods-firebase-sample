package org.jetbrains.kotlin.firebase.sample.samples

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDatabaseReference
import org.jetbrains.kotlin.firebase.sample.KMPError
import org.jetbrains.kotlin.firebase.sample.toKMPError

/**
 * Sample demonstrating Firebase Realtime Database:
 * reading, writing, and observing data in real time.
 */
@Suppress("UNCHECKED_CAST", "UNUSED")
object DatabaseSample {

    private val database: FIRDatabase
        get() = FIRDatabase.database()

    private val rootRef: FIRDatabaseReference
        get() = database.reference()

    /**
     * Enable offline persistence for the Realtime Database.
     */
    fun enablePersistence() {
        database.setPersistenceEnabled(true)
    }

    /**
     * Write data to a specific path.
     */
    fun writeData(path: String, data: Map<String, Any>, completion: (KMPError?) -> Unit) {
        rootRef.child(path).setValue(data as Any?) { error, _ ->
            completion(error?.toKMPError())
        }
    }

    /**
     * Read data once from a specific path.
     */
    fun readDataOnce(path: String, completion: (Any?, KMPError?) -> Unit) {
        rootRef.child(path).observeSingleEventOfType(
            FIRDataEventType.FIRDataEventTypeValue
        ) { snapshot ->
            completion(snapshot?.value(), null)
        }
    }

    /**
     * Observe real-time data changes at a path.
     * Returns a handle that can be used to remove the observer.
     */
    fun observeData(path: String, onChange: (Any?) -> Unit): ULong {
        return rootRef.child(path).observeEventType(
            FIRDataEventType.FIRDataEventTypeValue
        ) { snapshot ->
            onChange(snapshot?.value())
        }
    }

    /**
     * Remove an observer by handle.
     */
    fun removeObserver(path: String, handle: ULong) {
        rootRef.child(path).removeObserverWithHandle(handle)
    }

    /**
     * Push a new child with auto-generated key.
     */
    fun pushData(path: String, data: Map<String, Any>, completion: (String?, KMPError?) -> Unit) {
        val newRef = rootRef.child(path).childByAutoId()
        newRef.setValue(data as Any?) { error, _ ->
            if (error != null) {
                completion(null, error.toKMPError())
            } else {
                completion(newRef.key(), null)
            }
        }
    }

    /**
     * Update specific children at a path.
     */
    fun updateChildren(path: String, updates: Map<String, Any>, completion: (KMPError?) -> Unit) {
        rootRef.child(path).updateChildValues(updates as Map<Any?, *>) { error, _ ->
            completion(error?.toKMPError())
        }
    }

    /**
     * Delete data at a specific path.
     */
    fun deleteData(path: String, completion: (KMPError?) -> Unit) {
        rootRef.child(path).removeValueWithCompletionBlock { error, _ ->
            completion(error?.toKMPError())
        }
    }
}
