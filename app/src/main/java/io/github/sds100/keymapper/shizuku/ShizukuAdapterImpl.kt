package io.github.sds100.keymapper.shizuku

import io.github.sds100.keymapper.system.apps.PackageManagerAdapter
import io.github.sds100.keymapper.util.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import rikka.shizuku.Shizuku

/**
 * Created by sds100 on 20/07/2021.
 */
class ShizukuAdapterImpl(
    private val coroutineScope: CoroutineScope,
    private val packageManagerAdapter: PackageManagerAdapter,
) : ShizukuAdapter {
    override val isStarted by lazy { MutableStateFlow(Shizuku.getBinder() != null) }

    private val isAppInstalled: StateFlow<Boolean> = packageManagerAdapter.installedPackages
        .filter { it is State.Data }
        .map { state ->
            require(state is State.Data)

            state.data.any { it.packageName == ShizukuUtils.SHIZUKU_PACKAGE }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            coroutineScope,
            SharingStarted.WhileSubscribed(),
            packageManagerAdapter.isAppInstalled(ShizukuUtils.SHIZUKU_PACKAGE),
        )

    /**
     * See issue #1372.
     * Shizuku can be installed through Sui or the Shizuku app so
     * if Shizuku is started then assume it is installed with Sui.
     */
    override val isInstalled: StateFlow<Boolean> by lazy {
        combine(isStarted, isAppInstalled) { isStarted, isAppInstalled ->
            isStarted || isAppInstalled
        }.stateIn(coroutineScope, SharingStarted.Lazily, false)
    }

    init {
        Shizuku.addBinderReceivedListener {
            isStarted.value = Shizuku.getBinder() != null
        }

        Shizuku.addBinderDeadListener {
            isStarted.value = Shizuku.getBinder() != null
        }
    }

    override fun openShizukuApp() {
        packageManagerAdapter.openApp(ShizukuUtils.SHIZUKU_PACKAGE)
    }
}
