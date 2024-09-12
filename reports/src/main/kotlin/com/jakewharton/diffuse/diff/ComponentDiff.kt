package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.report.htmlEncoded
import com.jakewharton.diffuse.report.toDiffString
import com.jakewharton.picnic.renderText
import kotlinx.html.FlowContent
import kotlinx.html.br
import kotlinx.html.details
import kotlinx.html.div
import kotlinx.html.h4
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.summary
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.unsafe

internal class ComponentDiff<T>(
  val oldRawCount: Int,
  val oldCount: Int,
  val newRawCount: Int,
  val newCount: Int,
  val added: Set<T>,
  val removed: Set<T>,
) {
  val changed get() = added.isNotEmpty() || removed.isNotEmpty()
}

internal fun <R, T> componentDiff(oldItems: List<R>, newItems: List<R>, selector: (R) -> Collection<T>): ComponentDiff<T> {
  val oldRawCount = oldItems.sumOf { selector(it).size }
  val newRawCount = newItems.sumOf { selector(it).size }
  val oldSet = oldItems.flatMapTo(mutableSetOf(), selector)
  val newSet = newItems.flatMapTo(mutableSetOf(), selector)
  val added = newSet - oldSet
  val removed = oldSet - newSet
  return ComponentDiff(
    oldRawCount,
    oldSet.size,
    newRawCount,
    newSet.size,
    added,
    removed,
  )
}

internal fun StringBuilder.appendComponentDiff(name: String, diff: ComponentDiff<*>) {
  if (diff.changed) {
    appendLine()
    appendLine("$name:")
    appendLine()
    appendLine(
      buildString {
        appendLine(
          diffuseTable {
            header {
              row {
                cell("old")
                cell("new")
                cell("diff")
              }
            }

            val diffSize = (diff.added.size - diff.removed.size).toDiffString()
            val addedSize = diff.added.size.toDiffString(zeroSign = '+')
            val removedSize = (-diff.removed.size).toDiffString(zeroSign = '-')
            row(diff.oldCount, diff.newCount, "$diffSize ($addedSize $removedSize)")
          }.renderText(),
        )
        if (diff.added.isNotEmpty()) {
          appendLine()
          diff.added.forEach {
            appendLine("+ $it")
          }
        }
        if (diff.removed.isNotEmpty()) {
          appendLine()
          diff.removed.forEach {
            appendLine("- $it")
          }
        }
      }.prependIndent("  "),
    )
  }
}

internal fun FlowContent.appendComponentDiff(name: String, diff: ComponentDiff<*>) {
  if (diff.changed) {
    div {
      style = "margin: 24px 0;"

      h4 { +name }

      table {
        thead {
          tr {
            td { +"old" }
            td { +"new" }
            td { +"diff" }
          }
        }
        tbody {
          val diffSize = (diff.added.size - diff.removed.size).toDiffString()
          val addedSize = diff.added.size.toDiffString(zeroSign = '+')
          val removedSize = (-diff.removed.size).toDiffString(zeroSign = '-')

          tr {
            td { +diff.oldCount.toString() }
            td { +diff.newCount.toString() }
            td { +"$diffSize ($addedSize $removedSize)" }
          }
        }
      }

      details {
        summary { +"diff" }

        div {
          style = "margin-left: 16pt;"

          if (diff.added.isNotEmpty()) {
            br()
            diff.added.forEach {
              span { unsafe { raw("+ $it".htmlEncoded) } }
              br()
            }
          }

          if (diff.removed.isNotEmpty()) {
            br()
            diff.removed.forEach {
              span { unsafe { raw("- $it".htmlEncoded) } }
              br()
            }
          }
        }
      }
    }
  }
}
