package com.avinashdavid.markdowntexteditor

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_test.*
import ru.noties.markwon.Markwon

internal class TestActivity : AppCompatActivity(), MarkdownTextEditorFragment.IMarkdownTextEditorListener {
    override fun onMarkdownTextEditingCompleted(markdown: String) {
        etStartingText.setText(markdown)
        Markwon.setMarkdown(tvResultTextView, markdown)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        setSupportActionBar(toolbar)
        btLaunchFragment.setOnClickListener {
            launchFragmentWithText(etStartingText.text.toString())
        }
        supportFragmentManager.findFragmentByTag(MarkdownTextEditorFragment.FRAGMENT_TAG)?.let {
            (it as MarkdownTextEditorFragment).setMarkdownTextListener(this)
        }
    }

    private fun launchFragmentWithText(string: String?) {
        val fragment = MarkdownTextEditorFragment.newInstance(this, string)
        supportFragmentManager.beginTransaction()
                .replace(R.id.flFragmentContainer, fragment, MarkdownTextEditorFragment.FRAGMENT_TAG)
                .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return super.onOptionsItemSelected(item)
    }
}
