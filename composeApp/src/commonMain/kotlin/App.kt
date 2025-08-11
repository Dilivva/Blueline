import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dilivva.blueline.basic.builder.buildPrintData
import com.dilivva.blueline.compose.rememberComposeBuilder
import com.dilivva.blueline.core.commands.Config
import com.dilivva.blueline.core.connection.bluetooth.BlueLine
import com.dilivva.blueline.core.connection.bluetooth.ConnectionError
import com.dilivva.blueline.core.connection.bluetooth.ConnectionState
import com.dilivva.blueline.core.result.PrintDataResult
import escposprinter.composeapp.generated.resources.Res
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        val bluetoothConnection = remember { BlueLine() }
        val connectionState by bluetoothConnection.connectionState().collectAsState()
        val scope = rememberCoroutineScope()
        var message  by remember { mutableStateOf("") }
        var showDialog  by remember { mutableStateOf(false) }
        var scanCount by remember { mutableStateOf(0) }


        LaunchedEffect(Unit){
            if (!showContent) showContent = true
        }

        LaunchedEffect(connectionState.bluetoothConnectionError){
            message = when(connectionState.bluetoothConnectionError){
                ConnectionError.BLUETOOTH_DISABLED -> "Enable bluetooth on device and click retry"
                ConnectionError.BLUETOOTH_PERMISSION -> "Switch on location access and click retry"
                ConnectionError.BLUETOOTH_NOT_SUPPORTED -> "Bluetooth is not supported on this device"
                null -> ""
                ConnectionError.BLUETOOTH_PRINT_ERROR -> "Error while printing"
                ConnectionError.BLUETOOTH_PRINTER_DEVICE_NOT_FOUND -> "No printer found"
                ConnectionError.BLUETOOTH_SCAN_FAILED -> "An error occured during scanning"
                ConnectionError.BLUETOOTH_ADAPTER_ERROR -> "Bluetooth adapter has a problem"
            }
            showDialog = message.isNotEmpty()
        }
        LaunchedEffect(connectionState){
//            if (connectionState.isBluetoothReady && !connectionState.discoveredPrinter && !connectionState.isScanning && scanCount == 0){
//                scanCount++
//                scope.launch {
//                    bluetoothConnection.scanForPrinters()
//                }
//                //println("Scanning")
//            }
//            if (connectionState.discoveredPrinter && !connectionState.isConnected){
//                bluetoothConnection.connect()
//                println("Connecting")
//            }
//            if (connectionState.isPrinting){
//                //printingStarted = true
//                println("Printing")
//            }
        }

        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopAppBar(modifier = Modifier.fillMaxWidth(), title = { Text("Discovered Bluetooth devices") })
            AnimatedVisibility(showContent) {
                ConnectionItem(bluetoothConnection, connectionState, scope)
            }
        }

        if (showDialog){
            ShowDialog(message){
                showDialog = false
                if (connectionState.bluetoothConnectionError == ConnectionError.BLUETOOTH_DISABLED) {
                    bluetoothConnection.init()
                }
            }
        }


    }
}

@Composable
fun ShowDialog(
    message: String,
    onDismiss: () -> Unit
){
    AlertDialog(
        modifier = Modifier.fillMaxWidth(),
        title = { Text("Error") },
        text = { Text(message) },
        onDismissRequest = onDismiss,
        buttons = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = onDismiss) {
                    Text("Retry")
                }
            }
        },
        shape = RoundedCornerShape(15.dp)
    )
}



