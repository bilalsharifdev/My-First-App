package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.CategoryEntity
import com.example.data.EntryType
import com.example.data.LedgerEntryEntity
import com.example.ui.formatFullDateTime
import com.example.ui.getCategoryIcon
import com.example.ui.isDateBackdated
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditEntrySheet(
    categories: List<CategoryEntity>,
    currencySymbol: String,
    existingEntry: LedgerEntryEntity? = null,
    initialEntryType: EntryType = EntryType.EXPENSE,
    initialCategoryId: Long? = null,
    initialTimestamp: Long? = null,
    onDismiss: () -> Unit,
    onSave: (
        existingId: Long,
        title: String,
        amount: Double,
        entryType: EntryType,
        category: CategoryEntity,
        transactionTimestamp: Long,
        paymentMethod: String,
        notes: String
    ) -> Unit,
    onDelete: ((LedgerEntryEntity) -> Unit)? = null,
    onAddCustomCategory: (
        name: String,
        iconName: String,
        colorHex: Long,
        entryType: EntryType,
        onCreated: (CategoryEntity) -> Unit
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedType by remember {
        mutableStateOf(
            if (existingEntry != null) {
                EntryType.valueOf(existingEntry.entryType)
            } else {
                initialEntryType
            }
        )
    }

    val filteredCategories = remember(categories, selectedType) {
        categories.filter { it.entryType == selectedType.name }
    }

    var selectedCategory by remember {
        mutableStateOf(
            when {
                existingEntry != null -> categories.firstOrNull { it.id == existingEntry.categoryId }
                initialCategoryId != null -> categories.firstOrNull { it.id == initialCategoryId }
                else -> filteredCategories.firstOrNull()
            }
        )
    }

    LaunchedEffect(selectedType, categories) {
        if (selectedCategory == null || selectedCategory?.entryType != selectedType.name) {
            selectedCategory = categories.firstOrNull { it.entryType == selectedType.name }
        }
    }

    var amountText by remember {
        mutableStateOf(
            if (existingEntry != null) {
                if (existingEntry.amount % 1.0 == 0.0) existingEntry.amount.toInt().toString()
                else existingEntry.amount.toString()
            } else ""
        )
    }
    var titleText by remember { mutableStateOf(existingEntry?.title ?: "") }
    var transactionTimestamp by remember {
        mutableLongStateOf(
            existingEntry?.transactionTimestamp ?: initialTimestamp ?: System.currentTimeMillis()
        )
    }
    var amountError by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    val isBackdated = remember(transactionTimestamp) {
        isDateBackdated(transactionTimestamp)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Top Title + Delete button if editing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingEntry != null) "Edit Entry" else "Add New Entry",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (existingEntry != null && onDelete != null) {
                    OutlinedButton(
                        onClick = {
                            onDelete(existingEntry)
                            onDismiss()
                        },
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("delete_entry_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // STEP 1: Choose Expense or Saving (2 Large Navy/White Buttons)
            Text(
                text = "1. What do you want to enter?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EntryType.entries.forEach { type ->
                    val isSelected = selectedType == type
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            width = 2.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clickable { selectedType = type }
                            .testTag("entry_type_tab_${type.name.lowercase()}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (type == EntryType.EXPENSE) "Expense / Bill" else "Saving",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // STEP 2: Enter Amount in PKR (Large Box + Big Quick Add Buttons)
            Text(
                text = "2. Enter Amount ($currencySymbol)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it.filter { ch -> ch.isDigit() || ch == '.' }
                    amountError = false
                },
                label = { Text("Enter $currencySymbol Amount Here") },
                placeholder = { Text("e.g. 2000") },
                textStyle = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                isError = amountError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("entry_amount_input")
            )

            // Big Quick Tap PKR Buttons
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(500, 1000, 5000, 10000).forEach { increment ->
                    Button(
                        onClick = {
                            val current = amountText.toDoubleOrNull() ?: 0.0
                            val updated = current + increment
                            amountText = updated.toInt().toString()
                            amountError = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            text = "+$increment",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                if (amountText.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { amountText = "" },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text("Clear")
                    }
                }
            }

            // STEP 3: Choose Option / Category (Large Easy Cards)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedType == EntryType.EXPENSE) {
                        "3. Select Expense / Bill"
                    } else {
                        "3. Select Saving Type"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = { showAddCategoryDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("open_add_custom_category_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add New Option")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredCategories.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { category ->
                            val isSelected = selectedCategory?.id == category.id
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedCategory = category }
                                    .testTag("category_chip_${category.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) Color.White.copy(alpha = 0.2f)
                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getCategoryIcon(category.iconName),
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // STEP 4: Date & Time (Simple Today / Yesterday / Pick Past Date)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "4. Date & Time (Forgot a day? Pick past date)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = formatFullDateTime(transactionTimestamp),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { transactionTimestamp = System.currentTimeMillis() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isBackdated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (!isBackdated) Color.White else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text("Today")
                        }

                        Button(
                            onClick = {
                                val cal = Calendar.getInstance().apply {
                                    add(Calendar.DAY_OF_YEAR, -1)
                                }
                                transactionTimestamp = cal.timeInMillis
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("backdate_yesterday_chip")
                        ) {
                            Text("Yesterday")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showDatePicker = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("pick_date_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change Date")
                        }

                        Button(
                            onClick = { showTimePicker = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("pick_time_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change Time")
                        }
                    }

                    AnimatedVisibility(visible = isBackdated) {
                        Surface(
                            color = AmberContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = AmberWarning
                                )
                                Text(
                                    text = "Backdated Entry: This will be saved on the past date you selected.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF78350F),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // STEP 5: Optional Short Note
            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                label = { Text("5. Short Note (Optional)") },
                placeholder = {
                    Text(selectedCategory?.name ?: "Write note if needed...")
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("entry_title_input")
            )

            // Huge Navy Blue Save Button
            Button(
                onClick = {
                    val parsedAmount = amountText.toDoubleOrNull()
                    val chosenCat = selectedCategory ?: filteredCategories.firstOrNull()
                    if (parsedAmount == null || parsedAmount <= 0.0 || chosenCat == null) {
                        amountError = true
                    } else {
                        onSave(
                            existingEntry?.id ?: 0L,
                            titleText.ifBlank { chosenCat.name },
                            parsedAmount,
                            selectedType,
                            chosenCat,
                            transactionTimestamp,
                            existingEntry?.paymentMethod ?: "Cash",
                            existingEntry?.notes ?: ""
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("save_entry_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "SAVE ENTRY",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = transactionTimestamp
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        val selectedUtcMillis = datePickerState.selectedDateMillis
                        if (selectedUtcMillis != null) {
                            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = selectedUtcMillis
                            }
                            val localCal = Calendar.getInstance().apply {
                                timeInMillis = transactionTimestamp
                                set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
                                set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
                            }
                            transactionTimestamp = localCal.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Save Date")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val currentCal = Calendar.getInstance().apply { timeInMillis = transactionTimestamp }
        val timePickerState = rememberTimePickerState(
            initialHour = currentCal.get(Calendar.HOUR_OF_DAY),
            initialMinute = currentCal.get(Calendar.MINUTE),
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updatedCal = Calendar.getInstance().apply {
                            timeInMillis = transactionTimestamp
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                            set(Calendar.SECOND, 0)
                        }
                        transactionTimestamp = updatedCal.timeInMillis
                        showTimePicker = false
                    }
                ) {
                    Text("Save Time")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddCategoryDialog) {
        CreateCategoryDialog(
            initialEntryType = selectedType,
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, iconName, colorHex, type ->
                onAddCustomCategory(name, iconName, colorHex, type) { created ->
                    selectedType = type
                    selectedCategory = created
                }
                showAddCategoryDialog = false
            }
        )
    }
}
