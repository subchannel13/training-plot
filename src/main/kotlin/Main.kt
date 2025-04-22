import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
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
import hr.kravarscan.evolution.JExample
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Click me!") }
    var length by remember { mutableStateOf(0) }
    val data = mutableListOf(0)
    val coroutineScope = rememberCoroutineScope()

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
                text = "Running"
                coroutineScope.launch(Dispatchers.IO) {
                    val model = JExample()
                    data.clear()
                    for (i in 2..100) {
                        //delay(300)

                        val result = model.nextFit()
                        data.add(result)
                        length = data.size
                    }

                    text = "Click me!"
                }
            }) {
                Text(text)
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
