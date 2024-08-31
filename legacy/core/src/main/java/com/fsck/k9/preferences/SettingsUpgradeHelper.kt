package com.fsck.k9.preferences

import com.fsck.k9.K9
import com.fsck.k9.preferences.Settings.SettingsDescription
import java.util.TreeMap
import timber.log.Timber

internal object SettingsUpgradeHelper {
    /**
     * Upgrade settings using the settings structure and/or special upgrade code.
     *
     * @param version
     *   The content version of the settings in `settings`.
     * @param upgraders
     *   A map of [SettingsUpgrader]s for nontrivial settings upgrades.
     * @param settingsDescriptions
     *   The structure describing the different settings, possibly containing multiple versions.
     * @param settings
     *   The validated settings as returned by [Settings.validate].
     *
     * @return The upgraded settings.
     */
    @JvmStatic
    fun upgrade(
        version: Int,
        upgraders: Map<Int, SettingsUpgrader>,
        settingsDescriptions: Map<String, TreeMap<Int, SettingsDescription<*>?>>,
        settings: Map<String, Any?>,
    ): Map<String, Any?> {
        val upgradedSettings: MutableMap<String, Any?> = HashMap(settings)
        for (toVersion in version + 1..Settings.VERSION) {
            if (upgraders.containsKey(toVersion)) {
                val upgrader = upgraders[toVersion]
                upgrader!!.upgrade(upgradedSettings)
            }

            upgradeSettingsGeneric(settingsDescriptions, upgradedSettings, toVersion)
        }

        return upgradedSettings
    }

    private fun upgradeSettingsGeneric(
        settingsDescriptions: Map<String, TreeMap<Int, SettingsDescription<*>?>>,
        mutableSettings: MutableMap<String, Any?>,
        toVersion: Int,
    ) {
        for ((settingName, versionedSettingsDescriptions) in settingsDescriptions) {
            val isNewlyAddedSetting = versionedSettingsDescriptions.firstKey() == toVersion
            if (isNewlyAddedSetting) {
                val wasHandledByCustomUpgrader = mutableSettings.containsKey(settingName)
                if (wasHandledByCustomUpgrader) {
                    continue
                }

                val settingDescription = versionedSettingsDescriptions[toVersion]
                    ?: throw AssertionError("First version of a setting must be non-null!")

                upgradeSettingInsertDefault(mutableSettings, settingName, settingDescription)
            }

            val highestVersion = versionedSettingsDescriptions.lastKey()
            val isRemovedSetting = highestVersion == toVersion &&
                versionedSettingsDescriptions[highestVersion] == null

            if (isRemovedSetting) {
                mutableSettings.remove(settingName)
                Timber.v("Removed setting \"%s\"", settingName)
            }
        }
    }

    private fun <T> upgradeSettingInsertDefault(
        mutableSettings: MutableMap<String, Any?>,
        settingName: String,
        settingDescription: SettingsDescription<T>,
    ) {
        val defaultValue = settingDescription.getDefaultValue()
        mutableSettings[settingName] = defaultValue

        if (K9.isDebugLoggingEnabled) {
            val prettyValue = settingDescription.toPrettyString(defaultValue)
            Timber.v("Added new setting \"%s\" with default value \"%s\"", settingName, prettyValue)
        }
    }
}
