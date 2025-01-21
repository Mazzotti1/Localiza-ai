# Regras gerais
# Manter as classes do Retrofit
-keep class com.squareup.retrofit2.** { *; }
-keepclassmembers class * {
    @com.squareup.retrofit2.* <methods>;
}

# Manter as classes do Gson (caso esteja usando Gson com Retrofit)
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Manter as classes do Dagger
-keep class dagger.** { *; }
-keepclassmembers class * {
    @dagger.** <methods>;
}

# Manter as classes do ViewModel
-keep class androidx.lifecycle.viewmodel.** { *; }

# Manter as classes do Lifecycle (viewmodel, livedata, etc)
-keep class androidx.lifecycle.** { *; }

# Manter as classes da biblioteca de navegação do Compose
-keep class androidx.navigation.compose.** { *; }

# Manter as classes do JWT (com.auth0)
-keep class com.auth0.jwt.** { *; }

# Manter as classes do Maps e Maps Compose
-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.maps.android.** { *; }
-keep class com.google.android.gms.location.** { *; }

# Manter as classes do Accompanist Permissions
-keep class com.google.accompanist.permissions.** { *; }

# Manter as classes de anotação do Retrofit
-keep @interface com.squareup.retrofit2.* { *; }

# Manter a classe do Dotenv Kotlin (para variáveis de ambiente)
-keep class io.github.cdimascio.dotenvk.** { *; }

# Manter as classes do Compose
-keep class androidx.compose.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Manter as classes do Guava
-keep class com.google.common.** { *; }

# Manter as classes de SplashScreen
-keep class androidx.core.splashscreen.** { *; }

# Manter as classes do Google Maps Utils
-keep class com.google.maps.android.utils.** { *; }

# Permitir que o ProGuard preserve os arquivos de fontes, úteis para fontes customizadas
-keepclassmembers class * {
    @androidx.compose.ui.text.font.FontFamily <methods>;
}

# Permitir a preservação de atributos de fontes
-keepattributes *Annotation*

# Permitir os métodos e construtores da classe para otimização
-keep class * extends android.app.Activity { public void *(android.view.View); }
-keepclassmembers class * extends android.app.Activity { public void *(android.view.View); }

# Se estiver usando anotações para injeção de dependências, preserve as anotações
-keep @interface javax.inject.** { *; }
-keepclassmembers class * {
    @javax.inject.Inject <methods>;
}

# Se o aplicativo usa WebView com JS, adicione regras para o WebView
# -keepclassmembers class fqcn.of.javascript.interface.for.webview {
#     public *;
# }
