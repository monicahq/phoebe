package org.monicahq.phoebe.ui.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.monicahq.phoebe.R
import org.monicahq.phoebe.data.ContactsAdapter
import org.monicahq.phoebe.databinding.DashboardFragmentBinding

class DashboardFragment : Fragment() {

    private val viewModel: DashboardViewModel by activityViewModels { DashboardViewModelFactory(this.activity!!) }

    private var _binding: DashboardFragmentBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val b get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DashboardFragmentBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ContactsAdapter(this.context)
        b.recyclerview.adapter = adapter
        b.recyclerview.layoutManager = LinearLayoutManager(this.context)

        viewModel.contacts.observe(viewLifecycleOwner, Observer { contacts ->
            contacts.let {
                adapter.setContacts(it)
            }
        })
    }
}
