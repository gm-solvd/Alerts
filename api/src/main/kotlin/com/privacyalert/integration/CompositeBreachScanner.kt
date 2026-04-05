package com.privacyalert.integration

import com.privacyalert.domain.service.BreachResult
import com.privacyalert.domain.service.BreachScanner
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.util.Optional

@Primary
@Component
class CompositeBreachScanner(
    private val localBreachScanner: LocalBreachScannerImpl,
    private val pasteMonitorClient: PasteMonitorClient,
    private val hibpClient: Optional<HibpClientImpl>,
    private val xonClient: Optional<XonEmailScannerImpl>,
    private val combClient: Optional<CombScannerImpl>,
) : BreachScanner {
    override fun scanEmail(email: String): List<BreachResult> {
        val results = mutableListOf<BreachResult>()

        results.addAll(localBreachScanner.scanEmail(email))
        results.addAll(pasteMonitorClient.scanEmail(email))

        hibpClient.ifPresent { client ->
            results.addAll(client.scanEmail(email))
        }

        xonClient.ifPresent { client ->
            results.addAll(client.scanEmail(email))
        }

        combClient.ifPresent { client ->
            results.addAll(client.scanEmail(email))
        }

        return results.distinctBy { it.name }
    }

    override fun scanPhone(phone: String): List<BreachResult> {
        val results = mutableListOf<BreachResult>()

        results.addAll(localBreachScanner.scanPhone(phone))
        results.addAll(pasteMonitorClient.scanPhone(phone))

        return results.distinctBy { it.name }
    }
}
