package me.camsteffen.polite.ui.rule

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import me.camsteffen.polite.R
import me.camsteffen.polite.data.RuleService
import javax.inject.Inject

class RenameDialogFragment
@Inject constructor(
    private val ruleService: RuleService
) : DialogFragment() {

    fun setArguments(id: Long, name: String) {
        arguments = bundleOf(
            "id" to id,
            "name" to name
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val id = arguments!!.getLong("id")
        val name = arguments!!.getString("name")

        val view = activity!!.layoutInflater.inflate(R.layout.rename_dialog, null)
        val editText = view.findViewById(R.id.name) as EditText
        editText.setText(name)
        return AlertDialog.Builder(context!!)
            .setTitle(R.string.rename_rule)
            .setView(view)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val newName = editText.text.toString()
                ruleService.updateRuleNameAsync(id, newName)
            }
            .create()
    }
}