@Composable
fun ConnectionItem(
    connection: BlueLine,
    connectionState: ConnectionState,
    scope: CoroutineScope
){

    var image by remember { mutableStateOf<ImageBitmap?>(null) }
    var imageBytes by remember { mutableStateOf(byteArrayOf()) }
    val composeBuilder = rememberComposeBuilder()
    val discoveredDevices = connectionState.discoveredDevices

    LaunchedEffect(Unit){
        val bytes = Res.readBytes("drawable/label.png")
        imageBytes = bytes
        //image = getPlatform().toImage(bytes)
    }

   Box(
       modifier = Modifier.fillMaxWidth().padding(20.dp)
       .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(15.dp))
       .padding(8.dp)
   ){
       Column(modifier = Modifier.fillMaxWidth()){
           Text(
               text =
                    "ConnectedDeviceName: ${connectionState.connectedDevice?.name}\n" +
                    "ConnectedDeviceAddress: ${connectionState.connectedDevice?.address}\n" +
                    "discoveredPrinter: ${connectionState.discoveredPrinter}\n" +
                    "canPrint: ${connectionState.canPrint}\n" +
                    "isConnected: ${connectionState.isConnected}\n" +
                    "isBluetoothReady: ${connectionState.isBluetoothReady}\n" +
                    "bluetoothConnectionError: ${connectionState.bluetoothConnectionError?.name}\n" +
                    "isPrinting: ${connectionState.isPrinting}\n" +
                    "isScanning: ${connectionState.isScanning}\n" +
                    "isConnecting: ${connectionState.isConnecting}\n",
               fontSize = 14.sp,
               color = Color.White,
           )

           if(discoveredDevices.isEmpty()){
               Text(
                   text = "No devices found",
                   fontSize = 14.sp,
                   color = Color.White,
               )
           }

           discoveredDevices.forEach {
               Text(
                   text = "${it.value.name} - ${it.value.address}",
                   fontSize = 14.sp,
                   color = Color.White,
               )
           }

           Row(
               modifier = Modifier.fillMaxWidth(),
               horizontalArrangement = Arrangement.spacedBy(10.dp),
               verticalAlignment = Alignment.CenterVertically
           ) {

               Button(
                   onClick = { scope.launch(Dispatchers.IO) { connection.scanForPrinters() } },
                   enabled = connectionState.isBluetoothReady && !connectionState.discoveredPrinter && !connectionState.isScanning
               ) {
                   Text("Scan for printers")
               }
               if (connectionState.isScanning) {
                   CircularProgressIndicator()
               }
           }

           Button(
               onClick = {
                   val firstAddress = discoveredDevices.entries.first()
                   connection.connect(firstAddress.value.address)
               },
               enabled = !connectionState.isConnected && connectionState.discoveredPrinter
           ){
               Text("Connect")
               if (connectionState.isConnecting) {
                   CircularProgressIndicator(
                       color = Color.White
                   )
               }
           }

           Button(
               onClick = {
                   connection.disconnect()
               },
               enabled = connectionState.isConnected
           ){
               Text("Disconnect")
           }

           Row(
               modifier = Modifier.fillMaxWidth(),
               horizontalArrangement = Arrangement.spacedBy(10.dp),
               verticalAlignment = Alignment.CenterVertically
           ){
               Button(
                   onClick = {
                       scope.launch {
                           val printData = composeBuilder.create()
                           printData.preview?.let {
                               image = getPlatform().toImage(it)
                           }
                           //connection.print(printData.data)
                       }

                       //connection.print(printData.data)
                   },
                   enabled = true//connectionState.canPrint && connectionState.isConnected && !connectionState.isPrinting
               ){
                   Text("Print")
               }
               if (connectionState.isPrinting) {
                   CircularProgressIndicator()
               }
           }
           image?.let {
               Image(
                   bitmap = it,
                   contentDescription = null,
                   //contentScale = ContentScale.FillWidth,
                   modifier = Modifier.fillMaxWidth()
               )
           }
           composeBuilder.drawContents {
               PreviewPeopleTable()
           }

       }
   }
}







private fun textPrint(bytes: ByteArray): PrintDataResult {
    return buildPrintData {
        appendImage {
            imageBytes = bytes
        }
        appendText {
            styledText(data = "Send24", alignment = Config.Alignment.CENTER, fontSize = Config.FontSize.LARGE_2, style = Config.Style.BOLD)
            textNewLine()
            styledText(data = "================================", alignment =  Config.Alignment.CENTER, style = Config.Style.BOLD)
            textNewLine()
            text("Name: Juliette Gannon")
            textNewLine(2)
            text("Phone: 07033879645")
            textNewLine(2)
            styledText(data = "Variant:", fontSize = Config.FontSize.NORMAL, style = Config.Style.BOLD)
            text("HUB_TO_HUB")
        }
    }
}