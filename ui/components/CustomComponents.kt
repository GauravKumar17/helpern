package com.example.helpern2.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.helpern2.ui.theme.StarYellow

@Composable
fun CustomRatingBar(
    rating: Double,
    onRatingChanged: (Double) -> Unit = {},
    clickable: Boolean = false,
    maxRating: Int = 5
) {
    Row {
        repeat(maxRating) { index ->
            val starIndex = index + 1
            // Small epsilon (0.1) prevents rounding errors from making a 4.0 star look empty
            val isFilled = starIndex <= (rating + 0.1)
            
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Rate $starIndex stars",
                // Use bright yellow for filled, and a very subtle gray for empty
                tint = if (isFilled) StarYellow else Color.Gray.copy(alpha = 0.2f),
                modifier = Modifier
                    .size(32.dp) // Larger size for visibility
                    .padding(2.dp)
                    .then(
                        if (clickable) {
                            Modifier.clickable { onRatingChanged(starIndex.toDouble()) }
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        isError = isError,
        singleLine = true
    )
}
