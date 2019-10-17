-dontobfuscate
-allowaccessmodification

-keepclasseswithmembers class * {
  public static void main(...);
}

-keepattributes SourceFile, LineNumberTable

# APK signer uses reflection and annotations to parse signatures in Asn1BerParser.
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault
-keep @com.android.apksig.internal.asn1.Asn1Class class * {
  # @Asn1Class-annotated types are reflectively instantiated.
  public <init>();

  # Fields with @Asn1Field inside an @Asn1Class-annotated type dictate how parsing happens.
  @com.android.apksig.internal.asn1.Asn1Field public <fields>;
}
