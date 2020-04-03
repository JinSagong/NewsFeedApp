package com.jin.news.view.dialog

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jin.news.MyApplication.Companion.languagePreferences
import com.jin.news.R
import kotlinx.android.synthetic.main.dialog_language.*

class LanguageDialog(context: Context) : BottomSheetDialog(context) {

    fun setDialog(mCallback: DialogCallback) {
        setContentView(R.layout.dialog_language)
        behavior.skipCollapsed = true

        setOnShowListener {
            languageRadioGroup.check(languagePreferences.languageId)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        languageCancelTextView.setOnClickListener { dismiss() }

        languageDoneTextView.setOnClickListener {
            val checked = languageRadioGroup.checkedRadioButtonId
            if (checked != languagePreferences.languageId) {
                languagePreferences.languageId = checked
                mCallback.onDonePressed()
            }
            dismiss()
        }
    }
}