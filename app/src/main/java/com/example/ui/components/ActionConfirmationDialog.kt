package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.actions.ConfirmationRequest
import com.example.actions.RiskLevel

@Composable
fun ActionConfirmationDialog(
    request: ConfirmationRequest,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val riskColor = when (request.riskLevel) {
        RiskLevel.LOW -> Color(0xFF10B981)
        RiskLevel.MEDIUM -> Color(0xFFF59E0B)
        RiskLevel.HIGH -> Color(0xFFEF4444)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("action_confirmation_dialog"),
        containerColor = Color(0xFF0F172A),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(riskColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = request.riskLevel.name,
                        color = riskColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column {
                Text(
                    text = request.message,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "MYRAA requires explicit approval for actions with safety impact.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (request.riskLevel == RiskLevel.HIGH) Color(0xFFEF4444) else Color(0xFF00E5FF)
                ),
                modifier = Modifier.testTag("confirm_action_button")
            ) {
                Text(
                    text = "Confirm & Execute",
                    color = if (request.riskLevel == RiskLevel.HIGH) Color.White else Color(0xFF020617),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_action_button")
            ) {
                Text(
                    text = "Cancel",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    )
}
