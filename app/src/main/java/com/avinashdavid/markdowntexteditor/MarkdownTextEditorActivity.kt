package com.avinashdavid.markdowntexteditor

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import kotlinx.android.synthetic.main.activity_rich_text_editor.*
import ru.noties.markwon.Markwon

class MarkdownTextEditorActivity : AppCompatActivity() {
    private var startingText = ""
    private var currentNumberedListIndex = -1
    private var isNumberedListOn = false
    private var isBulletListOn = false
    private var isQuoteOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.hasExtra(EXTRA_STARTING_TEXT)) {
            startingText = intent.getStringExtra(EXTRA_STARTING_TEXT)
        }
        if (savedInstanceState?.containsKey(EXTRA_STARTING_TEXT) == true) {
            startingText = savedInstanceState.getString(EXTRA_STARTING_TEXT, "")
        }
        setContentView(R.layout.activity_rich_text_editor)
        etPrimaryEditor.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                p0?.let {
                    Markwon.setMarkdown(this@MarkdownTextEditorActivity.tvTextPreview, it.toString())
                    this@MarkdownTextEditorActivity.scrollViewPreview.fullScroll(View.FOCUS_DOWN)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
                val text = try { p0?.substring(start, start+count) } catch (e: Exception) {""}
                if (text == "\n") {
                    if (isBulletListOn) {
                        insertBulletListItem()
                    } else if (isNumberedListOn) {
                        insertNumberedListItem()
                    } else if (isQuoteOn) {
                        insertQuoteLine()
                    }
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
            if (!isQuoteOn) {
                currentNumberedListIndex = -1
                isQuoteOn = true
                insertQuoteLine()
            } else {
                isQuoteOn = false
                etPrimaryEditor.insertMarkdownNewline()
            }
            isNumberedListOn = false
            toggleControlButton(btNumberedList, isNumberedListOn)
            isBulletListOn = false
            toggleControlButton(btBulletList, isBulletListOn)
            toggleControlButton(btQuote, isQuoteOn)
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
            if (!isBulletListOn) {
                currentNumberedListIndex = -1
                isBulletListOn = true
                insertBulletListItem()
            } else {
                isBulletListOn = false
                etPrimaryEditor.insertMarkdownNewline()
            }
            isNumberedListOn = false
            toggleControlButton(btNumberedList, isNumberedListOn)
            isQuoteOn = false
            toggleControlButton(btQuote, isQuoteOn)
            toggleControlButton(btBulletList, isBulletListOn)
        }
        btNumberedList.setOnClickListener {
            if (!isNumberedListOn) {
                isNumberedListOn = true
                insertNumberedListItem()
            } else {
                isNumberedListOn = false
                currentNumberedListIndex = -1
                etPrimaryEditor.insertMarkdownNewline()
            }
            isBulletListOn = false
            toggleControlButton(btBulletList, isBulletListOn)
            isQuoteOn = false
            toggleControlButton(btQuote, isQuoteOn)
            toggleControlButton(btNumberedList, isNumberedListOn)
        }
        etPrimaryEditor.setText(startingText)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putString(EXTRA_STARTING_TEXT, etPrimaryEditor.text.toString())
        super.onSaveInstanceState(outState)
    }

    private fun insertBulletListItem() {
        etPrimaryEditor.addBulletListItem()
    }

    private fun insertNumberedListItem() {
        if (currentNumberedListIndex == -1) {
            currentNumberedListIndex = 1
        } else {
            currentNumberedListIndex ++
        }
        etPrimaryEditor.addNumberedListItem(currentNumberedListIndex)
    }

    private fun insertQuoteLine() {
        etPrimaryEditor.addQuote()
    }

    private fun toggleControlButton(button: ImageButton, isNowOn: Boolean) {
        if (isNowOn) {
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
        } else {
            val attrs = intArrayOf(R.attr.selectableItemBackground)
            val typedArray = obtainStyledAttributes(attrs)
            val backgroundResource = typedArray.getResourceId(0, 0)
            button.setBackgroundResource(backgroundResource)
            typedArray.recycle()
        }
    }

    companion object {
        private const val EXTRA_STARTING_TEXT = "EXTRA_STARTING_TEXT"
        const val KEY_RESULT_EXTRA_FINAL_TEXT = "KEY_RESULT_EXTRA_FINAL_TEXT"
        const val REQUEST_CODE_MARKDOWN_TEXT_EDITOR: Int = 8921072
        const val RESULT_CODE_EDITING_COMPLETED = Activity.RESULT_OK

        fun startForResult(activity: AppCompatActivity, startingMarkdownText: String? = null) {
            activity.startActivityForResult(Intent(activity, MarkdownTextEditorActivity::class.java).apply {
                startingMarkdownText?.let {
                    putExtra(EXTRA_STARTING_TEXT, it)
                }
            }, REQUEST_CODE_MARKDOWN_TEXT_EDITOR)
        }

        fun startForResult(activity: Activity, startingMarkdownText: String? = null) {
            activity.startActivityForResult(Intent(activity, MarkdownTextEditorActivity::class.java).apply {
                startingMarkdownText?.let {
                    putExtra(EXTRA_STARTING_TEXT, it)
                }
            }, REQUEST_CODE_MARKDOWN_TEXT_EDITOR)
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
    var selectionToSet = 0
    if (currentSelectionEnd != currentSelectionStart) {
        val preString = currentText.substring(0, currentSelectionStart)
        val stringToPutInList = currentText.substring(currentSelectionStart, currentSelectionEnd)
        val postString = currentText.substring(currentSelectionEnd)
        val stringToInsert = if (preString.count() == 0) {
            if (postString.count() == 0) {
                "$listMarker $stringToPutInList"
            } else {
                "$listMarker $stringToPutInList $lineBreak $postString"
            }
        } else {
            if (postString.count() == 0) {
                "$preString $lineBreak $listMarker $stringToPutInList"
            } else {
                "$preString $lineBreak $listMarker $stringToPutInList $lineBreak $postString"
            }
        }
        setText(stringToInsert)
        selectionToSet = if (preString.count() == 0) {
            "$listMarker $stringToPutInList ".count()
        } else {
            "$preString $lineBreak $listMarker $stringToPutInList ".count()
        }
    } else {
        val stringToInsert = if (currentText.count() > 0) {
            val prestring = currentText.substring(0, currentSelectionEnd)
            val postString = currentText.substring(currentSelectionEnd)
            val fullString = "$prestring $lineBreak $listMarker "
            selectionToSet = fullString.length
            if (postString.count() > 0) {
                "$fullString $lineBreak $postString"
            } else {
                fullString
            }
        } else {
            selectionToSet = listMarker.count() + 1
            "$listMarker "
        }
        setText(stringToInsert)
    }
    try { setSelection(selectionToSet) } catch (e: Exception) {
        setSelection(this.text.length)
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

private fun EditText.insertMarkdownNewline() {
    setText(text.toString() + lineBreak)
    setSelection(this.text.length)
}