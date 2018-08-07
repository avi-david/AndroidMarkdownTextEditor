package com.avinashdavid.markdowntexteditor

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_rich_text_editor.*
import ru.noties.markwon.Markwon

class MarkdownTextEditorActivity : AppCompatActivity() {
    private var currentNumberedListIndex = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rich_text_editor)
        etPrimaryEditor.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                p0?.let {
                    Markwon.setMarkdown(this@MarkdownTextEditorActivity.tvTextPreview, it.toString())
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0 == "\n") {
                    currentNumberedListIndex = -1
                }
            }
        })
        btBold.setOnClickListener {
            etPrimaryEditor.addBold()
        }
        btItalic.setOnClickListener {
            etPrimaryEditor.addItalic()
        }
        btQuote.setOnClickListener {
            etPrimaryEditor.addQuote()
        }
        btStrikethrough.setOnClickListener {
            etPrimaryEditor.addStrikethrough()
        }
        btCode.setOnClickListener {
            etPrimaryEditor.addCode()
        }
        btLink.setOnClickListener {
            etPrimaryEditor.addLink()
        }
        btBulletList.setOnClickListener {
            etPrimaryEditor.addBulletListItem()
        }
        btNumberedList.setOnClickListener {
            if (currentNumberedListIndex == -1) {
                currentNumberedListIndex = 1
            } else {
                currentNumberedListIndex ++
            }
            etPrimaryEditor.addNumberedListItem(currentNumberedListIndex)
        }
    }
}

private fun insertMarkdownInString(string: String, markdown: String, insertAtPosition: Int) : String {
    val prestring = string.substring(0, insertAtPosition)
    val postString = try {
        string.substring(insertAtPosition)
    } catch (e: Exception) {
        ""
    }
    return "$prestring$markdown$postString"
}

private fun wrapStringWithMarkdown(string: String, splitMarkdown: String, selectionStart: Int, selectionEnd: Int) : String {
    val stringToWrap = string.substring(selectionStart, selectionEnd)
    val prestring = string.substring(0, selectionStart)
    val poststring = string.substring(selectionEnd)
    return "$prestring$splitMarkdown$stringToWrap$splitMarkdown$poststring"
}

private fun getFormattingMarkdown(string: String, splitMarkdown: String, selectionStart: Int, selectionEnd: Int) : String {
    return if (selectionStart == selectionEnd) {
        insertMarkdownInString(string, splitMarkdown + splitMarkdown, selectionEnd)
    } else {
        wrapStringWithMarkdown(string, splitMarkdown, selectionStart, selectionEnd)
    }
}

private fun EditText.insertFormattingMarkdown(splitMarkdown: String) {
    val currentSelectionStart = selectionStart
    val currentSelectionEnd = selectionEnd
    val currentText = this.text.toString()
    val adjustedText = getFormattingMarkdown(currentText, splitMarkdown, currentSelectionStart, currentSelectionEnd)
    setText(adjustedText)
    setSelection(currentSelectionEnd + splitMarkdown.length)
}

private fun EditText.addBold() {
    insertFormattingMarkdown("**")
}

private fun EditText.addItalic() {
    insertFormattingMarkdown("_")
}

private fun EditText.addStrikethrough() {
    insertFormattingMarkdown("~~")
}

private fun EditText.addCode() {
    insertFormattingMarkdown("`")
}

private fun EditText.addLink() {
    val currentSelectionEnd = selectionEnd
    val currentText = this.text.toString()
    val prestring = currentText.substring(0, currentSelectionEnd)
    val postString = currentText.substring(currentSelectionEnd)
    val fullString = "$prestring [Link Label Text](link address) $postString"
    this.setText(fullString)
}

private fun EditText.addListItemWithListMarker(listMarker: String) {
    val currentSelectionStart = selectionStart
    val currentSelectionEnd = selectionEnd
    val currentText = this.text.toString()
    if (currentSelectionEnd != currentSelectionStart) {
        val preString = currentText.substring(0, currentSelectionStart)
        val stringToPutInList = currentText.substring(currentSelectionStart, currentSelectionEnd)
        val postString = currentText.substring(currentSelectionEnd)
        val stringToInsert = "$preString $lineBreak $listMarker $stringToPutInList $lineBreak $postString"
        setText(stringToInsert)
    } else {
        val stringToInsert = if (currentText.count() > 0) {
            "$currentText $lineBreak $listMarker "
        } else {
            "$listMarker "
        }
        setText(stringToInsert)
        setSelection(stringToInsert.length)
    }
}

private fun EditText.addQuote() {
    addListItemWithListMarker(">")
}

private fun EditText.addBulletListItem() {
    addListItemWithListMarker("*")
}

private fun EditText.addNumberedListItem(numberValue: Int) {
    addListItemWithListMarker(numberValue.toString() + ".")
}

private const val lineBreak = "\n\n"