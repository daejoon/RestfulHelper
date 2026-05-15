package com.github.nayacco.restfulhelper.utils

fun String.unquote(): String = if (length >= 2 && first() == '"' && last() == '"') substring(1, this.length - 1) else this

fun String.inCurlyBrackets(): Boolean = length >= 2 && first() == '{' && last() == '}'

fun String.unquoteCurlyBrackets(): String = if (this.inCurlyBrackets()) this.drop(1).dropLast(1) else this

fun String.addCurlyBrackets(): String = "{$this}"

fun List<String>.dropFirstEmptyStringIfExists(): List<String> = if (this.isNotEmpty() && this.first().isEmpty()) this.drop(1) else this

fun String.isNumeric(): Boolean = this.toBigDecimalOrNull() != null

// Collapses consecutive opening/closing curly brackets into a single one so that
// {{var}}-style input copied from tools like Postman/Mustache matches single-brace
// {var} mapping definitions. No pairing validation or semantic interpretation is done.
fun String.collapseRepeatedCurlyBrackets(): String =
    this.replace(OPENING_CURLY_BRACKETS_REGEX, "{").replace(CLOSING_CURLY_BRACKETS_REGEX, "}")

private val OPENING_CURLY_BRACKETS_REGEX = Regex("\\{+")
private val CLOSING_CURLY_BRACKETS_REGEX = Regex("\\}+")
