import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.lineSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberVicoScrollState
import hr.kravarscan.evolution.SolutionInfo
import hr.kravarscan.evolution.format
import hr.kravarscan.evolution.sample1.Sample1Optimizer
import hr.kravarscan.evolution.sample1.Sample1OptimizerBinary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Click me!") }
    var resultVisualisation by remember { mutableStateOf("") }
    var progressVisualisation by remember { mutableStateOf("") }
    var length by remember { mutableStateOf(0) }

    val data = mutableListOf(0.0)
    val model = Sample1OptimizerBinary()
    var bestFit = Double.POSITIVE_INFINITY
    var generations = 0

    val coroutineScope = rememberCoroutineScope()
    val isRunning = MutableStateFlow(false)
    val updates = Channel<SolutionInfo>()

    MaterialTheme {
        val modelProducer = remember { CartesianChartModelProducer() }
        LaunchedEffect(length) {
            modelProducer.runTransaction {
                lineSeries { series(data) }
            }
        }
        Column {
            CartesianChartHost(
                chart =
                    rememberCartesianChart(
                        rememberLineCartesianLayer(),
                        startAxis = VerticalAxis.rememberStart(),
                        bottomAxis = HorizontalAxis.rememberBottom(),
                    ),
                modelProducer = modelProducer,
                scrollState = rememberVicoScrollState(false)
            )
            Button(onClick = {
                isRunning.update { !it }
            }) {
                Text(text)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Dp(4f))) {
                Text(progressVisualisation)
                Text(resultVisualisation)
            }
        }
    }

    coroutineScope.launch {
        isRunning.collect {
            text = if (it)
                "Searching..."
            else
                "Run"

            if (it)
                launch(Dispatchers.IO) {
                    while (isRunning.value) {
                        val result = model.next()
                        val fit = model.eval(result)
                        generations++
                        if (fit < bestFit) {
                            bestFit = fit
                            updates.send(SolutionInfo(
                                model.dump(result),
                                fit
                            ))
                        }
                    }
                }
        }
    }

    coroutineScope.launch {
        updates.consumeEach {
            resultVisualisation = it.description
            if (length > 0)
                data.add(it.error)
            else
                data[0] = it.error
            length = data.size
        }
    }

    coroutineScope.launch {
        while (true) {
            delay(50)
            progressVisualisation = "Generation: $generations\nError: ${bestFit.format(3)}"
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
