package com.example.ui.screens.students.portal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.ReceiptRow
import com.example.ui.theme.*

/** Pure presentation of the existing verified payment controller; no settlement logic. */
@Composable fun StudentLightPaymentDialog(studentName:String,amountDue:Long,isProcessing:Boolean,paymentCompleted:Boolean,razorpayPaymentId:String,onDismiss:()->Unit,onStartRazorpay:(String)->Unit,receiptNo:String,statusMessage:String,onCheckStatus:(()->Unit)?){
 val interrupted=statusMessage.contains("cancel",true)||statusMessage.contains("failed",true)
 Dialog(onDismissRequest={if(!isProcessing)onDismiss()},properties=DialogProperties(usePlatformDefaultWidth=false)){
  Surface(Modifier.fillMaxWidth().padding(StudentLightLayout.margin).heightIn(max=StudentDarkLayout.viewportMax),color=StudentLightPalette.canvas,shape=MaterialTheme.shapes.large){
   Column(Modifier.verticalScroll(rememberScrollState()).padding(Spacing.lg16),verticalArrangement=Arrangement.spacedBy(Spacing.lg16)){
    Text(if(paymentCompleted)"Receipt"else "Payment status",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
    StudentCard(tone=StudentTone.BLUE){Column(Modifier.fillMaxWidth().padding(Spacing.lg16),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(Spacing.md12)){
     Icon(if(paymentCompleted)Icons.Outlined.CheckCircle else if(interrupted)Icons.Outlined.ErrorOutline else Icons.Outlined.Schedule,null,tint=StudentLightPalette.navy)
     Text(if(amountDue>0||paymentCompleted)studentMoney(amountDue)else "Amount not loaded",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold)
     Text(if(paymentCompleted)"Paid · Server confirmed"else if(interrupted)"Payment interrupted"else if(isProcessing)"Awaiting provider confirmation"else "Ready for secure checkout",style=MaterialTheme.typography.titleMedium)
     Text(if(paymentCompleted)"Test mode"else statusMessage.ifBlank{"Your balance changes only after server verification."},style=MaterialTheme.typography.bodySmall)
    }}
    if(paymentCompleted){
     ReceiptRow("Receipt",receiptNo.ifBlank{"Not returned"});ReceiptRow("Payment",razorpayPaymentId);ReceiptRow("Student",studentName)
     StudentAction("Done",onClick=onDismiss)
    }else if(isProcessing){LinearProgressIndicator(Modifier.fillMaxWidth());Text("Please wait for confirmation. Do not start another payment.",style=MaterialTheme.typography.bodySmall)}else{
     if(!interrupted&&amountDue>0)Box(Modifier.testTag("submit_fee_payment_button")){StudentAction("Pay ${studentMoney(amountDue)}"){onStartRazorpay("Razorpay Test")}}
     onCheckStatus?.let{check->StudentAction("Check status",onClick=check)}
     OutlinedButton(onClick=onDismiss,modifier=Modifier.fillMaxWidth().heightIn(min=StudentLightLayout.accountAvatar)){Text("Back to invoice")}
     Text("Razorpay TEST mode. No payment is confirmed by this screen alone.",style=MaterialTheme.typography.bodySmall,color=StudentLightPalette.muted)
    }
   }
  }
 }
}