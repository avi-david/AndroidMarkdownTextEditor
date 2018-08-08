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
        setHasOptionsMenu(true)
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
            fragmentView?.etPrimaryEditor?.addBold()
        }
        fragmentView?.btItalic?.setOnClickListener {
            fragmentView?.etPrimaryEditor?.addItalic()
        }
        fragmentView?.btQuote?.setOnClickListener {
            if (!isQuoteOn) {
                currentNumberedListIndex = -1
                isQuoteOn = true
                if (fragmentView?.etPrimaryEditor?.text?.count() ?: 0 > 0) {
                    fragmentView?.etPrimaryEditor?.insertDoubleNewLine()
                }
                insertQuoteLine()
            } else {
                isQuoteOn = false
                fragmentView?.etPrimaryEditor?.insertDoubleNewLine()
            }
            isNumberedListOn = false
            toggleControlButton(btNumberedList, isNumberedListOn)
            isBulletListOn = false
            toggleControlButton(btBulletList, isBulletListOn)
            toggleControlButton(btQuote, isQuoteOn)
        }
        fragmentView?.btStrikethrough?.setOnClickListener {
            fragmentView?.etPrimaryEditor?.addStrikethrough()
        }
        fragmentView?.btCode?.setOnClickListener {
            fragmentView?.etPrimaryEditor?.addCode()
        }
        fragmentView?.btLink?.setOnClickListener {
            fragmentView?.etPrimaryEditor?.addLink()
        }
        fragmentView?.btBulletList?.setOnClickListener {
            if (!isBulletListOn) {
                currentNumberedListIndex = -1
                isBulletListOn = true
                if (fragmentView?.etPrimaryEditor?.text?.count() ?: 0 > 0) {
                    fragmentView?.etPrimaryEditor?.insertDoubleNewLine()
                }
                insertBulletListItem()
            } else {
                isBulletListOn = false
                fragmentView?.etPrimaryEditor?.insertDoubleNewLine()
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
                if (fragmentView?.etPrimaryEditor?.text?.count() ?: 0 > 0) {
                    fragmentView?.etPrimaryEditor?.insertDoubleNewLine()
                }
                insertNumberedListItem()
            } else {
                isNumberedListOn = false
                currentNumberedListIndex = -1
                fragmentView?.etPrimaryEditor?.insertDoubleNewLine()
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.miMarkdownTextEditorDone -> {
                fragmentView?.etPrimaryEditor?.text?.toString()?.let {
                    try {
                        currentMarkdownTextEditorListener?.onMarkdownTextEditingCompleted(it)
                        activity?.let {
                            it.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
                        }
                    } catch (e: Exception) {

                    }
                    //TODO: remove from fragment backstack
                }
                true
            }
            else -> {
                false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_markdown_text_editor, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    fun setMarkdownTextListener(markdownTextEditorListener: IMarkdownTextEditorListener) {
        currentMarkdownTextEditorListener = markdownTextEditorListener
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

    interface IMarkdownTextEditorListener {
        fun onMarkdownTextEditingCompleted(markdown: String)
    }

    companion object {
        const val FRAGMENT_TAG = "MarkdownTextEditorFragment"
        const val ARG_STARTING_TEXT = "ARG_STARTING_TEXT"

        private var currentMarkdownTextEditorListener: IMarkdownTextEditorListener? = null

        /**
         * IMPORTANT: Please remember to call setMarkdownTextListener in your onCreate(savedInstanceState: Bundle?) in order to restore the interface instance for the editor fragment.
         * This allows for more flexible usage of this fragment, by allowing it to be launched and used directly from other fragments
         * @param markdownTextEditorListener An interface instance
         * @param startingText The initial markdown text in the editor fragment
         * @return A correctly initialized MarkdownTextEditorFragment
         */
        @JvmStatic
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

internal fun insertMarkdownInString(string: String, markdown: String, insertAtPosition: Int) : String {
    val prestring = string.substring(0, insertAtPosition)
    val postString = try {
        string.substring(insertAtPosition)
    } catch (e: Exception) {
        ""
    }
    return "$prestring$markdown$postString"
}

internal fun wrapStringWithMarkdown(string: String, splitMarkdown: String, selectionStart: Int, selectionEnd: Int) : String {
    val stringToWrap = string.substring(selectionStart, selectionEnd)
    val prestring = string.substring(0, selectionStart)
    val poststring = string.substring(selectionEnd)
    return "$prestring$splitMarkdown$stringToWrap$splitMarkdown$poststring"
}

internal fun getFormattingMarkdown(string: String, splitMarkdown: String, selectionStart: Int, selectionEnd: Int) : String {
    return if (selectionStart == selectionEnd) {
        insertMarkdownInString(string, splitMarkdown + splitMarkdown, selectionEnd)
    } else {
        wrapStringWithMarkdown(string, splitMarkdown, selectionStart, selectionEnd)
    }
}

internal fun EditText.insertFormattingMarkdown(splitMarkdown: String) {
    val currentSelectionStart = selectionStart
    val currentSelectionEnd = selectionEnd
    val currentText = this.text.toString()
    val adjustedText = getFormattingMarkdown(currentText, splitMarkdown, currentSelectionStart, currentSelectionEnd)
    setText(adjustedText)
    setSelection(currentSelectionEnd + splitMarkdown.length)
}

internal fun EditText.addBold() {
    insertFormattingMarkdown("**")
}

internal fun EditText.addItalic() {
    insertFormattingMarkdown("_")
}

internal fun EditText.addStrikethrough() {
    insertFormattingMarkdown("~~")
}

internal fun EditText.addCode() {
    insertFormattingMarkdown("`")
}

internal fun EditText.addLink() {
    val currentSelectionEnd = selectionEnd
    val currentText = this.text.toString()
    val prestring = currentText.substring(0, currentSelectionEnd)
    val postString = currentText.substring(currentSelectionEnd)
    val fullString = "$prestring [LABEL](link) $postString"
    this.setText(fullString)
}

internal fun EditText.addListItemWithListMarker(listMarker: String) {
    val currentSelectionStart = selectionStart
    val currentSelectionEnd = selectionEnd
    val currentText = this.text.toString()
    var selectionToSet = 0
    val lineBreak = "\n"
    if (currentSelectionEnd != currentSelectionStart) {
        val preString = currentText.substring(0, currentSelectionStart)
        val stringToPutInList = currentText.substring(currentSelectionStart, currentSelectionEnd)
        val postString = currentText.substring(currentSelectionEnd)
        val stringToInsert = if (preString.count() == 0) {
            selectionToSet = "$listMarker $stringToPutInList".count()
            if (postString.count() == 0) {
                "$listMarker $stringToPutInList"
            } else {
                if (stringToPutInList.endsWith("\n")) {
                    "$listMarker $stringToPutInList$postString"
                } else {
                    "$listMarker $stringToPutInList $lineBreak$postString"
                }
            }
        } else {
            if (postString.count() == 0) {
                if (preString.endsWith("\n")) {
                    selectionToSet = "$preString$listMarker $stringToPutInList".count()
                    "$preString$listMarker $stringToPutInList"
                } else {
                    selectionToSet = "$preString $lineBreak$listMarker $stringToPutInList".count()
                    "$preString $lineBreak$listMarker $stringToPutInList"
                }
            } else {
                if (preString.endsWith("\n")) {
                    selectionToSet = "$preString$listMarker $stringToPutInList".count()
                    if (stringToPutInList.endsWith("\n")) {
                        "$preString$listMarker $stringToPutInList$postString"
                    } else {
                        "$preString$listMarker $stringToPutInList $lineBreak$postString"
                    }
                } else {
                    selectionToSet = "$preString $lineBreak$listMarker $stringToPutInList".count()
                    if (stringToPutInList.endsWith("\n")) {
                        "$preString $lineBreak$listMarker $stringToPutInList$postString"
                    } else {
                        "$preString $lineBreak$listMarker $stringToPutInList $lineBreak$postString"
                    }
                }
            }
        }
        setText(stringToInsert)
    } else {
        val stringToInsert = if (currentText.count() > 0) {
            val prestring = currentText.substring(0, currentSelectionEnd)
            val postString = currentText.substring(currentSelectionEnd)
            val fullString = if (!currentText.endsWith("\n")) {
                "$prestring $lineBreak$listMarker "
            } else {
                "$prestring$listMarker "
            }
            selectionToSet = fullString.length
            if (postString.count() > 0) {
                "$fullString $lineBreak$postString"
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

internal fun EditText.addQuote() {
    addListItemWithListMarker(">")
}

internal fun EditText.addBulletListItem() {
    addListItemWithListMarker("*")
}

internal fun EditText.addNumberedListItem(numberValue: Int) {
    addListItemWithListMarker(numberValue.toString() + ".")
}

internal const val doubleLineBreak = "\n\n"

internal fun EditText.insertDoubleNewLine() {
    setText(text.toString() + doubleLineBreak)
    setSelection(this.text.length)
}

internal fun EditText.insertSingleNewLine() {
    setText(text.toString() + "\n")
    setSelection(this.text.length)
}