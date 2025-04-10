
-keep class com.hatlas.Model.** { *; }
-keepclassmembers class com.hatlas.Model.** {
    public *;
}

# Mantém as classes e métodos usados por Retrofit e Gson
-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes Signature,*Annotation*,EnclosingMethod
# Para Retrofit (mantém modelos e anotações)
-keep class retrofit2.** { *; }
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn javax.annotation.**

# Para Dagger
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-dontwarn dagger.**
-dontwarn javax.annotation.**

# Para OkHttp e AuthInterceptor
-keep class okhttp3.** { *; }
-keep class okhttp3.internal.** { *; }
-dontwarn okhttp3.**
-keep class com.hatlas.data.remote.NetworkClient { *; }
# Para Mapbox API
-keep class com.mapbox.** { *; }
-dontwarn com.mapbox.**

# Para Jetpack Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Mantém as funções usadas por sua APIf
-keepclassmembers class com.hatlas.data.remote.** {
    *;
}
-keep class com.hatlas.Model.Login { *; }
-keepattributes *Annotation*
-keep class com.hatlas.data.repository.LoginResult { *; }
-keep interface com.hatlas.data.remote.ApiService { *; }
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Garante que os tipos usados em serialização não sejam obfuscados
-keepclassmembers class * {
    ** genericSignature;
}

# Para evitar que o Jackson quebre
-keepattributes *Annotation*, Signature
-keepclassmembers class com.hatlas.** {
    *;
}
-keepclassmembers class * {
    @com.fasterxml.jackson.annotation.* <fields>;
    @com.fasterxml.jackson.annotation.* <methods>;
}

# Evita remoção de classes usadas pelo Jackson
-keep class com.fasterxml.** { *; }
-dontwarn com.fasterxml.jackson.databind.**

-keepclassmembers class * {
    ** genericSignature;
}
-keep class com.hatlas.** { *; }
-keep class com.fasterxml.jackson.** { *; }

-keep class com.fasterxml.jackson.databind.** { *; }
-keep class com.fasterxml.jackson.core.** { *; }
-keep class com.fasterxml.jackson.annotation.** { *; }
-dontwarn com.fasterxml.jackson.core.**
-dontwarn com.fasterxml.jackson.annotation.**

# Jackson core
-keep class com.fasterxml.jackson.databind.** { *; }
-keep class com.fasterxml.jackson.annotation.** { *; }
-keep class com.fasterxml.jackson.core.** { *; }
-dontwarn com.fasterxml.jackson.**

-keep class com.fasterxml.** { *; }
-keep class org.codehaus.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keep class * extends com.fasterxml.jackson.core.type.TypeReference


# General
-keepattributes SourceFile,LineNumberTable,*Annotation*,EnclosingMethod,Signature,Exceptions,InnerClasses

-keepnames @interface com.fasterxml.jackson.** { *; }

-keepclassmembernames class com.fasterxml.jackson.** { *; }

-keepclassmembers, allowobfuscation class * {
    @com.fasterxml.jackson.annotation.JsonProperty *;
}


-dontwarn com.ctc.wstx.**
-dontwarn org.codehaus.stax2.**