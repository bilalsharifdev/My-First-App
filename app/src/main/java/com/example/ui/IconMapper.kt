package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CategoryIconOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

val AvailableCategoryIcons = listOf(
    CategoryIconOption("school", "School & Fees", Icons.Default.School),
    CategoryIconOption("bolt", "Electricity", Icons.Default.Bolt),
    CategoryIconOption("local_fire_department", "Gas & Heating", Icons.Default.LocalFireDepartment),
    CategoryIconOption("water_drop", "Water Utility", Icons.Default.WaterDrop),
    CategoryIconOption("shopping_cart", "Groceries", Icons.Default.ShoppingCart),
    CategoryIconOption("directions_car", "Transport", Icons.Default.DirectionsCar),
    CategoryIconOption("medical_services", "Medical", Icons.Default.MedicalServices),
    CategoryIconOption("wifi", "Internet & Phone", Icons.Default.Wifi),
    CategoryIconOption("home", "Housing & Rent", Icons.Default.Home),
    CategoryIconOption("savings", "Daily Saving", Icons.Default.Savings),
    CategoryIconOption("account_balance", "Bank Saving", Icons.Default.AccountBalance),
    CategoryIconOption("verified_user", "Emergency Fund", Icons.Default.VerifiedUser),
    CategoryIconOption("child_care", "Children Care", Icons.Default.ChildCare),
    CategoryIconOption("fastfood", "Dining Out", Icons.Default.Fastfood),
    CategoryIconOption("checkroom", "Clothing", Icons.Default.Checkroom),
    CategoryIconOption("build", "Repairs", Icons.Default.Build),
    CategoryIconOption("receipt_long", "General Bill", Icons.Default.ReceiptLong),
    CategoryIconOption("payments", "Cash / Other", Icons.Default.Payments)
)

fun getCategoryIcon(iconName: String): ImageVector {
    return AvailableCategoryIcons.firstOrNull { it.key == iconName }?.icon ?: Icons.Default.Category
}

fun formatCurrency(amount: Double, symbol: String): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
        maximumFractionDigits = 2
    }
    val sign = if (amount < 0) "-" else ""
    val needsSpace = symbol.length > 1 || symbol.equals("PKR", ignoreCase = true) || symbol.equals("Rs", ignoreCase = true)
    val spacer = if (needsSpace) " " else ""
    return "$sign$symbol$spacer${formatter.format(kotlin.math.abs(amount))}"
}

fun formatYearMonthDisplay(yearMonth: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM", Locale.US)
        val date = parser.parse(yearMonth) ?: return yearMonth
        SimpleDateFormat("MMMM yyyy", Locale.US).format(date)
    } catch (e: Exception) {
        yearMonth
    }
}

fun formatDayHeader(timestamp: Long): String {
    val entryCal = Calendar.getInstance().apply { timeInMillis = timestamp }
    val todayCal = Calendar.getInstance()
    val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    val isToday = entryCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
        entryCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
    val isYesterday = entryCal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR) &&
        entryCal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR)

    val datePart = SimpleDateFormat("EEE, MMM d, yyyy", Locale.US).format(Date(timestamp))
    return when {
        isToday -> "Today · $datePart"
        isYesterday -> "Yesterday · $datePart"
        else -> datePart
    }
}

fun formatFullDateTime(timestamp: Long): String {
    return SimpleDateFormat("EEE, MMM d, yyyy · hh:mm a", Locale.US).format(Date(timestamp))
}

fun formatTimeOnly(timestamp: Long): String {
    return SimpleDateFormat("hh:mm a", Locale.US).format(Date(timestamp))
}

fun isDateBackdated(transactionTimestamp: Long, referenceTimestamp: Long = System.currentTimeMillis()): Boolean {
    val txCal = Calendar.getInstance().apply {
        timeInMillis = transactionTimestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val refCal = Calendar.getInstance().apply {
        timeInMillis = referenceTimestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return txCal.timeInMillis < refCal.timeInMillis
}
