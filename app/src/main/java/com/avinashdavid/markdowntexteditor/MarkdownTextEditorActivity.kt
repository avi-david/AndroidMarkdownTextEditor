package com.avinashdavid.markdowntexteditor

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import kotlinx.android.synthetic.main.activity_rich_text_editor.*
import ru.noties.markwon.Markwon
import android.R.attr.data
import android.content.Context
import android.content.res.TypedArray
import android.util.TypedValue



open class MarkdownTextEditorActivity : AppCompatActivity() {
    private var startingText = ""

    private var currentNumberedListIndex = -1
    private var isNumberedListOn = false
    private var isBulletListOn = false
    private var isQuoteOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.hasExtra(EXTRA_STARTING_TEXT)) {
            startingText = intent.getStringExtra(EXTRA_STARTING_TEXT)
//            toolbarColor = intent.getIntExtra(EXTRA_TOOLBAR_COLOR, ContextCompat.getColor(this, R.color.colorPrimary))
//            accentColor = intent.getIntExtra(EXTRA_ACCENT_COLOR, ContextCompat.getColor(this, R.color.colorAccent))
        }
        if (savedInstanceState?.containsKey(EXTRA_STARTING_TEXT) == true) {
            startingText = savedInstanceState.getString(EXTRA_STARTING_TEXT, "")
//            toolbarColor = savedInstanceState.getInt(EXTRA_TOOLBAR_COLOR, ContextCompat.getColor(this, R.color.colorPrimary))
//            accentColor = savedInstanceState.getInt(EXTRA_ACCENT_COLOR, ContextCompat.getColor(this, R.color.colorAccent))
        }
        setContentView(R.layout.activity_rich_text_editor)
        setSupportActionBar(toolbar)
//        supportActionBar?.setBackgroundDrawable(ColorDrawable(toolbarColor))
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
        outState?.apply {
            putString(EXTRA_STARTING_TEXT, etPrimaryEditor.text.toString())
//            putInt(EXTRA_TOOLBAR_COLOR, toolbarColor)
//            putInt(EXTRA_ACCENT_COLOR, accentColor)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_markdown_text_editor, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.miMarkdownTextEditorDone -> {
                setResult(RESULT_CODE_EDITING_COMPLETED, Intent().putExtra(RESULT_EXTRA_FINAL_TEXT, etPrimaryEditor.text.toString()))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
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
        const val EXTRA_STARTING_TEXT = "EXTRA_STARTING_TEXT"

        const val RESULT_EXTRA_FINAL_TEXT = "RESULT_EXTRA_FINAL_TEXT"
        const val RESULT_CODE_EDITING_COMPLETED = Activity.RESULT_OK
        const val REQUEST_CODE_MARKDOWN_TEXT_EDITOR: Int = 62371

        /**
         * When the user finishes editing and confirms final text, the final text will be sent back via result
         * with requestCode: REQUEST_CODE_MARKDOWN_TEXT_EDITOR
         * with result: RESULT_CODE_EDITING_COMPLETED
         * and an intent with string extra: RESULT_EXTRA_FINAL_TEXT
         * @param activity An AppCompatActivity
         * @param startingMarkdownText The initial text
         */
        @JvmStatic
        fun startForResult(activity: AppCompatActivity, startingMarkdownText: String? = null) {
            activity.startActivityForResult(Intent(activity, MarkdownTextEditorActivity::class.java).apply {
                startingMarkdownText?.let {
                    putExtra(EXTRA_STARTING_TEXT, it)
                }
            }, REQUEST_CODE_MARKDOWN_TEXT_EDITOR)
        }

        /**
         * When the user finishes editing and confirms final text, the final text will be sent back via result
         * with requestCode: REQUEST_CODE_MARKDOWN_TEXT_EDITOR
         * with result: RESULT_CODE_EDITING_COMPLETED
         * and an intent with string extra: RESULT_EXTRA_FINAL_TEXT
         * @param activity An Activity
         * @param startingMarkdownText The initial text
         */
        @JvmStatic
        fun startForResult(activity: Activity, startingMarkdownText: String? = null) {
            activity.startActivityForResult(Intent(activity, MarkdownTextEditorActivity::class.java).apply {
                startingMarkdownText?.let {
                    putExtra(EXTRA_STARTING_TEXT, it)
                }
            }, REQUEST_CODE_MARKDOWN_TEXT_EDITOR)
        }

        private fun fetchAccentColor(context: Context): Int {
            val typedValue = TypedValue()

            val a = context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorAccent))
            val color = a.getColor(0, 0)

            a.recycle()

            return color
        }
    }
}