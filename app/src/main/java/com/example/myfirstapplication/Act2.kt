package com.example.myfirstapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myfirstapplication.ui.theme.MyFirstApplicationTheme

class Act2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyFirstApplicationTheme {
                Screen2(intent.getStringExtra(Constants.MESSAGE))
            }
        }
    }
}
@Composable
fun Screen2(text: String?) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when {
                text.isNullOrBlank() -> stringResource(R.string.screen_default)
                else -> text
            }
        )

        Button(
            modifier = Modifier.padding(4.dp),
            onClick = {
                val intent: Intent = Intent(context, Act3::class.java)
                    .putExtra(Constants.MESSAGE, text)
                context.startActivity(intent)
            }
        ) {
            Text(text = stringResource(R.string.bt_to_screen3))
        }

        Button(
            modifier = Modifier.padding(4.dp),
            onClick = {
                val intent: Intent = Intent(context, Act1::class.java)
                    .putExtra(Constants.MESSAGE, text)
                context.startActivity(intent)
            }
        ) {
            Text(text = stringResource(R.string.bt_to_screen1))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview2() {
    MyFirstApplicationTheme {
        Screen2("")
    }
}