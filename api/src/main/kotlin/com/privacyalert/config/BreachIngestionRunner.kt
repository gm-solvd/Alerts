package com.privacyalert.config

import com.privacyalert.domain.model.KnownBreach
import com.privacyalert.domain.repository.BreachDatabaseRepository
import com.privacyalert.domain.service.DataTypeNormalizer
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.FileReader
import java.time.LocalDate

@Profile("ingest-breaches")
@Component
class BreachIngestionRunner(
    private val appProperties: AppProperties,
    private val breachDatabaseRepository: BreachDatabaseRepository,
    private val dataTypeNormalizer: DataTypeNormalizer,
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(BreachIngestionRunner::class.java)

    override fun run(args: ApplicationArguments) {
        val csvPath = appProperties.ingest.breachCsvPath
        if (csvPath.isBlank()) {
            log.warn("No CSV path configured (app.ingest.breach-csv-path). Skipping ingestion.")
            return
        }

        log.info("Starting breach data ingestion from: {}", csvPath)

        var total = 0
        var inserted = 0
        var skipped = 0

        BufferedReader(FileReader(csvPath)).use { reader ->
            val header = reader.readLine() ?: return
            val columns = parseCsvLine(header)
            val columnIndex = columns.withIndex().associate { (i, name) -> name.trim() to i }

            val entityIdx = columnIndex["Entity"] ?: columnIndex["entity"] ?: columnIndex["Organization"]
            val recordsIdx = columnIndex["Records"] ?: columnIndex["records"] ?: columnIndex["Records Lost"]
            val yearIdx = columnIndex["Year"] ?: columnIndex["year"]
            val methodIdx = columnIndex["Method"] ?: columnIndex["method"]
            val sourcesIdx =
                columnIndex["Sources"] ?: columnIndex["sources"]
                    ?: columnIndex["Data Sensitivity"] ?: columnIndex["data_classes"]

            if (entityIdx == null) {
                log.error("CSV must have an 'Entity' column. Found columns: {}", columns)
                return
            }

            reader.lineSequence().forEach { line ->
                if (line.isBlank()) return@forEach
                total++

                val fields = parseCsvLine(line)
                val name = fields.getOrNull(entityIdx)?.trim() ?: return@forEach

                if (breachDatabaseRepository.findByName(name) != null) {
                    skipped++
                    return@forEach
                }

                val recordCount = recordsIdx?.let { fields.getOrNull(it)?.trim()?.toLongOrNull() }
                val year = yearIdx?.let { fields.getOrNull(it)?.trim()?.toIntOrNull() }
                val breachDate = year?.let { LocalDate.of(it, 1, 1) }
                val rawDataTypes = sourcesIdx?.let { fields.getOrNull(it)?.trim() } ?: ""
                val dataClasses = dataTypeNormalizer.normalize(listOf(rawDataTypes))
                val method = methodIdx?.let { fields.getOrNull(it)?.trim() }

                breachDatabaseRepository.save(
                    KnownBreach(
                        name = name,
                        breachDate = breachDate,
                        dataClasses = dataClasses,
                        recordCount = recordCount,
                        sourceUrl = method,
                    ),
                )
                inserted++
            }
        }

        log.info("Breach ingestion complete. Total: {}, Inserted: {}, Skipped (duplicates): {}", total, inserted, skipped)
    }

    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    fields.add(current.toString())
                    current.clear()
                }
                else -> current.append(ch)
            }
        }
        fields.add(current.toString())
        return fields
    }
}
