package song.sam.cozytrain.ui.healthconnect.viewmodel

import android.os.RemoteException
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.records.Record
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CompletableDeferred
import song.sam.cozytrain.data.healthconnect.HealthConnectSource
import song.sam.cozytrain.ui.component.ViewModelData
import song.sam.cozytrain.ui.healthconnect.UiState
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException

/**
 * Implements [HealthConnectViewModel] and add some shared variables
 */
abstract class HealthConnectViewModel<T : Record> : ViewModel() {
    /**
     * Holds UiState to show (or not) data, request permission button, exceptions...
     */
    var uiState: UiState by mutableStateOf(UiState.Uninitialized)
        protected set

    protected val elements: MutableState<List<T>> = mutableStateOf(listOf())

    /**
     * Launcher to request permissions
     */
    val permissionLauncher = PermissionController.createRequestPermissionResultContract()
    
//    fun init(HealthConnectSource: HealthConnectSource<T>) : Boolean{
//        val result = CompletableDeferred<Boolean>()
//
//        viewModelScope.launch {
//            val permissionsCheckResult = HealthConnectSource.readPermissionsCheck()
//            result.complete(permissionsCheckResult)
//        }
//
//        return runBlocking {
//            result.await()
//        }
//    }

    fun readData(HealthConnectSource: HealthConnectSource<T>) {
        viewModelScope.launch {
            uiState = try {
                if (HealthConnectSource.readPermissionsCheck()) {
                    elements.value = HealthConnectSource.readSource()
                    UiState.Success
                }
                else
                    UiState.NotEnoughPermissions
            }
            catch (remoteException: RemoteException) {
                UiState.Error(remoteException)
            }
            catch (securityException: SecurityException) {
                UiState.Error(securityException)
            }
            catch (ioException: IOException) {
                UiState.Error(ioException)
            }
            catch (illegalStateException: IllegalStateException) {
                UiState.Error(illegalStateException)
            }
        }
    }

    @Composable
    abstract fun getViewModelData(): ViewModelData<T>
}