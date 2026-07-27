package com.example.habitz.feature.upserthabit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habitz.core.designsystem.component.CounterInput
import com.example.habitz.core.designsystem.theme.HabitzTheme
import com.example.habitz.feature.upserthabit.HabitType

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
    modifier: Modifier = Modifier
) {
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

        if(showOtherUnitInput){
            OutlinedTextField(
                value = otherUnitInput,
                onValueChange = onOtherUnitInputChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 16.dp),
                placeholder = { Text("Unit") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
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
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
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
                        if (unit == "Other...") {
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
    HabitzTheme {
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
            modifier = Modifier.padding(16.dp)
        )
    }
}
