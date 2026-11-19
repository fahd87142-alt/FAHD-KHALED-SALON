package com.fahdkhaled.salon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    private var user by mutableStateOf<FirebaseUser?>(null)
    private var phone by mutableStateOf("+20")
    private var code by mutableStateOf("")
    private var verificationId by mutableStateOf<String?>(null)
    private var codeSent by mutableStateOf(false)
    private var loading by mutableStateOf(false)
    private var message by mutableStateOf("أدخل رقم هاتفك للمتابعة")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        setContent {
            SalonScreen()
        }
    }

    private fun sendCode() {
        if (phone.length < 10 || !phone.startsWith("+")) {
            message = "اكتب رقم الهاتف بالصيغة الدولية"
            return
        }

        loading = true
        message = "جاري إرسال كود التحقق..."

        val callbacks =
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(
                    credential: PhoneAuthCredential
                ) {
                    login(credential)
                }

                override fun onVerificationFailed(
                    e: FirebaseException
                ) {
                    loading = false
                    message = e.localizedMessage ?: "تعذر إرسال الكود"
                }

                override fun onCodeSent(
                    id: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    loading = false
                    verificationId = id
                    codeSent = true
                    message = "تم إرسال كود التحقق لهاتفك"
                }
            }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode() {
        val id = verificationId

        if (id == null || code.length != 6) {
            message = "اكتب كود التحقق المكون من 6 أرقام"
            return
        }

        loading = true
        message = "جاري التأكد من الكود..."

        val credential = PhoneAuthProvider.getCredential(id, code)
        login(credential)
    }

    private fun login(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { result ->

                loading = false

                if (result.isSuccessful) {
                    user = auth.currentUser
                    message = "تم تسجيل الدخول بنجاح ❤️"
                } else {
                    message = result.exception?.localizedMessage
                        ?: "فشل تسجيل الدخول"
                }
            }
    }

    @Composable
    private fun SalonScreen() {
        MaterialTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Text(
                        text = "F H",
                        color = Color.White,
                        fontSize = 55.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "FAHD KHALED SALON",
                        color = Color.White,
                        fontSize = 24.sp
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (user != null) {

                        Text(
                            text = "أهلاً بيك ❤️",
                            color = Color.White,
                            fontSize = 22.sp
                        )

                        Text(
                            text = user?.phoneNumber ?: "",
                            color = Color.LightGray
                        )

                    } else {

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("رقم الهاتف") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(15.dp))

                        if (codeSent) {
                            OutlinedTextField(
                                value = code,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                        code = it
                                    }
                                },
                                label = { Text("كود التحقق") },
                                keyboardOptions = KeyboardOptions(
                                   
