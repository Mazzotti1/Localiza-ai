
-keep class com.hatlas.Model.** { *; }
-keepclassmembers class com.hatlas.Model.** {
    public *;
}

# Mantém as classes e métodos usados por Retrofit e Gson
-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations

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

# Mantém as funções usadas por sua API
-keep class com.hatlas.Model.** { *; }
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