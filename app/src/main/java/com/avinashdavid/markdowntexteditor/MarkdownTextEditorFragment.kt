package com.avinashdavid.markdowntexteditor

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.ImageButton
import kotlinx.android.synthetic.main.fragment_markdown_text_editor.*
import kotlinx.android.synthetic.main.fragment_markdown_text_editor.view.*
import ru.noties.markwon.Markwon

open class MarkdownTextEditorFragment : Fragment() {
    private var startingText: String? = null
    private var fragmentView: View? = null

    private var currentNumberedListIndex = -1
    private var isNumberedListOn = false
    private var isBulletListOn = false
    private var isQuoteOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listOf(arguments, savedInstanceState).forEach {
            it?.let {
                getPropertiesFromBundle(it)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = inflater.inflate(R.layout.fragment_markdown_text_editor, container, false)

        fragmentView?.etPrimaryEditor?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                p0?.let {
                    this@MarkdownTextEditorFragment.fragmentView?.tvTextPreview?.let {
                        Markwon.setMarkdown(it, p0.toString())
                    }
                    this@MarkdownTextEditorFragment.fragmentView?.scrollViewPreview?.fullScroll(View.FOCUS_DOWN)
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
        fragmentView?.btBold?.setOnClickListener {
            etPrimaryEditor.addBold()
        }
        fragmentView?.btItalic?.setOnClickListener {
            etPrimaryEditor.addItalic()
        }
        fragmentView?.btQuote?.setOnClickListener {
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
        fragmentView?.btStrikethrough?.setOnClickListener {
            etPrimaryEditor.addStrikethrough()
        }
        fragmentView?.btCode?.setOnClickListener {
            etPrimaryEditor.addCode()
        }
        fragmentView?.btLink?.setOnClickListener {
            etPrimaryEditor.addLink()
        }
        fragmentView?.btBulletList?.setOnClickListener {
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
        fragmentView?.btNumberedList?.setOnClickListener {
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
        startingText?.let {
            fragmentView?.etPrimaryEditor?.setText(it)
        }

        return fragmentView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        fragmentView?.etPrimaryEditor?.text?.toString()?.let {
            outState.putString(ARG_STARTING_TEXT, it)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_markdown_text_editor, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun getPropertiesFromBundle(bundle: Bundle) {
        if (bundle.containsKey(ARG_STARTING_TEXT)) {
            startingText = bundle.getString(ARG_STARTING_TEXT)
        }
    }

    private fun insertBulletListItem() {
        fragmentView?.etPrimaryEditor?.addBulletListItem()
    }

    private fun insertNumberedListItem() {
        if (currentNumberedListIndex == -1) {
            currentNumberedListIndex = 1
        } else {
            currentNumberedListIndex ++
        }
        fragmentView?.etPrimaryEditor?.addNumberedListItem(currentNumberedListIndex)
    }

    private fun insertQuoteLine() {
        fragmentView?.etPrimaryEditor?.addQuote()
    }

    private fun toggleControlButton(button: ImageButton, isNowOn: Boolean) {
        activity?.let {
            if (isNowOn) {
                button.setBackgroundColor(ContextCompat.getColor(it, R.color.colorAccent))
            } else {
                val attrs = intArrayOf(R.attr.selectableItemBackground)
                val typedArray = it.obtainStyledAttributes(attrs)
                val backgroundResource = typedArray.getResourceId(0, 0)
                button.setBackgroundResource(backgroundResource)
                typedArray.recycle()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.miMarkdownTextEditorDone -> {
                fragmentView?.etPrimaryEditor?.text?.toString()?.let {
                    currentMarkdownTextEditorListener?.onMarkdownTextEditingCompleted(it)
                    //TODO: remove from fragment backstack
                }
                true
            }
            else -> {
                false
            }
        }
    }

    interface IMarkdownTextEditorListener {
        fun onMarkdownTextEditingCompleted(markdown: String)
    }

    companion object {
        const val ARG_STARTING_TEXT = "ARG_STARTING_TEXT"

        private var currentMarkdownTextEditorListener: IMarkdownTextEditorListener? = null

        fun newInstance(markdownTextEditorListener: IMarkdownTextEditorListener, startingText: String?) : MarkdownTextEditorFragment {
            currentMarkdownTextEditorListener = markdownTextEditorListener
            return MarkdownTextEditorFragment().apply {
                arguments = Bundle().apply {
                    startingText?.let {
                        putString(ARG_STARTING_TEXT, it)
                    }
                }
            }
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