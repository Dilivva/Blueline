[![Kotlin](https://img.shields.io/badge/kotlin-2.1.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/Dilivva/Blueline)](https://github.com/Dilivva/Blueline/releases)
[![Maven Central](https://img.shields.io/maven-central/v/com.dilivva/blueline.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.dilivva%22%20AND%20a:%22blueline%22)

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
            //old -  pre 2.0.0
            implementation("com.dilivva:blueline:${bluelineVersion}")
            //New 2.0.0 Basic 
            implementation("com.dilivva.blueline:basic-builder:${bluelineVersion}")
            //New 2.0.0 Compose 
            implementation("com.dilivva.blueline:compose-builder:${bluelineVersion}")
        } 
    }
   }
   ```

2. **Explore the API:**

## Usage Example

Here's an example demonstrating how to print some formatted text and an image using basic builder:

```kotlin
import com.dilivva.blueline.basic.*

fun main() { 
    val blueLine = BlueLine()
    //Monitor Connection state
    val connectionState = bluetoothConnection.connectionState() //StateFlow<ConnectionState>
    //Scan for printers
    blueLine.scanForPrinters()
    //Connect
    blueLine.connect()
   
    //Build print data
    val result = buildPrintData {
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
    blueLine.print(result.data)
    //preview
    result.preview
}
```

Here's an example using compose builder:

```kotlin
import com.dilivva.blueline.compose.*

fun main() { 
    val blueLine = BlueLine()
    //Monitor Connection state
    val connectionState = bluetoothConnection.connectionState() //StateFlow<ConnectionState>
   val composeBuilder = rememberComposeBuilder() 
    //Scan for printers
    blueLine.scanForPrinters()
    //Connect
    blueLine.connect()
   
    //Use compose multiplatform to draw print data
    composeBuilder.drawContents {
        PreviewPeopleTable()
    }
   
    //Build print data
    val result = composeBuilder.create()
    
    //print
    blueLine.print(result.data)
    //preview
    result.preview
}
```


## Contributing

We welcome contributions to Blueline!

## License

Blueline is licensed under the MIT License. See the LICENSE file for details.


