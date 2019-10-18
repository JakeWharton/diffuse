package com.jakewharton.diffuse

import com.jakewharton.diffuse.ArchiveFile.Type.Companion.toJarFileType
import com.jakewharton.diffuse.ArchiveFiles.Companion.toArchiveFiles
import com.jakewharton.diffuse.io.Input
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class Jar private constructor(
  override val filename: String?,
  val files: ArchiveFiles,
  val declaredMembers: List<Member>,
  val referencedMembers: List<Member>
) : Binary {
  val members = declaredMembers + referencedMembers

  companion object {
    @JvmStatic
    @JvmName("parse")
    fun Input.toJar(): Jar {
      toZip().use { zip ->
        val files = zip.toArchiveFiles { it.toJarFileType() }

        val declaredMembers = mutableListOf<Member>()
        val referencedMembers = mutableSetOf<Member>()
        zip.entries
            .filter { it.path.endsWith(".class") }
            .forEach { entry ->
              val reader = ClassReader(entry.input().toByteArray())
              val type = TypeDescriptor("L${reader.className};")
              reader.accept(object : ClassVisitor(Opcodes.ASM7) {
                override fun visitMethod(
                  access: Int,
                  name: String,
                  descriptor: String,
                  signature: String?,
                  exceptions: Array<out String>?
                ): MethodVisitor? {
                  val method = parseMethod(type, name, descriptor)
                  declaredMembers += method

                  return object : MethodVisitor(Opcodes.ASM7) {
                    override fun visitMethodInsn(
                      opcode: Int,
                      owner: String,
                      name: String,
                      descriptor: String,
                      isInterface: Boolean
                    ) {
                      val ownerType = TypeDescriptor("L$owner;")
                      val referencedMethod = parseMethod(ownerType, name, descriptor)
                      referencedMembers += referencedMethod
                    }

                    override fun visitFieldInsn(
                      opcode: Int,
                      owner: String,
                      name: String,
                      descriptor: String
                    ) {
                      val ownerType = TypeDescriptor("L$owner;")
                      val referencedField = Field(ownerType, name, TypeDescriptor(descriptor))
                      referencedMembers += referencedField
                    }
                  }
                }

                override fun visitField(
                  access: Int,
                  name: String,
                  descriptor: String,
                  signature: String?,
                  value: Any?
                ): FieldVisitor? {
                  val field = Field(type, name, TypeDescriptor(descriptor))
                  declaredMembers += field
                  return null
                }

                private fun parseMethod(
                  owner: TypeDescriptor,
                  name: String,
                  descriptor: String
                ): Method {
                  val parameterTypes = mutableListOf<TypeDescriptor>()
                  val returnType: TypeDescriptor
                  var i = 1
                  loop@ while (true) {
                    parameterTypes += when (val char = descriptor[i]) {
                      ')' -> break@loop
                      'L' -> {
                        val start = i
                        val end = descriptor.indexOf(';', startIndex = i)
                        i = end + 1
                        TypeDescriptor(descriptor.substring(start, end))
                      }
                      else -> {
                        i++
                        TypeDescriptor(char.toString())
                      }
                    }
                  }
                  returnType = TypeDescriptor(descriptor.substring(i + 1))
                  return Method(owner, name, parameterTypes, returnType)
                }
              }, 0)
            }

        // Declared methods are likely to reference other declared members.
        referencedMembers -= declaredMembers

        return Jar(name, files, declaredMembers.sorted(), referencedMembers.toList().sorted())
      }
    }
  }
}
