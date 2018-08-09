# AndroidMarkdownTextEditor
Markdown text editor fragment for Android

This project contains an extendable fragment with UI to edit standard Markdown text. By exposing both the Markdown and the preview to the user, it allows for a better editing experience than most Markdown editors that I have experienced on consumer facing apps.

### Installation
To import this library, include Jitpack in your build script and project repositories


 build.gradle (Project)
    
        maven {
        
            url 'https://jitpack.io'
            
        }
        
 

Then add the project reference in your app dependencies

`implementation 'com.github.avi-david:AndroidMarkdownTextEditor:0.0.9'`

### Usage

* Implement `MarkdownTextEditorFragment.IMarkdownTextEditorListener` in the activity or fragment that you want to launch the editor from.
* Use `MarkdownTextEditorFragment.newInstance(markdownTextEditorListener: IMarkdownTextEditorListener, startingText: String?)` to create the fragment, and show it using FragmentTransaction.
* Call `(MarkdownTextEditorFragment).setMarkdownTextListener(markdownTextEditorListener: IMarkdownTextEditorListener)` in onCreate if `savedInstanceState != null`. This reassigns the interface instance to the recreated activity or fragment when required.
