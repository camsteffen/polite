package me.camsteffen.polite

import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.EditText

class RenameDialogFragment : DialogFragment() {

    companion object {
        const val FRAGMENT_TAG = "RenameDialogFragment"

        fun newInstance(id: Long, position: Int, name: String): RenameDialogFragment {
            val bundle = Bundle()
            bundle.putLong("id", id)
            bundle.putInt("position", position)
            bundle.putString("name", name)
            val fragment = RenameDialogFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val id = arguments.getLong("id")
        val position = arguments.getInt("position")
        val name = arguments.getString("name")

        val view = activity.layoutInflater.inflate(R.layout.rename_dialog, null)
        val editText = view.findViewById(R.id.name) as EditText
        editText.setText(name)
        return AlertDialog.Builder(activity)
                .setTitle(R.string.rename_rule)
                .setView(view)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, { _, _ ->
                    val newName = editText.text.toString()
                    val rulesFragment = fragmentManager.findFragmentByTag(RulesFragment.FRAGMENT_TAG) as RulesFragment
                    rulesFragment.renameRule(id, position, newName)
                })
                .create()
    }
}
