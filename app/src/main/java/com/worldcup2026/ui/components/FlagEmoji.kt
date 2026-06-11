package com.worldcup2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// FIFA nameCode (3-letter) → ISO alpha-2, for knockout bracket data
// which only provides nameCode, not country.alpha2
private val namecodeToAlpha2: Map<String, String> = mapOf(
    "AFG" to "af", "ALB" to "al", "ALG" to "dz", "AGO" to "ao", "ARG" to "ar",
    "ARM" to "am", "AUS" to "au", "AUT" to "at", "AZE" to "az", "BEL" to "be",
    "BLR" to "by", "BOL" to "bo", "BIH" to "ba", "BRA" to "br", "BUL" to "bg",
    "BFA" to "bf", "CMR" to "cm", "CAN" to "ca", "CPV" to "cv", "CHI" to "cl",
    "CHN" to "cn", "COL" to "co", "COD" to "cd", "CRC" to "cr", "CIV" to "ci",
    "CRO" to "hr", "CZE" to "cz", "DEN" to "dk", "ECU" to "ec", "EGY" to "eg",
    "ENG" to "gb-eng", "FIN" to "fi", "FRA" to "fr", "GAM" to "gm", "GEO" to "ge",
    "GER" to "de", "GHA" to "gh", "GRE" to "gr", "GTM" to "gt", "HAI" to "ht",
    "HON" to "hn", "HUN" to "hu", "ISL" to "is", "IND" to "in", "IDN" to "id",
    "IRN" to "ir", "IRI" to "ir", "IRQ" to "iq", "IRL" to "ie", "ISR" to "il",
    "ITA" to "it", "JAM" to "jm", "JPN" to "jp", "JOR" to "jo", "KAZ" to "kz",
    "KEN" to "ke", "KOR" to "kr", "KSA" to "sa", "KUW" to "kw", "LIB" to "lb",
    "MLI" to "ml", "MAR" to "ma", "MEX" to "mx", "MDA" to "md", "MNG" to "mn",
    "MNE" to "me", "MOZ" to "mz", "NAM" to "na", "NED" to "nl", "NZL" to "nz",
    "NCA" to "ni", "NGA" to "ng", "NIR" to "gb-nir", "NOR" to "no", "OMA" to "om",
    "PAN" to "pa", "PAR" to "py", "PER" to "pe", "POL" to "pl", "POR" to "pt",
    "QAT" to "qa", "ROU" to "ro", "RUS" to "ru", "KSA" to "sa", "SCO" to "gb-sct",
    "SEN" to "sn", "SRB" to "rs", "SVK" to "sk", "SVN" to "si", "RSA" to "za",
    "ESP" to "es", "SUI" to "ch", "SWE" to "se", "SYR" to "sy", "TAN" to "tz",
    "THA" to "th", "TUN" to "tn", "TUR" to "tr", "TTO" to "tt", "UGA" to "ug",
    "UKR" to "ua", "UAE" to "ae", "URU" to "uy", "USA" to "us", "UZB" to "uz",
    "VEN" to "ve", "WAL" to "gb-wls", "ZAM" to "zm", "ZIM" to "zw",
)

// Non-standard 2-letter codes that SofaScore returns for sub-national teams
private val alpha2Fix: Map<String, String> = mapOf(
    "en" to "gb-eng",   // England
    "sc" to "gb-sct",   // Scotland
    "wa" to "gb-wls",   // Wales
    "ni" to "gb-nir",   // Northern Ireland
)

private fun resolveAlpha2(code: String): String {
    val c = code.trim()
    val lower = c.lowercase()
    return when {
        c.length == 2 -> alpha2Fix[lower] ?: lower                       // check override first
        c.length >= 3 -> namecodeToAlpha2[c.uppercase()] ?: lower        // nameCode lookup
        else -> lower
    }
}

// countryCode: alpha-2 (from country.alpha2) or FIFA nameCode (from bracket data)
// URL: https://flagcdn.com/w80/{alpha2_lowercase}.png
@Composable
fun FlagEmoji(
    countryCode: String,
    size: Dp = 32.dp,
    fontSize: TextUnit = 28.sp,  // kept for API compatibility, unused
    modifier: Modifier = Modifier
) {
    val code = resolveAlpha2(countryCode)

    if (code.isNotEmpty()) {
        AsyncImage(
            model = "https://flagcdn.com/w80/$code.png",
            contentDescription = countryCode,
            contentScale = ContentScale.FillBounds,
            modifier = modifier
                .size(width = size * 1.4f, height = size)
                .clip(RoundedCornerShape(3.dp))
        )
    } else {
        Box(
            modifier = modifier
                .size(width = size * 1.4f, height = size)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFFCCCCCC)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "?",
                fontSize = 10.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
        }
    }
}
