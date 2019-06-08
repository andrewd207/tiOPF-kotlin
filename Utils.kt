package tiOPF

fun tiLineEnd(count: Int = 1): String{
    return tiReplicate(CLineEnding.toString(), count)
}

fun tiReplicate(value: String, replicateCount: Int): String{
    var result = value
    for (i in 1..replicateCount ){
        result += value
    }
    return result
}

fun tiAddTrailingValue(line: String, value: String, duplicates: Boolean = true): String{
    if (line.isEmpty())
        return line

    if (duplicates)
        return line + value

    val start = line.length - value.length + 1
    val subString = line.substring( start, start + value.length)

    if (value === subString)
        return line + value

    return line
}



fun tiAddEllipsis(string: String, width: Int = 20): String{
    val len = string.length
    when {
        len < width -> return string
        len > width -> return string.substring(0, width-4) + "..."
        else -> return string.substring(0, len-4) + "..."
    }
}

