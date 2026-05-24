package com.ottertondev.metroatbkk.data

object GtfsCsvParser {
    fun parseLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var index = 0

        while (index < line.length) {
            val char = line[index]
            when {
                char == '"' && inQuotes && index + 1 < line.length && line[index + 1] == '"' -> {
                    current.append('"')
                    index++
                }

                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    fields += current.toString()
                    current.clear()
                }

                else -> current.append(char)
            }
            index++
        }

        fields += current.toString()
        return fields
    }

    fun headerIndexes(headerLine: String): Map<String, Int> {
        return parseLine(headerLine).mapIndexed { index, columnName -> columnName to index }.toMap()
    }

    fun field(fields: List<String>, headerIndexes: Map<String, Int>, name: String): String {
        return headerIndexes[name]?.let { index -> fields.getOrNull(index) }.orEmpty()
    }
}
