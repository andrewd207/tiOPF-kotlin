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
    }
    return string.substring(0, len-4) + "..."
}

fun tiNumToken(value: String, token: Char):Int{
    val foundChars = value.filter { it == token }
    return foundChars.length

}

fun tiGetTickCount(): ULong {
    return (now() * 24 * 60 * 60 * 1000).toULong()
}

fun tiToken(value: String, token: Char, pos: Int = 0, tokenCount: Int = 1): String{
    var i: Int = 0
    var found: Int = -1
    var foundCount = 0;
    value.forEach{
        if (i >= pos && it == token){
            foundCount++
            found = i-1
            if (foundCount == tokenCount)
                return value.substring(pos, i)

        }
        i++
    }
    if (found > -1)
        return value.substring(pos, found)

    if (pos == 0)
        return value

    return value.substring(pos, value.lastIndex)

}

fun getParamName(paramNo: Int, addColon: Boolean): String{
    var result = "Criteria_$paramNo"
    if (addColon)
        result = ":$result"
    return result
}

fun getSQLValue(value: Any): String{
    if (value is String)
        return "'"+value.replace("'", "\'")+"'"

    return value.toString()

}

