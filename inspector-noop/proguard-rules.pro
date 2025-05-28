-dontobfuscate
-keep class com.uhufor.** { *; }
-keep interface com.uhufor.** { *; }

-keep class java.lang.invoke.StringConcatFactory { *; }
-dontwarn java.lang.invoke.StringConcatFactory
