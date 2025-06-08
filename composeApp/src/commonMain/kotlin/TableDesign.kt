/*
 * Copyright (C) 2025, Send24.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT
 * SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Person(
    val id: Int,
    val fullName: String,
    val position: String,
    val salary: String
)

@Composable
fun PeopleTable(
    people: List<Person>,
    modifier: Modifier = Modifier
) {
    // Outer border
    Surface(
        modifier = modifier,
        shape = RectangleShape,
        border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color.Red)
    ) {
        Column {
            // Header row
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
            ) {
                TableCell(text = "No.", weight = 1f, isHeader = true)
                TableCell(text = "Full Name", weight = 3f, isHeader = true)
                TableCell(text = "Position", weight = 3f, isHeader = true)
                TableCell(text = "Salary", weight = 2f, isHeader = true)
            }
            LazyColumn {
                itemsIndexed(people) { index, person ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                    ) {
                        TableCell(text = person.id.toString(), weight = 1f)
                        TableCell(text = person.fullName, weight = 3f)
                        TableCell(text = person.position, weight = 3f)
                        TableCell(text = person.salary, weight = 2f)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .border(
                // draw cell‐internal vertical borders
                BorderStroke(1.dp, androidx.compose.ui.graphics.Color.Red),
                RectangleShape
            )
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        BasicText(
            text = text,
            style = androidx.compose.ui.text.TextStyle(
                fontSize = if (isHeader) 16.sp else 14.sp,
                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}

@Composable
fun PreviewPeopleTable() {
    val sample = listOf(
        Person(1, "Bill Gates", "Founder Microsoft",  "$1000"),
        Person(2, "Steve Jobs", "Founder Apple",     "$1200"),
        Person(3, "Larry Page", "Founder Google",    "$1100"),
        Person(4, "Mark Zuckerberg", "Founder Facebook", "$1300")
    )
    PeopleTable(
        people = sample,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}
