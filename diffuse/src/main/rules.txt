-dontobfuscate
-keepattributes SourceFile, LineNumberTable

-allowaccessmodification

-keep class com.jakewharton.diffuse.Diffuse {
  public static void main(java.lang.String[]);
}

##############
### APKSIG ###
##############

# Keeps generic signatures for reflection. It requires InnerClasses which requires EnclosingMethod.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Annotations for reflection.
-keepattributes RuntimeVisible*Annotations,AnnotationDefault
-keep class com.android.apksig.internal.** { *; }

# Called by reflection
-keep class com.android.apksig.ApkVerifier$Result {
    private java.util.List getV4SchemeSigners();
}

##############
### APKSIG ###
##############
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
