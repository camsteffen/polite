package me.camsteffen.polite.rule

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import me.camsteffen.polite.R
import me.camsteffen.polite.rule.master.RulesFragment

class RenameDialogFragment : DialogFragment() {

    companion object {
        const val FRAGMENT_TAG = "RenameDialogFragment"

        fun newInstance(id: Long, name: String): RenameDialogFragment {
            val bundle = Bundle()
            bundle.putLong("id", id)
            bundle.putString("name", name)
            val fragment = RenameDialogFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val id = arguments!!.getLong("id")
        val name = arguments!!.getString("name")

        val view = activity!!.layoutInflater.inflate(R.layout.rename_dialog, null)
        val editText = view.findViewById(R.id.name) as EditText
        editText.setText(name)
        return AlertDialog.Builder(activity!!)
                .setTitle(R.string.rename_rule)
                .setView(view)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, { _, _ ->
                    val newName = editText.text.toString()
                    val rulesFragment = fragmentManager!!.findFragmentByTag(RulesFragment.FRAGMENT_TAG) as RulesFragment
                    rulesFragment.renameRule(id, newName)
                })
                .create()
    }
}
