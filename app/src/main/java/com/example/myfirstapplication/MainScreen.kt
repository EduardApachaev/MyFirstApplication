package com.example.myfirstapplication

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun MainScreen(
    modifier: Modifier,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    var selectedDispatcher by remember { mutableStateOf("Default") }
    var sliderPosition by remember { mutableFloatStateOf(10f) }
    var isSequential by remember { mutableStateOf(true) }
    var isParallel by remember { mutableStateOf(false) }
    var isLazy by remember { mutableStateOf(false) }
    val intValue = sliderPosition.roundToInt()
    var isRunning by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var parentJob by remember { mutableStateOf<Job?>(null) }

    Column (modifier = modifier
        .padding(16.dp)
        .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            stringResource(R.string.coroutine_num_txt) + intValue,
            modifier = Modifier
                .fillMaxWidth()
        )
        Row {
            Slider(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                valueRange = Constants.MIN_COROUTINES..Constants.MAX_COROUTINES,
                steps = 17
            )
        }

        var expanded by remember { mutableStateOf(false) }
        Row {
            Text(
                text = stringResource(R.string.selected_dispather_txt) + selectedDispatcher,
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { expanded = true }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {expanded = !expanded},
                content = {
                    Constants.DISPATCHER_TYPES.forEach { dispatcher ->
                        DropdownMenuItem(
                            onClick = {
                                selectedDispatcher = dispatcher
                                expanded = false
                            },
                            text = { Text(dispatcher) }
                        )
                    }
                }
            )
        }

        Row(
            modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.sequential_execution_txt),
                modifier = Modifier.padding(8.dp, 0.dp)
            )
            Switch(
                checked = isSequential,
                onCheckedChange = {
                    isSequential = it
                    isParallel = !it}
            )
        }

        Row (
            modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.parallel_execution_txt),
                modifier = Modifier.padding(8.dp, 0.dp))
            Switch(
                checked = isParallel,
                onCheckedChange = {
                    isParallel = it
                    isSequential = !it}
            )
        }

        Row (
            modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.lazy_execution_txt),
                modifier = Modifier.padding(8.dp, 0.dp))
            Switch(
                checked = isLazy,
                onCheckedChange = { isLazy = it }
            )
        }

        if (isRunning) {
            CircularProgressIndicator()

            Button(
                onClick = {
                    val activeChildren = parentJob?.children?.filter { it.isActive }?.toList() ?: emptyList()
                    val cancelledCount = activeChildren.size

                    activeChildren.forEach { it.cancel() }

                    showToast(context, "Выполнено корутин: $cancelledCount", coroutineScope)

                    parentJob = null
                    isRunning = false
                }
            ) { Text(stringResource(R.string.cancel_coroutines_btn)) }
        } else {
            Button(
                onClick = {
                    isRunning = true
                    parentJob = startCoroutines(
                        coroutineScope,
                        intValue,
                        selectedDispatcher,
                        isSequential,
                        isLazy,
                        onComplete = { isRunning = false },
                        onToast = {message -> showToast(context, message, coroutineScope)},
                        onSnackbar = {message -> showSnackbar(snackbarHostState, message, coroutineScope)},
                        onReset = {
                            selectedDispatcher = Constants.DEFAULT_DISPATCHER
                            sliderPosition = Constants.MIN_COROUTINES
                            isSequential = true
                            isParallel = false
                            isLazy = false
                        }
                    )
                }
            ) { Text(stringResource(R.string.run_coroutines_btn)) }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

private fun showToast(context: Context, message: String, scope: CoroutineScope) {
    scope.launch(Dispatchers.Main) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

private fun showSnackbar(snackbarHostState: SnackbarHostState, message: String, scope: CoroutineScope) {
    scope.launch {
        snackbarHostState.showSnackbar(message)
    }
}

fun startCoroutines(
    scope: CoroutineScope,
    count: Int,
    selectedDispatcher: String,
    isSequential: Boolean,
    isLazy: Boolean,
    onComplete: () -> Unit,
    onSnackbar: (String) -> Unit,
    onToast: (String) -> Unit,
    onReset: () -> Unit
) :Job {
    val dispatcher = when (selectedDispatcher) {
        "IO" -> Dispatchers.IO
        "Main" -> Dispatchers.Main
        "Unconfined" -> Dispatchers.Unconfined
        else -> Dispatchers.Default
    }
    val parentJob = scope.launch {
        if (isLazy) {
            val lazyJobs = mutableListOf<Job>()

            if (isSequential) {
                repeat(count) {
                    val lazyJob = launch(dispatcher, CoroutineStart.LAZY) {
                        executeCoroutine(onToast, onSnackbar,onReset)
                    }
                    lazyJobs.add(lazyJob)
                }
                lazyJobs.forEach {
                    it.start()
                    it.join()
                }
            } else {
                repeat(count) {
                    val lazyJob = launch (dispatcher,CoroutineStart.LAZY) {
                        executeCoroutine(onToast, onSnackbar,onReset)
                    }
                    lazyJobs.add(lazyJob)
                }
                lazyJobs.forEach { it.start() }
                lazyJobs.joinAll()
            }
        } else {
            if (isSequential) {
                repeat(count) {
                    val job = launch(dispatcher) {
                        executeCoroutine(onToast, onSnackbar,onReset)
                    }
                    job.join()
                }
            } else {
                val jobs = mutableListOf<Job>()
                repeat(count) {
                    val job = launch (dispatcher) {
                        executeCoroutine(onToast, onSnackbar,onReset)
                    }
                    jobs.add(job)
                }
                jobs.joinAll()
            }
        }
    }
    scope.launch {
        parentJob.join()
        onComplete()
    }
    return parentJob
}

private suspend fun executeCoroutine(
    onToast: (String) -> Unit,
    onSnackbar: (String) -> Unit,
    onReset: () -> Unit
) {
    try {
        coroutineTask()
    } catch (e: CancellationException) {
        throw e
    } catch (e: ToastException) {
        onToast(e.message ?: "Ошибка toast")
    } catch (e: SnackbarException) {
        onSnackbar(e.message ?: "Ошибка snackbar")
    } catch (e: ResetException) {
        onReset()
    }
}

suspend fun coroutineTask() {
    val delayTime = Random.nextLong(1000, 10001)
    delay(delayTime)

    if (delayTime >= 7000 && Random.nextFloat() < 0.3f) {
        when (Random.nextInt(1, 4)) {
            1 -> throw ToastException("Ошибка toast")
            2 -> throw SnackbarException("Ошибка snack")
            3 -> throw ResetException()
        }
    }
}

class ToastException(message: String) : Exception(message)
class SnackbarException(message: String) : Exception(message)
class ResetException() : Exception()