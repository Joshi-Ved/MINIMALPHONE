package com.example.minimalphone.domain.usecase

import com.example.minimalphone.domain.model.AppBlockMode
import com.example.minimalphone.domain.model.FocusSettings

class ShouldBlockPackageUseCase {
    operator fun invoke(settings: FocusSettings, packageName: String): Boolean {
        if (!settings.premiumEnabled) return false

        return when (settings.blockMode) {
            AppBlockMode.BLOCK_SELECTED -> settings.blockedPackages.contains(packageName)
            AppBlockMode.ALLOW_ONLY_WHITELIST -> !settings.allowedPackages.contains(packageName)
        }
    }
}
