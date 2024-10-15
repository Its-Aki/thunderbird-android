package app.k9mail.feature.funding.googleplay.data

import android.app.Activity
import app.k9mail.feature.funding.googleplay.domain.entity.Contribution
import app.k9mail.feature.funding.googleplay.domain.entity.OneTimeContribution
import app.k9mail.feature.funding.googleplay.domain.entity.RecurringContribution
import com.android.billingclient.api.ProductDetails

interface DataContract {

    interface Mapper {
        interface Product {
            fun mapToContribution(product: ProductDetails): Contribution

            fun mapToOneTimeContribution(product: ProductDetails): OneTimeContribution
            fun mapToRecurringContribution(product: ProductDetails): RecurringContribution
        }
    }

    interface BillingClient {

        /**
         * Connect to the billing service.
         *
         * @param onConnected Callback to be invoked when the billing service is connected.
         */
        suspend fun <T> connect(onConnected: suspend () -> T): T

        /**
         * Disconnect from the billing service.
         */
        fun disconnect()

        /**
         * Load one-time contributions.
         */
        suspend fun loadOneTimeContributions(
            productIds: List<String>,
        ): List<OneTimeContribution>

        /**
         * Load recurring contributions.
         */
        suspend fun loadRecurringContributions(
            productIds: List<String>,
        ): List<RecurringContribution>

        /**
         * Load purchased contributions.
         */
        suspend fun loadPurchasedContributions(): List<Contribution>

        /**
         * Purchase a contribution.
         */
        suspend fun purchaseContribution(
            activity: Activity,
            contribution: Contribution,
        ): Contribution?
    }
}
