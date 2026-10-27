package com.example.ui.screens.students.portal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.ReceiptRow
import com.example.ui.theme.*

/** Uses the existing payment controller's verified state and callbacks verbatim. */
@Composable fun StudentDarkPaymentDialog(studentName:String,amountDue:Long,isProcessing:Boolean,paymentCompleted:Boolean,razorpayPaymentId:String,onDismiss:()->Unit,onStartRazorpay:(String)->Unit,receiptNo:String,statusMessage:String,onCheckStatus:(()->Unit)?) {
 val interrupted=statusMessage.contains("cancel",true)||statusMessage.contains("failed",true)
 Dialog(onDismissRequest={if(!isProcessing)onDismiss()},properties=DialogProperties(usePlatformDefaultWidth=false)){
  Surface(Modifier.fillMaxWidth().padding(StudentLayout.margin).heightIn(max=StudentDarkLayout.viewportMax),color=StudentDarkPalette.canvas,shape=androidx.compose.foundation.shape.RoundedCornerShape(StudentDarkPalette.radius)){
   Column(Modifier.verticalScroll(rememberScrollState()).padding(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.section)){
    Text(if(paymentCompleted)"RECEIPT"else "PAYMENT STATUS",style=MaterialTheme.typography.labelSmall,color=StudentDarkPalette.muted)
    Text(if(paymentCompleted)"Paid. And recorded."else if(isProcessing)"We’re checking your payment."else if(interrupted)"You can try again."else "Your payment, clearly.",style=MaterialTheme.typography.headlineLarge)
    StudentDarkFeature(if(amountDue>0L||paymentCompleted)studentMoney(amountDue)else "Amount not loaded",if(paymentCompleted)"Paid · Test mode"else studentName,if(paymentCompleted)StudentTone.MINT else if(interrupted)StudentTone.ORANGE else StudentTone.BLUE,if(paymentCompleted)Icons.Outlined.CheckCircle else StudentIcons.Document)
    StudentCard{Column(Modifier.padding(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){
     if(paymentCompleted){ReceiptRow("Receipt",receiptNo);ReceiptRow("Payment",razorpayPaymentId);ReceiptRow("Student",studentName);Text("Captured · verified (test)",style=MaterialTheme.typography.titleSmall)}
     else{Text(if(isProcessing)"Awaiting provider confirmation"else "Server confirmation required",style=MaterialTheme.typography.titleMedium);Text(statusMessage.ifBlank{"Your balance updates after verification. A cancellation alone cannot confirm whether a payment was captured."},style=MaterialTheme.typography.bodySmall,color=StudentDarkPalette.muted)}
    }}
    if(isProcessing)LinearProgressIndicator(Modifier.fillMaxWidth())
    else if(paymentCompleted)StudentAction("Done",onClick=onDismiss)
    else{
     if(amountDue>0L)StudentAction("Pay ${studentMoney(amountDue)}"){onStartRazorpay("Razorpay Test")}
     onCheckStatus?.let{check->StudentAction("Check status",onClick=check)}
     OutlinedButton(onClick=onDismiss,modifier=Modifier.fillMaxWidth()){Text("Back to invoice")}
    }
   }
  }
 }
}
