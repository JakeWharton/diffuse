package com.jakewharton.diffuse

interface BinaryMembers {
  val members: List<Member>
  val declaredMembers: List<Member>
  val referencedMembers: List<Member>
}
