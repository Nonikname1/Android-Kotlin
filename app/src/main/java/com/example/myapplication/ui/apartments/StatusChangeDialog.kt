package com.example.myapplication.ui.apartments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.myapplication.databinding.DialogStatusChangeBinding

class StatusChangeDialog : DialogFragment() {

    interface OnStatusChangedListener {
        fun onStatusChanged(newStatus: String, comment: String?)
    }

    var listener: OnStatusChangedListener? = null

    private val statusValues = listOf("available", "reserved", "sold", "cancelled")
    private val statusLabels = listOf("Доступна", "Бронь", "Продана", "Отменена")

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogStatusChangeBinding.inflate(LayoutInflater.from(requireContext()))

        val currentStatus = arguments?.getString(ARG_CURRENT_STATUS) ?: "available"
        val currentIndex = statusValues.indexOf(currentStatus).coerceAtLeast(0)

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            statusLabels
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.statusSpinner.adapter = spinnerAdapter
        binding.statusSpinner.setSelection(currentIndex)

        return AlertDialog.Builder(requireContext())
            .setTitle("Изменить статус")
            .setView(binding.root)
            .setPositiveButton("Подтвердить") { _, _ ->
                val selectedIndex = binding.statusSpinner.selectedItemPosition
                val newStatus = statusValues[selectedIndex]
                val comment = binding.commentEditText.text?.toString()
                listener?.onStatusChanged(newStatus, comment)
            }
            .setNegativeButton("Отмена", null)
            .create()
    }

    companion object {
        const val TAG = "StatusChangeDialog"
        private const val ARG_CURRENT_STATUS = "currentStatus"

        fun newInstance(currentStatus: String) = StatusChangeDialog().apply {
            arguments = Bundle().apply { putString(ARG_CURRENT_STATUS, currentStatus) }
        }
    }
}
