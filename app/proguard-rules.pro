
-keepclassmembers class com.cars24.sdui.schema.** {
    *** Companion;
}
-keepclasseswithmembers class com.cars24.sdui.schema.** {
    kotlinx.serialization.KSerializer serializer(...);
}

