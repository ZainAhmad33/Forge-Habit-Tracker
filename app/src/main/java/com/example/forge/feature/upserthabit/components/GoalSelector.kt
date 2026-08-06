package com.example.forge.feature.upserthabit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.designsystem.component.CounterInput
import com.example.forge.core.designsystem.theme.ForgeTheme

@Composable
fun GoalSelector(
    goal: Int,
    onGoalChange: (Int) -> Unit,
    showUnit: Boolean,
    unit: String,
    availableUnits: List<String>,
    onUnitSelected: (String) -> Unit,
    otherUnitInput: String,
    onOtherUnitInputChange: (String) -> Unit,
    showCounter: Boolean,
    modifier: Modifier = Modifier,
    isOtherUnitError: Boolean = false,
    habitTypeSelected: HabitType
) {
    val focusManager = LocalFocusManager.current
    var showOtherUnitInput by remember {mutableStateOf(false)}
    Column(
        verticalArrangement = Arrangement.SpaceBetween
    ){
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if(showCounter){
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily target",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CounterInput(
                        value = goal,
                        onValueChange = onGoalChange,
                        valueMin = 1
                    )
                }
            }


            // Unit selector (if applicable)
            if (showUnit) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Unit",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    UnitPicker(
                        selectedUnit = unit,
                        availableUnits = availableUnits,
                        onUnitSelected = onUnitSelected,
                        onShowOtherUnitInput = { showOtherUnitInput = it}
                    )
                }
            }
        }

        if(showOtherUnitInput && habitTypeSelected == HabitType.Quantity){
            OutlinedTextField(
                value = otherUnitInput,
                onValueChange = onOtherUnitInputChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                placeholder = { Text("Unit name") },
                shape = RoundedCornerShape(12.dp),
                isError = isOtherUnitError,
                supportingText = if (isOtherUnitError) {
                    { Text("Please enter a unit name") }
                } else null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
        else{
            onOtherUnitInputChange("")
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitPicker(
    selectedUnit: String,
    availableUnits: List<String>,
    onUnitSelected: (String) -> Unit,
    onShowOtherUnitInput: (Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedUnit,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            availableUnits.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(text = unit) },
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (unit == "Other") {
                            onUnitSelected("Other")
                            onShowOtherUnitInput(true)
                        } else {
                            onUnitSelected(unit)
                            onShowOtherUnitInput(false)
                        }
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GoalSelectorPreview() {
    ForgeTheme {
        GoalSelector(
            goal = 3,
            onGoalChange = {},
            showUnit = true,
            showCounter = true,
            unit = "Liters",
            availableUnits = listOf("Liters", "Minutes", "Hours", "Pages"),
            onUnitSelected = {},
            otherUnitInput = "",
            onOtherUnitInputChange = {},
            modifier = Modifier.padding(16.dp),
            habitTypeSelected = HabitType.YesNo
        )
    }
}
