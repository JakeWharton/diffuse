package com.jakewharton.diffuse.format

interface CodeBinary {
  val members: List<Member>
  val declaredMembers: List<Member>
  val referencedMembers: List<Member>
}
