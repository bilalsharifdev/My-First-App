package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.EntryType
import com.example.ui.AvailableCategoryIcons
import com.example.ui.theme.CategoryColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateCategoryDialog(
    initialEntryType: EntryType = EntryType.EXPENSE,
    onDismiss: () -> Unit,
    onConfirm: (name: String, iconName: String, colorHex: Long, entryType: EntryType) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(initialEntryType) }
    var selectedIconKey by remember { mutableStateOf(AvailableCategoryIcons.first().key) }
    var selectedColorIndex by remember { mutableIntStateOf(0) }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Custom Category",
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Create custom expense or saving options (e.g., School Van Fee, Solar Bill, Tuition, Gold Saving).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EntryType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.label) },
                            modifier = Modifier.testTag("custom_cat_type_${type.name.lowercase()}")
                        )
                    }
                }

                OutlinedTextField(
                    value = categoryName,
                    onValueChange = {
                        categoryName = it
                        nameError = false
                    },
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g., Children Coaching Fee") },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_category_name_input")
                )

                Text(
                    text = "Choose Icon",
                    style = MaterialTheme.typography.labelLarge
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AvailableCategoryIcons.forEach { option ->
                        val isSelected = selectedIconKey == option.key
                        val chosenColor = CategoryColors[selectedColorIndex]
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) chosenColor.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) chosenColor else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedIconKey = option.key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = option.label,
                                tint = if (isSelected) chosenColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Choose Badge Color",
                    style = MaterialTheme.typography.labelLarge
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryColors.forEachIndexed { index, color ->
                        val isSelected = selectedColorIndex == index
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorIndex = index }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryName.isBlank()) {
                        nameError = true
                    } else {
                        val colorLong = CategoryColors[selectedColorIndex].toArgb().toLong() and 0xFFFFFFFFL
                        onConfirm(categoryName.trim(), selectedIconKey, colorLong, selectedType)
                    }
                },
                modifier = Modifier.testTag("save_custom_category_button")
            ) {
                Text("Add Option")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
