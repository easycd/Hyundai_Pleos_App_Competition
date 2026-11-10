# Pleos SDK (AIDL·Gson 사용)
-keep class ai.pleos.** { *; }
-dontwarn ai.pleos.**
# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class com.easycd.nextplace.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.easycd.nextplace.**$$serializer { *; }
# Retrofit
-keepattributes Signature, Exceptions
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
