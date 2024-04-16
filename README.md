![badge][badge-android]
![badge][badge-ios]
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.dilivva/blueline/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dilivva/blueline)

# BlueLine

**BlueLine** is a Kotlin multiplatform library that simplifies Bluetooth printer integration in your applications. It provides a platform-agnostic API for common printer operations across mobile platforms (Android, iOS).

**Key Features:**

* **Effortless Printer Interaction:** Discover, connect, and manage Bluetooth printers with ease.
* **Rich Text Formatting:** Enhance your printouts with options for alignment, font, style, and color.
* **Seamless Image Printing:** Print images directly from your application.
* **Custom Command Support:** Send any printer-specific commands for advanced control.
* **Kotlin multiplatform for mobile:** Develop your app targeting Android and iOS.

## Getting Started

BlueLine is easy to integrate into your Kotlin multiplatform project. Here's a quick guide:

1. **Add BlueLine to your dependencies:**

   ```kotlin
   // In your root build.gradle.kts file
   repositories {
       mavenCentral()
   }
   kotlin{
       sourceSets {
         commonMain.dependencies{
            implementation("com.dilivva:blueline:${bluelineVersion}")
        } 
    }
   }
   ```

2. **Explore the API:**

## Usage Example

Here's a basic example demonstrating how to print some formatted text and an image:

```kotlin
import com.dilivva.blueline.*

fun main() { 
    val blueLine = BlueLine()
    //Monitor Connection state
    val connectionState = bluetoothConnection.connectionState() //StateFlow<ConnectionState>
    //Scan for printers
    blueLine.scanForPrinters()
    //Connect
    blueLine.connect()
   
    //Build print data
    val (printData, imagePreview) = buildPrintData {
        appendImage {
            imageBytes = bytes
        }
        appendText { 
            styledText(data = "Send24", alignment = Config.Alignment.CENTER, font = Config.Font.LARGE_2, style = Config.Style.BOLD)
            textNewLine()
            styledText(data = "================================", alignment =  Config.Alignment.CENTER, style = Config.Style.BOLD)
            textNewLine()
            text("Name: Bob Oscar")
            textNewLine(2)
            text("Phone: +111111111")
            textNewLine(2)
            styledText(data = "Variant:", font = Config.Font.NORMAL, style = Config.Style.BOLD)
            text("HUB_TO_HUB")
        }
    }
    
    //print
    blueLine.print(printData)
}
```

This example demonstrates:

* Connecting to a printer (replace with Blueline's connection logic).
* Printing formatted text with alignment, font, and style options.
* Printing an image from a resource.

## Contributing

We welcome contributions to Blueline!

## License

Blueline is licensed under the MIT License. See the LICENSE file for details.
