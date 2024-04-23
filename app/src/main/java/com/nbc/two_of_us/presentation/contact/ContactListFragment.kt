package com.nbc.two_of_us.presentation.contact

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Contacts
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.nbc.two_of_us.R
import com.nbc.two_of_us.data.ContactInfo
import com.nbc.two_of_us.data.ContactManager
import com.nbc.two_of_us.databinding.FragmentContactListBinding
import com.nbc.two_of_us.permission.ContactDatasource
import com.nbc.two_of_us.permission.PermissionManager
import com.nbc.two_of_us.presentation.contact_detail.ContactDetailFragment
import com.nbc.two_of_us.presentation.contact_detail.ContactDetailFragment.Companion.BUNDLE_KEY_FOR_CONTACT_INFO


class ContactListFragment : Fragment() {

    private var _binding: FragmentContactListBinding? = null
    private val binding: FragmentContactListBinding
        get() = _binding!!

    private val permissionManager by lazy {
        PermissionManager(this)
    }
    private val contactDatasource by lazy {
        ContactDatasource(requireContext())
    }
    private val adapter = ContactAdapter()

    private val contactsPermissionDialog by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.contacts_permission_dialog_title))
            .setMessage(getString(R.string.contacts_permission_dialog_message))
            .setNegativeButton(getString(R.string.contacts_permission_dialog_negative)) { _: DialogInterface, _: Int ->
            }
            .setPositiveButton(getString(R.string.contacts_permission_dialog_positive)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:" + requireContext().packageName)
                }
                startActivity(intent)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getContactsInfo()
    }

    private fun getContactsInfo() {
        permissionManager.getPermission(
            Manifest.permission.READ_CONTACTS,
            onGranted = {
                changePermissionViewVisibility(View.GONE)
                contactDatasource.getAllContacts {
                    for (contactInfo in it) {
                        ContactManager.add(contactInfo)
                    }
                    adapter.add(it)
                }
            },
            onDenied = {
                changePermissionViewVisibility(View.VISIBLE)
                contactsPermissionDialog.show()
            }
        )
    }

    private fun changePermissionViewVisibility(visibility: Int) = with(binding) {
        ivEmptyContact.visibility = visibility
        btnRequestPermission.visibility = visibility
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        //recyclerView 그리기
        fragmentListListRecyclerView.adapter = adapter
        fragmentListListRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        setListener()
    }

    private fun setListener() = with(binding) {
        //아이템 클릭 이벤트
        adapter.itemClick = object : ContactAdapter.ItemClick {
            override fun onClick(contactInfo : ContactInfo) {
                //데이터를 번들로 넘겨줌
                val bundle = Bundle().apply {
                    putParcelable(BUNDLE_KEY_FOR_CONTACT_INFO, contactInfo)
                }

                val fragmentDetail = ContactDetailFragment()
                fragmentDetail.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, fragmentDetail)
                    .addToBackStack(null)
                    .commit()
            }
        }

        //FAB 클릭 이벤트
        fragmentListAddButtonFab.setOnClickListener {

//            val fragmentAddDialog = AddContactDialogFragment()
//            fragmentAddDialog.show(parentFragmentManager, "add_contact_dialog")
        }

        btnRequestPermission.setOnClickListener {
            getContactsInfo()
        }
    }

    override fun onDestroyView() {
        _binding = null

        super.onDestroyView()
    }
}