// This file is part of The Ekdahl FAR firmware.
//
// The Ekdahl FAR firmware is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// The Ekdahl FAR firmware is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with The Ekdahl FAR firmware. If not, see <https://www.gnu.org/licenses/>.
//
// Copyright (C) 2024 Karl Ekdahl

// SymPy-like symbolic math is replaced with simple regex parsing and logic

/*
fun getVariable(equationStr: String, variableStr: String): Pair<Double, Double> {
    return try {
        val cleanedEquation = equationStr.replace(" ", "")
        val terms = cleanedEquation.split("+", "-").filter { it.isNotEmpty() }

        var offset = 0.0
        var multiplier = 0.0

        for (term in terms) {
            if (term.contains(variableStr)) {
                val pattern = Regex("""([-\d.]+)?\*?$variableStr""")
                val match = pattern.find(term)
                val coefficient = match?.groups?.get(1)?.value?.toDoubleOrNull() ?: 1.0
                multiplier += coefficient
            } else {
                offset += term.toDoubleOrNull() ?: 0.0
            }
        }
        Pair(offset, multiplier)
    } catch (e: Exception) {
        Pair(0.0, 0.0)
    }
}
*/
fun getVariable(equation: String, variable: String): Pair<Double, Double> {
    val cleanedEquation = equation.replace(" ", "")

    var multiplier = 0.0
    var offset = 0.0

    // Split the equation into additive/subtractive terms
    val additiveTerms = Regex("(?=[+-])").split(cleanedEquation)

    for (term in additiveTerms) {
        if (term.contains(variable)) {
            // Extract multiplier
            val multRegex = Regex("([\\d\\.]+)\\*?$variable|$variable\\*([\\d\\.]+)")
            val match = multRegex.find(term)
            val value = match?.groups?.get(1)?.value ?: match?.groups?.get(2)?.value

            if (value != null) {
                multiplier += value.toDouble()
            } else if (!term.contains("*")) {
                // If no explicit multiplier (e.g., just 'velocity'), treat as 1.0
                multiplier += 1.0
            } else {
                // More complex form, try to evaluate the multiplier
                val nestedRegex = Regex("\\(?$variable\\*([\\d\\.]+)\\)?")
                val nestedMatch = nestedRegex.find(term)
                val nestedVal = nestedMatch?.groups?.get(1)?.value
                multiplier += nestedVal?.toDouble() ?: 1.0
            }
        } else {
            // Try to parse constant offset
            try {
                // Remove parentheses and evaluate constant
                val cleaned = term.replace(Regex("[()]+"), "")
                if (cleaned.isNotBlank()) {
                    offset += cleaned.toDouble()
                }
            } catch (_: NumberFormatException) {
                // Skip non-constant terms like (notecount - 1)
            }
        }
    }

    return Pair(multiplier, offset)
}

fun isVariableInEquation(equationStr: String, variableStr: String): Boolean {
    return try {
        equationStr.contains(variableStr)
    } catch (e: Exception) {
        false
    }
}

fun removeFunction(equationStr: String, functionStr: String): String {
    val pattern = Regex("""^$functionStr\((.+?),\d+\)""")
    return pattern.replace(equationStr, "($1)")
}

fun extractValueOffsetAndDivisor(equationStr: String): Pair<Double, Double> {
    val pattern = Regex("""\(value-(\d+\.?\d*)\)/(\d+\.?\d*)""")
    val match = pattern.find(equationStr)

    if (match != null) {
        val value1 = match.groupValues[1].toDouble()
        val value2 = match.groupValues[2].toDouble()
        return Pair(value1, value2)
    } else {
        throw IllegalArgumentException("Equation format is invalid")
    }
}

fun extractValueOffsetAndMultiplier(equationStr: String): Pair<Double, Double> {
    val pattern = Regex("""value-(\d+\.?\d*)\)\*\(?(\d+\.?\d*)""")
    val match = pattern.find(equationStr)

    if (match != null) {
        val number1 = match.groupValues[1].toDouble()
        val number2 = match.groupValues[2].toDouble()
        return Pair(number1, number2)
    } else {
        throw IllegalArgumentException("Equation format is invalid")
    }
}

data class EquationParts(
    val zeroPosition: Double = 0.0,
    val coefficient: Double = 1.0,
    val offset: Double = 0.0
)

fun extractZeroCoefficientOffset(equation: String): EquationParts {
    var cleanedEquation = equation.replace("\\s+".toRegex(), "")
    var zeroPosition = 0.0
    var coefficient = 1.0
    var offset = 0.0

    val zeroPositionMatch = Regex("""value([\+\-]\d+(\.\d*)?)""").find(cleanedEquation)
    if (zeroPositionMatch != null) {
        zeroPosition = zeroPositionMatch.groupValues[1].toDouble()
    }

    val coefficientMatch = Regex("""([*/])([\d.]+)""").find(cleanedEquation)
    if (coefficientMatch != null) {
        val op = coefficientMatch.groupValues[1]
        val value = coefficientMatch.groupValues[2].toDouble()
        coefficient = if (op == "*") value else 1 / value
    }

    val offsetMatch = Regex("""[+\-]\(?([\d.]+)\)?$""").find(cleanedEquation)
    if (offsetMatch != null) {
        val offsetValue = offsetMatch.groupValues[1].toDouble()
        val sign = offsetMatch.value.first()
        offset = if (sign == '+') offsetValue else -offsetValue
    }

    return EquationParts(zeroPosition, coefficient, offset)
}

fun stripBoolIBool(expression: String): Double? {
    val cleanedExpression = expression.replace("\\s+".toRegex(), "")
    val match = Regex("""\((.*)\)""").find(cleanedExpression)

    if (match != null) {
        val inner = match.groupValues[1]
        val numberMatch = Regex("""[-+]?\d*\.?\d+""").find(inner)
        if (numberMatch != null) {
            return numberMatch.value.toDouble()
        }
    }
    return null
}
